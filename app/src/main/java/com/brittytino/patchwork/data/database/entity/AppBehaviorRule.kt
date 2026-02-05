package com.brittytino.patchwork.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "app_behavior_rules")
data class AppBehaviorRule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val packageName: String,
    val appName: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Audio Control
    val setRingVolume: Int? = null, // 0-100
    val setMediaVolume: Int? = null,
    val setNotificationVolume: Int? = null,
    val muteOnEntry: Boolean = false,
    
    // Display Control
    val setBrightness: Int? = null, // 0-255
    val keepScreenAwake: Boolean = false,
    val setScreenTimeout: Int? = null,
    val enableNightLight: Boolean? = null,
    val setOrientation: String? = null, // PORTRAIT, LANDSCAPE, AUTO
    
    // Privacy Control
    val disableScreenshots: Boolean = false,
    val clearClipboardOnExit: Boolean = false,
    val disableNotificationPeeking: Boolean = false,
    
    // Notifications
    val blockNotifications: Boolean = false,
    val hideNotificationContents: Boolean = false,
    
    // Network Control
    val blockNetworkAccess: Boolean = false,
    val allowOnlyWifi: Boolean = false,
    
    // Performance
    val prioritizePower: Boolean = false,
    val priorityLevel: Int? = null,
    
    // Metadata
    val notes: String = "",
    val lastAppliedAt: Long? = null,
    val applyCount: Int = 0
)
