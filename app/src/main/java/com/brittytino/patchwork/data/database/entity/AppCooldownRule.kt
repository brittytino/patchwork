package com.brittytino.patchwork.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "app_cooldown_rules")
data class AppCooldownRule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val packageName: String,
    val appName: String,
    val enabled: Boolean = true,
    
    // Cooldown Configuration
    val cooldownPeriodMinutes: Int = 30,
    val maxDailyOpens: Int? = null,
    val maxHourlyOpens: Int? = null,
    
    // Behavior Options
    val showWarningDialog: Boolean = true,
    val blockLaunch: Boolean = false,
    
    // Statistics
    val timesStopped: Int = 0,
    val timesBypassed: Int = 0,
    val lastTriggered: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_usage_events")
data class AppUsageEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val wasBlocked: Boolean = false,
    val durationMs: Long? = null
)
