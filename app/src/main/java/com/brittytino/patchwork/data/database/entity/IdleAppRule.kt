package com.brittytino.patchwork.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "idle_app_rules")
data class IdleAppRule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val packageName: String,
    val appName: String,
    val enabled: Boolean = true,
    
    // Trigger Configuration
    val idleThresholdMinutes: Int = 180, // 3 hours default
    val action: Action = Action.NOTIFY,
    
    // Statistics
    val actionCount: Int = 0,
    val lastCheckedAt: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class Action {
        FREEZE,
        KILL,
        CLEAR_CACHE,
        NOTIFY
    }
}

@Entity(tableName = "idle_app_actions_log")
data class IdleAppActionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val packageName: String,
    val appName: String,
    val action: IdleAppRule.Action,
    val timestamp: Long,
    val idleTimeMinutes: Int,
    val success: Boolean,
    val errorMessage: String? = null
)
