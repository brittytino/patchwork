package com.brittytino.patchwork.services

import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.database.entity.TriggerSource
import com.brittytino.patchwork.data.repository.AppBehaviorRuleRepository
import com.brittytino.patchwork.utils.ActionLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Engine that applies per-app behavior rules when apps enter/exit foreground
 */
class AppBehaviorEngine(private val context: Context) {
    
    private val repository = AppBehaviorRuleRepository(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val contentResolver = context.contentResolver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var currentForegroundPackage: String? = null
    private var previousState: SystemState? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    data class SystemState(
        val ringVolume: Int,
        val mediaVolume: Int,
        val notificationVolume: Int,
        val brightness: Int?,
        val screenTimeout: Int?
    )
    
    fun onAppEnterForeground(packageName: String, appName: String) {
        // Skip system UI and Patchwork itself
        if (packageName == "com.brittytino.patchwork" || 
            packageName == "com.android.systemui") {
            return
        }
        
        // Exit previous app first
        currentForegroundPackage?.let { exitApp(it) }
        
        scope.launch {
            try {
                val rule = repository.getRuleForApp(packageName)
                if (rule == null || !rule.enabled) {
                    currentForegroundPackage = packageName
                    return@launch
                }
                
                // Save current state for restoration
                previousState = captureCurrentState()
                
                val results = mutableListOf<String>()
                
                // Apply audio changes
                rule.setMediaVolume?.let { volumePercent ->
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val newVolume = (volumePercent / 100f * maxVolume).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                    results.add("Media volume → $volumePercent%")
                }
                
                rule.setRingVolume?.let { volumePercent ->
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                    val newVolume = (volumePercent / 100f * maxVolume).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, newVolume, 0)
                    results.add("Ring volume → $volumePercent%")
                }
                
                if (rule.muteOnEntry) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                    results.add("Muted")
                }
                
                // Apply display changes
                rule.setBrightness?.let { brightness ->
                    try {
                        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                        results.add("Brightness → ${(brightness * 100 / 255)}%")
                    } catch (e: SecurityException) {
                        Log.w(TAG, "No WRITE_SETTINGS permission for brightness")
                    }
                }
                
                if (rule.keepScreenAwake) {
                    acquireWakeLock()
                    results.add("Keep screen awake")
                }
                
                rule.setScreenTimeout?.let { timeout ->
                    try {
                        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeout)
                        results.add("Screen timeout → ${timeout / 1000}s")
                    } catch (e: SecurityException) {
                        Log.w(TAG, "No WRITE_SETTINGS permission for screen timeout")
                    }
                }
                
                rule.enableNightLight?.let { enabled ->
                    try {
                        Settings.Secure.putInt(contentResolver, "night_display_activated", if (enabled) 1 else 0)
                        results.add("Night Light → ${if (enabled) "ON" else "OFF"}")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to toggle Night Light")
                    }
                }
                
                // Update rule stats
                repository.markRuleApplied(rule.id)
                
                // Log to action history
                if (results.isNotEmpty()) {
                    ActionLogger.log(
                        ActionType.APP_BEHAVIOR_APPLIED,
                        "App Behavior",
                        "Rules Applied",
                        "Applied rules for $appName: ${results.joinToString(", ")}",
                        targetApp = packageName,
                        triggerSource = TriggerSource.APP_BEHAVIOR
                    )
                }
                
                currentForegroundPackage = packageName
                
            } catch (e: Exception) {
                Log.e(TAG, "Error applying app behavior rules", e)
            }
        }
    }
    
    fun onAppExitForeground(packageName: String) {
        if (packageName != currentForegroundPackage) return
        
        scope.launch {
            try {
                val rule = repository.getRuleForApp(packageName)
                if (rule == null || !rule.enabled) return@launch
                
                // Restore previous state
                previousState?.let { restoreSystemState(it) }
                
                // Release wake lock
                if (rule.keepScreenAwake) {
                    releaseWakeLock()
                }
                
                // Clear clipboard if requested
                if (rule.clearClipboardOnExit) {
                    clearClipboard()
                }
                
                ActionLogger.log(
                    ActionType.APP_BEHAVIOR_APPLIED,
                    "App Behavior",
                    "Rules Reverted",
                    "Restored system state after exiting ${rule.appName}",
                    targetApp = packageName,
                    triggerSource = TriggerSource.APP_BEHAVIOR
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error reverting app behavior rules", e)
            } finally {
                currentForegroundPackage = null
                previousState = null
            }
        }
    }
    
    private fun captureCurrentState(): SystemState {
        return SystemState(
            ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING),
            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
            notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
            brightness = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                null
            },
            screenTimeout = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            } catch (e: Exception) {
                null
            }
        )
    }
    
    private fun restoreSystemState(state: SystemState) {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, state.ringVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, state.mediaVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, state.notificationVolume, 0)
        
        state.brightness?.let {
            try {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, it)
            } catch (e: SecurityException) {
                Log.w(TAG, "No WRITE_SETTINGS permission")
            }
        }
        
        state.screenTimeout?.let {
            try {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, it)
            } catch (e: SecurityException) {
                Log.w(TAG, "No WRITE_SETTINGS permission")
            }
        }
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "Patchwork:AppBehaviorWakeLock"
            )
        }
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
    
    private fun clearClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear clipboard", e)
        }
    }
    
    private fun exitApp(packageName: String) {
        // Already handled by onAppExitForeground
        onAppExitForeground(packageName)
    }
    
    companion object {
        private const val TAG = "AppBehaviorEngine"
        
        @Volatile
        private var INSTANCE: AppBehaviorEngine? = null
        
        fun getInstance(context: Context): AppBehaviorEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppBehaviorEngine(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
