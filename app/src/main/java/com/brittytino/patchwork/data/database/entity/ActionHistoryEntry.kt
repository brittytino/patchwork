package com.brittytino.patchwork.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_history")
data class ActionHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long, // System.currentTimeMillis()
    
    val actionType: ActionType,
    
    val category: String, // "System", "App", "Automation", "Security", etc.
    
    val title: String, // "App Frozen", "Volume Changed", "Night Light Enabled"
    
    val description: String, // Detailed description
    
    val targetApp: String? = null, // Package name if app-specific
    
    val triggerSource: TriggerSource,
    
    val success: Boolean = true,
    
    val errorMessage: String? = null,
    
    val metadata: String? = null // JSON for additional data
)

enum class ActionType {
    APP_FROZEN,
    APP_UNFROZEN,
    VOLUME_CHANGED,
    BRIGHTNESS_CHANGED,
    NIGHT_LIGHT_TOGGLED,
    AOD_TOGGLED,
    WIFI_TOGGLED,
    BLUETOOTH_TOGGLED,
    AUTOMATION_TRIGGERED,
    AUTOMATION_CREATED,
    AUTOMATION_DELETED,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,
    QS_TILE_TOGGLED,
    SYSTEM_SETTING_CHANGED,
    APP_BEHAVIOR_APPLIED,
    APP_COOLDOWN_TRIGGERED,
    APP_COOLDOWN_BLOCKED,
    IDLE_APP_ACTION,
    SERVICE_STOPPED,
    NOTIFICATION_INTERCEPTED,
    SNAPSHOT_SAVED,
    SNAPSHOT_RESTORED
}

enum class TriggerSource {
    USER_MANUAL,      // User clicked a button
    AUTOMATION,       // DIY automation triggered
    APP_BEHAVIOR,     // App behavior rule
    APP_COOLDOWN,     // Cooldown engine
    IDLE_APP_ENGINE,  // Idle app monitoring
    SCHEDULE,         // Time-based trigger
    SYSTEM_EVENT      // Screen on/off, charging, etc.
}
