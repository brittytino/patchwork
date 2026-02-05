package com.brittytino.patchwork.utils

import android.content.Context
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.database.entity.TriggerSource
import com.brittytino.patchwork.data.repository.ActionHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Global logger for all Patchwork actions
 * Provides transparency and accountability
 */
object ActionLogger {
    private lateinit var repository: ActionHistoryRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false
    
    fun init(context: Context) {
        if (isInitialized) return
        
        val database = AppDatabase.getInstance(context.applicationContext)
        repository = ActionHistoryRepository(database.actionHistoryDao())
        isInitialized = true
    }
    
    fun log(
        type: ActionType,
        category: String,
        title: String,
        description: String,
        targetApp: String? = null,
        triggerSource: TriggerSource = TriggerSource.USER_MANUAL,
        success: Boolean = true,
        errorMessage: String? = null,
        metadata: Map<String, Any>? = null
    ) {
        if (!isInitialized) {
            android.util.Log.w("ActionLogger", "ActionLogger not initialized, skipping log")
            return
        }
        
        scope.launch {
            try {
                repository.logAction(
                    type, category, title, description,
                    targetApp, triggerSource, success, errorMessage, metadata
                )
            } catch (e: Exception) {
                android.util.Log.e("ActionLogger", "Error logging action", e)
            }
        }
    }
    
    // Convenience methods for common actions
    fun logAppFrozen(packageName: String, appName: String, triggerSource: TriggerSource = TriggerSource.USER_MANUAL) {
        log(
            ActionType.APP_FROZEN,
            "App Management",
            "App Frozen",
            "Froze $appName",
            targetApp = packageName,
            triggerSource = triggerSource
        )
    }
    
    fun logAppUnfrozen(packageName: String, appName: String, triggerSource: TriggerSource = TriggerSource.USER_MANUAL) {
        log(
            ActionType.APP_UNFROZEN,
            "App Management",
            "App Unfrozen",
            "Unfroze $appName",
            targetApp = packageName,
            triggerSource = triggerSource
        )
    }
    
    fun logAutomationTriggered(automationName: String, actions: List<String>) {
        log(
            ActionType.AUTOMATION_TRIGGERED,
            "Automation",
            "Automation Executed",
            "Triggered '$automationName': ${actions.joinToString(", ")}",
            triggerSource = TriggerSource.AUTOMATION
        )
    }
    
    fun logSystemSettingChanged(
        settingName: String, 
        oldValue: String, 
        newValue: String, 
        triggerSource: TriggerSource = TriggerSource.USER_MANUAL
    ) {
        log(
            ActionType.SYSTEM_SETTING_CHANGED,
            "System",
            "Setting Changed",
            "$settingName: $oldValue → $newValue",
            triggerSource = triggerSource,
            metadata = mapOf("setting" to settingName, "old" to oldValue, "new" to newValue)
        )
    }
    
    fun logQSTileToggled(tileName: String, enabled: Boolean) {
        log(
            ActionType.QS_TILE_TOGGLED,
            "Quick Settings",
            "Tile Toggled",
            "$tileName: ${if (enabled) "ON" else "OFF"}",
            triggerSource = TriggerSource.USER_MANUAL
        )
    }
    
    fun logVolumeChanged(volumeType: String, newLevel: Int) {
        log(
            ActionType.VOLUME_CHANGED,
            "System",
            "Volume Changed",
            "$volumeType volume set to $newLevel",
            triggerSource = TriggerSource.USER_MANUAL
        )
    }
    
    fun logBrightnessChanged(newBrightness: Int) {
        log(
            ActionType.BRIGHTNESS_CHANGED,
            "System",
            "Brightness Changed",
            "Screen brightness set to $newBrightness",
            triggerSource = TriggerSource.USER_MANUAL
        )
    }
}
