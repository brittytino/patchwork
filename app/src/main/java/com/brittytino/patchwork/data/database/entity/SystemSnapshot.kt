package com.brittytino.patchwork.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "system_snapshots")
data class SystemSnapshot(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    val description: String = "",
    val iconName: String = "default",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    
    // Audio Settings
    val ringVolume: Int? = null,
    val mediaVolume: Int? = null,
    val alarmVolume: Int? = null,
    val notificationVolume: Int? = null,
    val soundMode: String? = null, // NORMAL, VIBRATE, SILENT
    
    // Display Settings
    val brightness: Int? = null,
    val brightnessMode: String? = null, // MANUAL, AUTO
    val screenTimeout: Int? = null,
    val nightLightEnabled: Boolean? = null,
    val aodEnabled: Boolean? = null,
    val blueFilterEnabled: Boolean? = null,
    
    // Connectivity
    val wifiEnabled: Boolean? = null,
    val bluetoothEnabled: Boolean? = null,
    val mobileDataEnabled: Boolean? = null,
    val nfcEnabled: Boolean? = null,
    val airplaneModeEnabled: Boolean? = null,
    
    // Other
    val rotationLocked: Boolean? = null,
    val doNotDisturbMode: Int? = null,
    
    // Metadata
    val isQuickAccess: Boolean = false,
    val useCount: Int = 0
)
