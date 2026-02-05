package com.brittytino.patchwork.data.repository

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.dao.SystemSnapshotDao
import com.brittytino.patchwork.data.database.entity.SystemSnapshot
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.database.entity.TriggerSource
import com.brittytino.patchwork.utils.ActionLogger
import kotlinx.coroutines.flow.Flow

class SystemSnapshotRepository(private val context: Context) {
    
    private val dao: SystemSnapshotDao
    private val audioManager: AudioManager
    private val contentResolver = context.contentResolver
    
    init {
        val database = AppDatabase.getInstance(context)
        dao = database.systemSnapshotDao()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    fun getAllSnapshots(): Flow<List<SystemSnapshot>> = dao.getAllSnapshots()
    
    fun getQuickAccessSnapshots(): Flow<List<SystemSnapshot>> = dao.getQuickAccessSnapshots()
    
    suspend fun getSnapshot(id: String): SystemSnapshot? = dao.getSnapshot(id)
    
    suspend fun createSnapshotFromCurrent(name: String, description: String = ""): SystemSnapshot {
        val snapshot = SystemSnapshot(
            name = name,
            description = description,
            
            // Capture current audio settings
            ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING),
            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
            alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM),
            notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
            soundMode = getCurrentSoundMode(),
            
            // Capture current display settings
            brightness = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                null
            },
            brightnessMode = try {
                val mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) "AUTO" else "MANUAL"
            } catch (e: Exception) {
                null
            },
            screenTimeout = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            } catch (e: Exception) {
                null
            },
            nightLightEnabled = getNightLightState(),
            
            // Capture rotation lock
            rotationLocked = try {
                Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 0
            } catch (e: Exception) {
                null
            }
        )
        
        dao.insertSnapshot(snapshot)
        
        ActionLogger.log(
            ActionType.SNAPSHOT_SAVED,
            "System",
            "Snapshot Created",
            "Created snapshot '$name'",
            triggerSource = TriggerSource.USER_MANUAL
        )
        
        return snapshot
    }
    
    suspend fun restoreSnapshot(snapshot: SystemSnapshot) {
        val changedSettings = mutableListOf<String>()
        
        try {
            // Restore audio
            snapshot.ringVolume?.let {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, it, 0)
                changedSettings.add("Ring Volume → $it")
            }
            snapshot.mediaVolume?.let {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it, 0)
                changedSettings.add("Media Volume → $it")
            }
            snapshot.alarmVolume?.let {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
                changedSettings.add("Alarm Volume → $it")
            }
            snapshot.notificationVolume?.let {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, it, 0)
                changedSettings.add("Notification Volume → $it")
            }
            
            // Restore display
            snapshot.brightness?.let {
                try {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, it)
                    changedSettings.add("Brightness → ${(it * 100 / 255)}%")
                } catch (e: SecurityException) {
                    // Need WRITE_SETTINGS permission
                }
            }
            
            snapshot.brightnessMode?.let { mode ->
                try {
                    val modeInt = if (mode == "AUTO") 
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC 
                    else 
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, modeInt)
                    changedSettings.add("Brightness Mode → $mode")
                } catch (e: SecurityException) {
                    // Need WRITE_SETTINGS permission
                }
            }
            
            snapshot.screenTimeout?.let {
                try {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, it)
                    changedSettings.add("Screen Timeout → ${it / 1000}s")
                } catch (e: SecurityException) {
                    // Need WRITE_SETTINGS permission
                }
            }
            
            snapshot.rotationLocked?.let { locked ->
                try {
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.ACCELEROMETER_ROTATION,
                        if (locked) 0 else 1
                    )
                    changedSettings.add("Rotation → ${if (locked) "Locked" else "Auto"}")
                } catch (e: SecurityException) {
                    // Need WRITE_SETTINGS permission
                }
            }
            
            // Mark as used
            dao.markSnapshotUsed(snapshot.id, System.currentTimeMillis())
            
            ActionLogger.log(
                ActionType.SNAPSHOT_RESTORED,
                "System",
                "Snapshot Restored",
                "Restored '${snapshot.name}': ${changedSettings.joinToString(", ")}",
                triggerSource = TriggerSource.USER_MANUAL
            )
            
        } catch (e: Exception) {
            android.util.Log.e("SystemSnapshotRepo", "Error restoring snapshot", e)
        }
    }
    
    suspend fun updateSnapshot(snapshot: SystemSnapshot) {
        dao.updateSnapshot(snapshot)
    }
    
    suspend fun deleteSnapshot(snapshot: SystemSnapshot) {
        dao.deleteSnapshot(snapshot)
    }
    
    private fun getCurrentSoundMode(): String {
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "SILENT"
            AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
            AudioManager.RINGER_MODE_NORMAL -> "NORMAL"
            else -> "NORMAL"
        }
    }
    
    private fun getNightLightState(): Boolean? {
        return try {
            Settings.Secure.getInt(contentResolver, "night_display_activated") == 1
        } catch (e: Exception) {
            null
        }
    }
}
