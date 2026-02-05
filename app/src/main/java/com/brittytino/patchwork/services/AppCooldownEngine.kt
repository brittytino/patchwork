package com.brittytino.patchwork.services

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.WindowManager
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.AppUsageEvent
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.database.entity.TriggerSource
import com.brittytino.patchwork.data.repository.AppCooldownRepository
import com.brittytino.patchwork.utils.ActionLogger
import com.brittytino.patchwork.data.database.entity.ActionHistoryEntry
import kotlinx.coroutines.*

/**
 * Smart App Cooldown Engine
 * Prevents compulsive app reopening by enforcing cooldown periods and usage limits
 */
class AppCooldownEngine private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AppCooldownEngine"
        
        @Volatile
        private var instance: AppCooldownEngine? = null
        
        fun getInstance(context: Context): AppCooldownEngine {
            return instance ?: synchronized(this) {
                instance ?: AppCooldownEngine(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val repository = AppCooldownRepository(AppDatabase.getInstance(context))
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val activeApps = mutableMapOf<String, Long>() // packageName -> startTime
    
    /**
     * Check if app should be blocked from launching
     * Returns Pair<shouldBlock, reasonMessage>
     */
    suspend fun checkAppLaunch(packageName: String, appName: String): Pair<Boolean, String?> {
        try {
            val (shouldBlock, reason) = repository.shouldBlockAppLaunch(packageName)
            
            if (shouldBlock) {
                ActionLogger.log(
                    type = ActionType.APP_COOLDOWN_BLOCKED,
                    category = "App Control",
                    title = "App Launch Blocked",
                    description = "$appName was blocked: $reason",
                    targetApp = packageName,
                    triggerSource = TriggerSource.APP_COOLDOWN,
                    success = true
                )
            }
            
            return Pair(shouldBlock, reason)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking app launch", e)
            return Pair(false, null)
        }
    }
    
    /**
     * Record that an app was opened
     */
    fun onAppOpened(packageName: String, appName: String) {
        scope.launch {
            try {
                val now = System.currentTimeMillis()
                activeApps[packageName] = now
                
                // Record usage event (duration will be set when closed)
                val event = AppUsageEvent(
                    packageName = packageName,
                    appName = appName,
                    timestamp = now,
                    wasBlocked = false,
                    durationMs = null
                )
                repository.recordUsageEvent(event)
                
                ActionLogger.log(
                    type = ActionType.APP_COOLDOWN_TRIGGERED,
                    category = "App Control",
                    title = "App Opened",
                    description = "$appName opened",
                    targetApp = packageName,
                    triggerSource = TriggerSource.APP_COOLDOWN,
                    success = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error recording app open", e)
            }
        }
    }
    
    /**
     * Record that an app was closed
     */
    fun onAppClosed(packageName: String, appName: String) {
        scope.launch {
            try {
                val startTime = activeApps.remove(packageName)
                if (startTime != null) {
                    val duration = System.currentTimeMillis() - startTime
                    Log.d(TAG, "$appName closed after ${duration}ms")
                    // TODO: Add method to update the last event's duration
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recording app close", e)
            }
        }
    }
    
    /**
     * Show blocking dialog to user
     */
    fun showBlockingDialog(packageName: String, appName: String, reason: String, onDismiss: () -> Unit) {
        try {
            val dialog = AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("⏸️ App Cooldown Active")
                .setMessage("$appName is currently in cooldown.\n\n$reason\n\nTake a break and try something else! 🌟")
                .setPositiveButton("OK") { _, _ -> onDismiss() }
                .setNegativeButton("View Settings") { _, _ ->
                    // TODO: Navigate to cooldown settings
                    onDismiss()
                }
                .setCancelable(false)
                .create()
            
            // Make dialog show over other apps
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing blocking dialog", e)
            onDismiss()
        }
    }
    
    /**
     * Get remaining cooldown time for an app in milliseconds
     */
    suspend fun getRemainingCooldown(packageName: String): Long {
        return try {
            repository.getRemainingCooldownMs(packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting remaining cooldown", e)
            0L
        }
    }
    
    /**
     * Get usage statistics for an app
     */
    suspend fun getAppStats(packageName: String): AppStats {
        return try {
            val now = System.currentTimeMillis()
            val dayStart = now - (now % 86400000)
            val hourStart = now - 3600000
            
            val todayOpens = repository.getAppOpenCountInWindow(packageName, dayStart, now)
            val hourlyOpens = repository.getAppOpenCountInWindow(packageName, hourStart, now)
            val totalScreenTime = repository.getTotalScreenTime(packageName, dayStart)
            
            AppStats(
                todayOpens = todayOpens,
                hourlyOpens = hourlyOpens,
                totalScreenTimeMs = totalScreenTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app stats", e)
            AppStats(0, 0, 0L)
        }
    }
    
    /**
     * Cleanup old usage events (keep last 30 days)
     */
    fun scheduleCleanup() {
        scope.launch {
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                repository.cleanupOldEvents(thirtyDaysAgo)
                Log.d(TAG, "Cleaned up old usage events")
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up events", e)
            }
        }
    }
    
    data class AppStats(
        val todayOpens: Int,
        val hourlyOpens: Int,
        val totalScreenTimeMs: Long
    )
}
