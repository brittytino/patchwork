# Patchwork Core Features - Implementation Guide
**Behavior Control Tools for Universal Android Compatibility**

> Last Updated: February 4, 2026  
> Philosophy: Behavior Control, Not Cosmetic Control

---

## Table of Contents
1. [Action History Timeline](#1-action-history-timeline)
2. [System State Snapshots](#2-system-state-snapshots)
3. [App Behavior Controller](#3-app-behavior-controller)
4. [Smart App Cooldown](#4-smart-app-cooldown)
5. [Idle App Auto-Action Engine](#5-idle-app-auto-action-engine)
6. [Permission Abuse Notifier](#6-permission-abuse-notifier)
7. [Foreground Service Watchdog](#7-foreground-service-watchdog)
8. [Quick Wins & Bonus Features](#8-bonus-features)

---

# 1. Action History Timeline

## 📋 Overview
A local, private log that records every action Patchwork performs on the device. Provides transparency and accountability for power users.

## 🎯 User Benefits
- **Transparency**: See exactly what Patchwork changed and when
- **Debugging**: Identify which automation caused unexpected behavior
- **Audit Trail**: Track system modifications over time
- **Trust Building**: Proof that Patchwork isn't doing anything malicious
- **Learning**: Understand how automations actually work

## 🛠️ Technical Implementation

### Database Schema (Room)
```kotlin
@Entity(tableName = "action_history")
data class ActionHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long, // System.currentTimeMillis()
    
    val actionType: ActionType, // Enum: AUTOMATION, TOGGLE, FREEZE, PERMISSION, etc.
    
    val category: String, // "System", "App", "Automation", "Security", etc.
    
    val title: String, // "App Frozen", "Volume Changed", "Night Light Enabled"
    
    val description: String, // Detailed description
    
    val targetApp: String? = null, // Package name if app-specific
    
    val triggerSource: TriggerSource, // USER, AUTOMATION, SYSTEM, SCHEDULE
    
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
    SERVICE_STOPPED,
    NOTIFICATION_INTERCEPTED,
    SNAPSHOT_SAVED,
    SNAPSHOT_RESTORED
}

enum class TriggerSource {
    USER_MANUAL,      // User clicked a button
    AUTOMATION,       // DIY automation triggered
    APP_BEHAVIOR,     // App behavior rule
    SCHEDULE,         // Time-based trigger
    SYSTEM_EVENT,     // Screen on/off, charging, etc.
    COOLDOWN,         // App cooldown triggered
    IDLE_DETECTION    // Idle app engine
}
```

### Repository
```kotlin
@Dao
interface ActionHistoryDao {
    @Query("SELECT * FROM action_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActions(limit: Int = 100): Flow<List<ActionHistoryEntry>>
    
    @Query("SELECT * FROM action_history WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getActionsSince(startTime: Long): Flow<List<ActionHistoryEntry>>
    
    @Query("SELECT * FROM action_history WHERE category = :category ORDER BY timestamp DESC")
    fun getActionsByCategory(category: String): Flow<List<ActionHistoryEntry>>
    
    @Query("SELECT * FROM action_history WHERE targetApp = :packageName ORDER BY timestamp DESC")
    fun getActionsByApp(packageName: String): Flow<List<ActionHistoryEntry>>
    
    @Query("SELECT * FROM action_history WHERE actionType = :type ORDER BY timestamp DESC")
    fun getActionsByType(type: ActionType): Flow<List<ActionHistoryEntry>>
    
    @Insert
    suspend fun insertAction(action: ActionHistoryEntry)
    
    @Query("DELETE FROM action_history WHERE timestamp < :beforeTime")
    suspend fun deleteOldActions(beforeTime: Long)
    
    @Query("DELETE FROM action_history")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM action_history")
    suspend fun getCount(): Int
}

class ActionHistoryRepository(private val dao: ActionHistoryDao) {
    
    fun getRecentActions(limit: Int = 100) = dao.getRecentActions(limit)
    
    fun getActionsSince(startTime: Long) = dao.getActionsSince(startTime)
    
    fun getActionsByCategory(category: String) = dao.getActionsByCategory(category)
    
    fun getActionsByApp(packageName: String) = dao.getActionsByApp(packageName)
    
    fun getActionsByType(type: ActionType) = dao.getActionsByType(type)
    
    suspend fun logAction(
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
        val entry = ActionHistoryEntry(
            timestamp = System.currentTimeMillis(),
            actionType = type,
            category = category,
            title = title,
            description = description,
            targetApp = targetApp,
            triggerSource = triggerSource,
            success = success,
            errorMessage = errorMessage,
            metadata = metadata?.let { Json.encodeToString(it) }
        )
        dao.insertAction(entry)
    }
    
    suspend fun cleanupOldLogs(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        dao.deleteOldActions(cutoffTime)
    }
}
```

### Logger Utility (Global Access)
```kotlin
object ActionLogger {
    private lateinit var repository: ActionHistoryRepository
    
    fun init(context: Context) {
        val database = AppDatabase.getInstance(context)
        repository = ActionHistoryRepository(database.actionHistoryDao())
    }
    
    suspend fun log(
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
        repository.logAction(
            type, category, title, description, 
            targetApp, triggerSource, success, errorMessage, metadata
        )
    }
    
    // Convenience methods
    suspend fun logAppFrozen(packageName: String, appName: String, triggerSource: TriggerSource) {
        log(
            ActionType.APP_FROZEN,
            "App Management",
            "App Frozen",
            "Froze $appName",
            targetApp = packageName,
            triggerSource = triggerSource
        )
    }
    
    suspend fun logAutomationTriggered(automationName: String, actions: List<String>) {
        log(
            ActionType.AUTOMATION_TRIGGERED,
            "Automation",
            "Automation Executed",
            "Triggered '$automationName': ${actions.joinToString(", ")}",
            triggerSource = TriggerSource.AUTOMATION
        )
    }
    
    suspend fun logSystemSettingChanged(settingName: String, oldValue: String, newValue: String, triggerSource: TriggerSource) {
        log(
            ActionType.SYSTEM_SETTING_CHANGED,
            "System",
            "Setting Changed",
            "$settingName: $oldValue → $newValue",
            triggerSource = triggerSource,
            metadata = mapOf("setting" to settingName, "old" to oldValue, "new" to newValue)
        )
    }
}
```

## 🎨 UI Design

### Main History Screen
```kotlin
@Composable
fun ActionHistoryScreen(
    viewModel: ActionHistoryViewModel
) {
    val actions by viewModel.actions.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Action History") },
                actions = {
                    // Filter button
                    IconButton(onClick = { viewModel.showFilterSheet() }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    // Clear all button
                    IconButton(onClick = { viewModel.showClearDialog() }) {
                        Icon(Icons.Default.Delete, "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Time range selector
            TimeRangeSelector(
                selected = viewModel.selectedTimeRange,
                onSelect = { viewModel.filterByTimeRange(it) }
            )
            
            // Action list
            LazyColumn {
                items(actions.groupBy { it.getDateHeader() }) { (date, actionsForDate) ->
                    DateHeader(date)
                    
                    actionsForDate.forEach { action ->
                        ActionHistoryItem(
                            action = action,
                            onClick = { viewModel.showActionDetails(action) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionHistoryItem(
    action: ActionHistoryEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on action type
            Icon(
                painter = painterResource(action.actionType.getIcon()),
                contentDescription = null,
                tint = if (action.success) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.error
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = action.getTimeAgo(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Trigger source badge
            TriggerSourceBadge(action.triggerSource)
        }
    }
}
```

## 🔧 Integration Points

### Where to Log Actions

**1. App Freezing (Already exists)**
```kotlin
// In FreezeHandler.kt or wherever app freezing happens
suspend fun freezeApp(packageName: String, appName: String) {
    val success = ShellUtils.execute("pm disable $packageName")
    
    ActionLogger.logAppFrozen(
        packageName = packageName,
        appName = appName,
        triggerSource = TriggerSource.USER_MANUAL
    )
}
```

**2. DIY Automation Execution**
```kotlin
// In AutomationExecutor
suspend fun executeAutomation(automation: Automation) {
    val actionDescriptions = mutableListOf<String>()
    
    automation.actions.forEach { action ->
        when (action) {
            is Action.TurnOnFlashlight -> {
                toggleFlashlight(true)
                actionDescriptions.add("Flashlight ON")
            }
            is Action.SetVolume -> {
                setVolume(action.level)
                actionDescriptions.add("Volume → ${action.level}")
            }
        }
    }
    
    ActionLogger.logAutomationTriggered(
        automationName = automation.name,
        actions = actionDescriptions
    )
}
```

**3. System Settings Changes**
```kotlin
// In any system setting toggle
fun toggleNightLight(enabled: Boolean) {
    val oldValue = Settings.Secure.getInt(contentResolver, "night_display_activated", 0)
    Settings.Secure.putInt(contentResolver, "night_display_activated", if (enabled) 1 else 0)
    
    GlobalScope.launch {
        ActionLogger.logSystemSettingChanged(
            settingName = "Night Light",
            oldValue = if (oldValue == 1) "On" else "Off",
            newValue = if (enabled) "On" else "Off",
            triggerSource = TriggerSource.USER_MANUAL
        )
    }
}
```

## ✅ Testing Requirements
- [ ] Log entries created correctly
- [ ] Filter by category works
- [ ] Filter by time range works
- [ ] Filter by app works
- [ ] Old logs cleaned up (30 days default)
- [ ] UI displays correctly with 0 items
- [ ] UI displays correctly with 1000+ items
- [ ] Action details sheet works
- [ ] Clear all confirmation works
- [ ] Export functionality (optional)

---

# 2. System State Snapshots

## 📋 Overview
Save and restore complete system state profiles for different contexts (Work, Home, Night, Travel, etc.). One-tap switching between configurations.

## 🎯 User Benefits
- **Instant Context Switch**: "Work mode" → "Home mode" in one tap
- **Consistency**: Always return to a known-good state
- **Convenience**: No manual adjustment of 10+ settings
- **Experimentation**: Try new settings, easily restore previous state
- **Profiles for Travel**: Airplane-friendly configurations

## 🛠️ Technical Implementation

### Snapshot Data Model
```kotlin
@Entity(tableName = "system_snapshots")
data class SystemSnapshot(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    val description: String = "",
    val iconName: String = "default", // For UI
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    
    // Audio Settings
    val ringVolume: Int? = null,
    val mediaVolume: Int? = null,
    val alarmVolume: Int? = null,
    val notificationVolume: Int? = null,
    val soundMode: SoundMode? = null, // NORMAL, VIBRATE, SILENT
    
    // Display Settings
    val brightness: Int? = null,
    val brightnessMode: BrightnessMode? = null, // MANUAL, AUTO
    val screenTimeout: Int? = null, // milliseconds
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
    val doNotDisturbMode: Int? = null, // OFF, PRIORITY, ALARMS, TOTAL_SILENCE
    
    // Metadata
    val isQuickAccess: Boolean = false, // Show in Quick Settings
    val useCount: Int = 0
)

enum class SoundMode { NORMAL, VIBRATE, SILENT }
enum class BrightnessMode { MANUAL, AUTO }

data class SnapshotDiff(
    val audio: List<String>,
    val display: List<String>,
    val connectivity: List<String>,
    val other: List<String>
)
```

### Repository
```kotlin
@Dao
interface SystemSnapshotDao {
    @Query("SELECT * FROM system_snapshots ORDER BY lastUsedAt DESC")
    fun getAllSnapshots(): Flow<List<SystemSnapshot>>
    
    @Query("SELECT * FROM system_snapshots WHERE isQuickAccess = 1")
    fun getQuickAccessSnapshots(): Flow<List<SystemSnapshot>>
    
    @Query("SELECT * FROM system_snapshots WHERE id = :id")
    suspend fun getSnapshot(id: String): SystemSnapshot?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SystemSnapshot)
    
    @Update
    suspend fun updateSnapshot(snapshot: SystemSnapshot)
    
    @Delete
    suspend fun deleteSnapshot(snapshot: SystemSnapshot)
    
    @Query("UPDATE system_snapshots SET lastUsedAt = :timestamp, useCount = useCount + 1 WHERE id = :id")
    suspend fun markSnapshotUsed(id: String, timestamp: Long)
}

class SystemSnapshotRepository(private val dao: SystemSnapshotDao, private val context: Context) {
    
    fun getAllSnapshots() = dao.getAllSnapshots()
    fun getQuickAccessSnapshots() = dao.getQuickAccessSnapshots()
    
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
            brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1),
            brightnessMode = if (Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 1) 
                BrightnessMode.AUTO else BrightnessMode.MANUAL,
            screenTimeout = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, -1),
            nightLightEnabled = getNightLightState(),
            aodEnabled = getAodState(),
            
            // Capture connectivity
            wifiEnabled = wifiManager.isWifiEnabled,
            bluetoothEnabled = bluetoothAdapter?.isEnabled,
            mobileDataEnabled = getMobileDataState(),
            nfcEnabled = getNfcState(),
            airplaneModeEnabled = getAirplaneModeState(),
            
            // Other
            rotationLocked = getRotationLockState(),
            doNotDisturbMode = notificationManager.currentInterruptionFilter
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
        
        // Restore audio
        snapshot.ringVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, it, 0)
            changedSettings.add("Ring Volume")
        }
        snapshot.mediaVolume?.let {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it, 0)
            changedSettings.add("Media Volume")
        }
        snapshot.soundMode?.let {
            setSoundMode(it)
            changedSettings.add("Sound Mode")
        }
        
        // Restore display
        snapshot.brightness?.let {
            if (PermissionUtils.canWriteSettings(context)) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, it)
                changedSettings.add("Brightness")
            }
        }
        snapshot.nightLightEnabled?.let {
            setNightLight(it)
            changedSettings.add("Night Light")
        }
        snapshot.aodEnabled?.let {
            setAod(it)
            changedSettings.add("AOD")
        }
        
        // Restore connectivity
        snapshot.wifiEnabled?.let {
            setWifi(it)
            changedSettings.add("Wi-Fi")
        }
        snapshot.bluetoothEnabled?.let {
            setBluetooth(it)
            changedSettings.add("Bluetooth")
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
    }
    
    suspend fun compareWithCurrent(snapshot: SystemSnapshot): SnapshotDiff {
        val audio = mutableListOf<String>()
        val display = mutableListOf<String>()
        val connectivity = mutableListOf<String>()
        val other = mutableListOf<String>()
        
        // Compare volumes
        snapshot.ringVolume?.let { saved ->
            val current = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            if (saved != current) audio.add("Ring: $current → $saved")
        }
        
        // Compare brightness
        snapshot.brightness?.let { saved ->
            val current = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
            if (saved != current) display.add("Brightness: $current → $saved")
        }
        
        // ... more comparisons
        
        return SnapshotDiff(audio, display, connectivity, other)
    }
}
```

## 🎨 UI Design

### Snapshots List Screen
```kotlin
@Composable
fun SystemSnapshotsScreen(
    viewModel: SystemSnapshotViewModel
) {
    val snapshots by viewModel.snapshots.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Profiles") },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "Create")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.CameraAlt, null) },
                text = { Text("Capture Current") }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Quick Access section
            item {
                Text(
                    "Quick Access",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            val quickAccess = snapshots.filter { it.isQuickAccess }
            if (quickAccess.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(quickAccess) { snapshot ->
                            SnapshotQuickCard(
                                snapshot = snapshot,
                                onRestore = { viewModel.restoreSnapshot(it) },
                                onClick = { viewModel.showSnapshotDetails(it) }
                            )
                        }
                    }
                }
            }
            
            // All Snapshots
            item {
                Text(
                    "All Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            items(snapshots) { snapshot ->
                SnapshotListItem(
                    snapshot = snapshot,
                    onRestore = { viewModel.restoreSnapshot(it) },
                    onEdit = { viewModel.editSnapshot(it) },
                    onDelete = { viewModel.deleteSnapshot(it) },
                    onClick = { viewModel.showSnapshotDetails(it) }
                )
            }
        }
    }
}

@Composable
fun SnapshotListItem(
    snapshot: SystemSnapshot,
    onRestore: (SystemSnapshot) -> Unit,
    onEdit: (SystemSnapshot) -> Unit,
    onDelete: (SystemSnapshot) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(snapshot.getIconRes()),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    snapshot.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (snapshot.description.isNotEmpty()) {
                    Text(
                        snapshot.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                snapshot.lastUsedAt?.let {
                    Text(
                        "Last used ${it.toTimeAgo()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            IconButton(onClick = { onRestore(snapshot) }) {
                Icon(Icons.Default.PlayArrow, "Restore")
            }
            
            IconButton(onClick = { /* Show menu */ }) {
                Icon(Icons.Default.MoreVert, "More")
            }
        }
    }
}
```

### Snapshot Details / Comparison View
```kotlin
@Composable
fun SnapshotDetailsSheet(
    snapshot: SystemSnapshot,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: SystemSnapshotViewModel = viewModel()
    val diff by viewModel.getDiff(snapshot).collectAsState(initial = null)
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                snapshot.name,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                "Created ${snapshot.createdAt.toReadableDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Show what will change
            diff?.let {
                Text("Changes from Current State:", style = MaterialTheme.typography.titleSmall)
                
                if (it.audio.isNotEmpty()) {
                    SectionHeader("Audio")
                    it.audio.forEach { change -> ChangeItem(change) }
                }
                
                if (it.display.isNotEmpty()) {
                    SectionHeader("Display")
                    it.display.forEach { change -> ChangeItem(change) }
                }
                
                if (it.connectivity.isNotEmpty()) {
                    SectionHeader("Connectivity")
                    it.connectivity.forEach { change -> ChangeItem(change) }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    onRestore()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Restore Profile")
            }
        }
    }
}
```

## 🔌 Quick Settings Tile Integration
```kotlin
class SystemSnapshotTileService : TileService() {
    
    private val repository by lazy { SystemSnapshotRepository(this) }
    
    override fun onClick() {
        // Show dialog to select snapshot
        startActivityAndCollapse(
            Intent(this, SnapshotSelectorActivity::class.java)
        )
    }
    
    override fun onStartListening() {
        lifecycleScope.launch {
            repository.getQuickAccessSnapshots().collectLatest { snapshots ->
                qsTile?.apply {
                    label = if (snapshots.isEmpty()) "No Profiles" else "Profiles (${snapshots.size})"
                    state = if (snapshots.isEmpty()) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
                    updateTile()
                }
            }
        }
    }
}
```

## ✅ Testing Requirements
- [ ] Snapshot creation captures all settings correctly
- [ ] Snapshot restoration applies settings correctly
- [ ] Permissions handled gracefully (fallback if no WRITE_SETTINGS)
- [ ] Comparison shows accurate diff
- [ ] Quick access shortcuts work
- [ ] Delete confirmation works
- [ ] Works on multiple Android versions
- [ ] Works on different manufacturers (test Xiaomi, Samsung, Pixel)

---

# 3. App Behavior Controller

## 📋 Overview
Define per-app behavior rules that automatically apply when the app is in the foreground. This is THE signature feature of Patchwork - context-aware behavior control.

## 🎯 User Benefits
- **Context-Aware**: Different behavior for different apps automatically
- **No Manual Switching**: Set it once, forget it
- **Privacy**: Auto-disable screenshots in banking apps
- **Focus**: Auto-block notifications during gaming
- **Battery**: Auto-adjust brightness for video apps
- **Convenience**: Volume adjusts automatically for social media

## 🛠️ Technical Implementation

### Behavior Rules Data Model
```kotlin
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
    val setScreenTimeout: Int? = null, // milliseconds
    val enableNightLight: Boolean? = null,
    val setOrientation: Orientation? = null, // PORTRAIT, LANDSCAPE, AUTO
    
    // Privacy Control
    val disableScreenshots: Boolean = false, // FLAG_SECURE via overlay
    val clearClipboardOnExit: Boolean = false,
    val disableNotificationPeeking: Boolean = false,
    
    // Notifications
    val blockNotifications: Boolean = false, // DND for this app's time
    val hideNotificationContents: Boolean = false,
    
    // Network Control (Advanced - requires VPN)
    val blockNetworkAccess: Boolean = false,
    val allowOnlyWifi: Boolean = false,
    
    // Performance
    val prioritizePower: Boolean = false, // Suggest to system
    val priorityLevel: Int? = null, // Nice value hint
    
    // Metadata
    val notes: String = "",
    val lastAppliedAt: Long? = null,
    val applyCount: Int = 0
)

enum class Orientation { PORTRAIT, LANDSCAPE, AUTO }

data class BehaviorActionResult(
    val success: Boolean,
    val action: String,
    val error: String? = null
)
```

### Behavior Engine
```kotlin
class AppBehaviorEngine(private val context: Context) {
    
    private val repository = AppBehaviorRepository(context)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private var currentForegroundPackage: String? = null
    private var previousState: SystemState? = null
    private var activeOverlay: View? = null
    
    data class SystemState(
        val ringVolume: Int,
        val mediaVolume: Int,
        val notificationVolume: Int,
        val brightness: Int,
        val screenTimeout: Int,
        val notificationFilter: Int
    )
    
    suspend fun onAppEnterForeground(packageName: String, appName: String) {
        // Exit previous app first
        currentForegroundPackage?.let { exitApp(it) }
        
        val rule = repository.getRuleForApp(packageName) ?: return
        if (!rule.enabled) return
        
        // Save current state for restoration
        previousState = captureCurrentState()
        
        val results = mutableListOf<BehaviorActionResult>()
        
        // Apply audio changes
        rule.setMediaVolume?.let {
            val newVolume = (it / 100f * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            results.add(BehaviorActionResult(true, "Media volume → $it%"))
        }
        
        rule.setRingVolume?.let {
            val newVolume = (it / 100f * audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_RING, newVolume, 0)
            results.add(BehaviorActionResult(true, "Ring volume → $it%"))
        }
        
        if (rule.muteOnEntry) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            results.add(BehaviorActionResult(true, "Muted"))
        }
        
        // Apply display changes
        rule.setBrightness?.let {
            if (PermissionUtils.canWriteSettings(context)) {
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, it)
                results.add(BehaviorActionResult(true, "Brightness → $it"))
            } else {
                results.add(BehaviorActionResult(false, "Brightness", "No permission"))
            }
        }
        
        if (rule.keepScreenAwake) {
            startWakeLock()
            results.add(BehaviorActionResult(true, "Keep screen awake"))
        }
        
        rule.enableNightLight?.let {
            setNightLight(it)
            results.add(BehaviorActionResult(true, "Night Light → ${if (it) "ON" else "OFF"}"))
        }
        
        // Apply privacy controls
        if (rule.disableScreenshots) {
            showSecureOverlay()
            results.add(BehaviorActionResult(true, "Screenshots blocked"))
        }
        
        // Apply notification controls
        if (rule.blockNotifications) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            results.add(BehaviorActionResult(true, "Notifications blocked"))
        }
        
        // Update rule stats
        repository.markRuleApplied(rule.id)
        
        // Log to action history
        ActionLogger.log(
            ActionType.APP_BEHAVIOR_APPLIED,
            "App Behavior",
            "Rules Applied",
            "Applied rules for $appName: ${results.filter { it.success }.map { it.action }.joinToString(", ")}",
            targetApp = packageName,
            triggerSource = TriggerSource.APP_BEHAVIOR
        )
        
        currentForegroundPackage = packageName
    }
    
    suspend fun onAppExitForeground(packageName: String) {
        val rule = repository.getRuleForApp(packageName) ?: return
        if (!rule.enabled) return
        
        // Restore previous state
        previousState?.let { restoreSystemState(it) }
        
        // Remove secure overlay
        removeSecureOverlay()
        
        // Release wake lock
        releaseWakeLock()
        
        // Clear clipboard if requested
        if (rule.clearClipboardOnExit) {
            clearClipboard()
        }
        
        // Restore notification filter
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        
        ActionLogger.log(
            ActionType.APP_BEHAVIOR_APPLIED,
            "App Behavior",
            "Rules Reverted",
            "Restored system state after exiting ${rule.appName}",
            targetApp = packageName,
            triggerSource = TriggerSource.APP_BEHAVIOR
        )
        
        currentForegroundPackage = null
        previousState = null
    }
    
    private fun captureCurrentState() = SystemState(
        ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING),
        mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
        notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
        brightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128),
        screenTimeout = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000),
        notificationFilter = notificationManager.currentInterruptionFilter
    )
    
    private fun restoreSystemState(state: SystemState) {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, state.ringVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, state.mediaVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, state.notificationVolume, 0)
        
        if (PermissionUtils.canWriteSettings(context)) {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, state.brightness)
        }
        
        notificationManager.setInterruptionFilter(state.notificationFilter)
    }
    
    private fun showSecureOverlay() {
        // Create transparent overlay with FLAG_SECURE
        val windowManager = context.getSystemService(WindowManager::class.java)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_SECURE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        activeOverlay = View(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }
        
        windowManager.addView(activeOverlay, layoutParams)
    }
    
    private fun removeSecureOverlay() {
        activeOverlay?.let {
            val windowManager = context.getSystemService(WindowManager::class.java)
            windowManager.removeView(it)
            activeOverlay = null
        }
    }
}
```

### Integration with Accessibility Service
```kotlin
// In InputEventListenerService or create AppBehaviorAccessibilityService

override fun onAccessibilityEvent(event: AccessibilityEvent) {
    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        val packageName = event.packageName?.toString() ?: return
        val appName = getAppName(packageName)
        
        if (packageName != previousPackage) {
            lifecycleScope.launch {
                // Exit previous app
                previousPackage?.let {
                    behaviorEngine.onAppExitForeground(it)
                }
                
                // Enter new app
                behaviorEngine.onAppEnterForeground(packageName, appName)
                
                previousPackage = packageName
            }
        }
    }
}
```

## 🎨 UI Design

### App Behavior Rules Screen
```kotlin
@Composable
fun AppBehaviorScreen(
    viewModel: AppBehaviorViewModel
) {
    val rules by viewModel.rules.collectAsState()
    val apps by viewModel.installedApps.collectAsState()
    var showAppPicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Behavior Control") },
                subtitle = { Text("${rules.size} rules active") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAppPicker = true }) {
                Icon(Icons.Default.Add, "Add Rule")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                InfoCard(
                    title = "What is App Behavior Control?",
                    description = "Define rules that automatically apply when you open specific apps. Control volume, brightness, privacy, and more - all contextually."
                )
            }
            
            if (rules.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.AppSettingsAlt,
                        title = "No Rules Yet",
                        description = "Tap + to create your first app behavior rule",
                        actionLabel = "Add Rule",
                        onAction = { showAppPicker = true }
                    )
                }
            } else {
                items(rules) { rule ->
                    AppBehaviorRuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleRule(it) },
                        onEdit = { viewModel.editRule(it) },
                        onDelete = { viewModel.deleteRule(it) }
                    )
                }
            }
        }
    }
    
    if (showAppPicker) {
        AppPickerDialog(
            apps = apps,
            onSelect = { app ->
                viewModel.createRuleForApp(app)
                showAppPicker = false
            },
            onDismiss = { showAppPicker = false }
        )
    }
}

@Composable
fun AppBehaviorRuleCard(
    rule: AppBehaviorRule,
    onToggle: (AppBehaviorRule) -> Unit,
    onEdit: (AppBehaviorRule) -> Unit,
    onDelete: (AppBehaviorRule) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onEdit(rule) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(packageName = rule.packageName, size = 40.dp)
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rule.appName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        rule.getSummary(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = { onToggle(rule) }
                )
                
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null)
                }
            }
            
            // Show active rules summary
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rule.setMediaVolume?.let {
                    Chip(text = "Volume: $it%")
                }
                if (rule.keepScreenAwake) {
                    Chip(text = "Keep Awake")
                }
                if (rule.disableScreenshots) {
                    Chip(text = "No Screenshots")
                }
                if (rule.blockNotifications) {
                    Chip(text = "Block Notifications")
                }
                rule.setBrightness?.let {
                    Chip(text = "Brightness: ${(it * 100 / 255)}%")
                }
            }
            
            if (rule.lastAppliedAt != null) {
                Text(
                    "Last applied ${rule.lastAppliedAt.toTimeAgo()} • Used ${rule.applyCount} times",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
```

### Rule Editor
```kotlin
@Composable
fun AppBehaviorRuleEditor(
    rule: AppBehaviorRule?,
    onSave: (AppBehaviorRule) -> Unit,
    onDismiss: () -> Unit
) {
    var editedRule by remember { mutableStateOf(rule ?: AppBehaviorRule(packageName = "", appName = "")) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "Edit Behavior for ${editedRule.appName}",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Audio Section
                SectionHeader("Audio Control")
                SliderSetting(
                    title = "Media Volume",
                    value = editedRule.setMediaVolume,
                    onValueChange = { editedRule = editedRule.copy(setMediaVolume = it) },
                    valueRange = 0f..100f,
                    enabled = editedRule.setMediaVolume != null,
                    onToggle = { enabled ->
                        editedRule = editedRule.copy(setMediaVolume = if (enabled) 50 else null)
                    }
                )
                
                SwitchSetting(
                    title = "Mute on Entry",
                    checked = editedRule.muteOnEntry,
                    onCheckedChange = { editedRule = editedRule.copy(muteOnEntry = it) }
                )
                
                // Display Section
                SectionHeader("Display Control")
                SliderSetting(
                    title = "Brightness",
                    value = editedRule.setBrightness,
                    onValueChange = { editedRule = editedRule.copy(setBrightness = it) },
                    valueRange = 0f..255f,
                    enabled = editedRule.setBrightness != null,
                    onToggle = { enabled ->
                        editedRule = editedRule.copy(setBrightness = if (enabled) 128 else null)
                    }
                )
                
                SwitchSetting(
                    title = "Keep Screen Awake",
                    description = "Prevent screen from turning off while this app is open",
                    checked = editedRule.keepScreenAwake,
                    onCheckedChange = { editedRule = editedRule.copy(keepScreenAwake = it) }
                )
                
                SwitchSetting(
                    title = "Night Light",
                    checked = editedRule.enableNightLight == true,
                    enabled = editedRule.enableNightLight != null,
                    onCheckedChange = { editedRule = editedRule.copy(enableNightLight = it) },
                    onToggle = { enabled ->
                        editedRule = editedRule.copy(enableNightLight = if (enabled) false else null)
                    }
                )
                
                // Privacy Section
                SectionHeader("Privacy & Security")
                SwitchSetting(
                    title = "Disable Screenshots",
                    description = "Prevent screenshots while this app is open",
                    checked = editedRule.disableScreenshots,
                    onCheckedChange = { editedRule = editedRule.copy(disableScreenshots = it) }
                )
                
                SwitchSetting(
                    title = "Clear Clipboard on Exit",
                    description = "Automatically clear clipboard when leaving this app",
                    checked = editedRule.clearClipboardOnExit,
                    onCheckedChange = { editedRule = editedRule.copy(clearClipboardOnExit = it) }
                )
                
                // Notifications Section
                SectionHeader("Notifications")
                SwitchSetting(
                    title = "Block Notifications",
                    description = "Enable Do Not Disturb while this app is open",
                    checked = editedRule.blockNotifications,
                    onCheckedChange = { editedRule = editedRule.copy(blockNotifications = it) }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Save/Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onSave(editedRule) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Rule")
                    }
                }
            }
        }
    }
}
```

## ✅ Testing Requirements
- [ ] Rules apply when app enters foreground
- [ ] Rules revert when app exits foreground
- [ ] Previous system state restored correctly
- [ ] Multiple rule changes work together
- [ ] Screenshot blocking works (overlay + FLAG_SECURE)
- [ ] Wake lock properly released
- [ ] Clipboard clears on exit
- [ ] Notification blocking works
- [ ] Works with rapid app switching
- [ ] Battery impact acceptable
- [ ] Works on multiple Android versions
- [ ] No crashes with system apps

---

*[Continue to Part 2 for remaining features...]*
