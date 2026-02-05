# Patchwork Core Features - Implementation Guide (Part 2)
**Behavior Control Tools for Universal Android Compatibility**

> Continuation from Part 1

---

# 4. Smart App Cooldown

## 📋 Overview
Prevents compulsive app reopening by adding a mandatory delay or confirmation before allowing the user to reopen an app they just closed. Helps break the "close Instagram, immediately reopen Instagram" loop.

## 🎯 User Benefits
- **Break Compulsive Habits**: Physical barrier to mindless reopening
- **Screen Time Reduction**: Adds friction to addictive app usage
- **Awareness Building**: Makes users aware of their app usage patterns
- **Customizable**: Different cooldown periods for different apps
- **Smart Detection**: Only triggers if app was closed recently (< X minutes)

## 🛠️ Technical Implementation

### Data Model
```kotlin
@Entity(tableName = "app_cooldown_rules")
data class AppCooldownRule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val packageName: String,
    val appName: String,
    val enabled: Boolean = true,
    
    // Cooldown Configuration
    val cooldownSeconds: Int = 30, // How long to wait before reopening
    val requireConfirmation: Boolean = true, // Show "Are you sure?" dialog
    val triggerThreshold: Int = 1, // How many times closed in X minutes triggers cooldown
    val triggerWindowMinutes: Int = 5, // Time window for threshold
    
    // Behavior Options
    val showTimer: Boolean = true, // Show countdown in notification
    val allowEmergencyBypass: Boolean = false, // Long press to skip
    val bypassOnImportantNotification: Boolean = true, // Bypass if important notification
    
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
    val timestamp: Long,
    val eventType: EventType, // OPENED, CLOSED, COOLDOWN_TRIGGERED, COOLDOWN_BYPASSED
    val sessionDuration: Long? = null // milliseconds
)

enum class EventType {
    OPENED,
    CLOSED,
    COOLDOWN_TRIGGERED,
    COOLDOWN_BYPASSED
}
```

### Cooldown Engine
```kotlin
class SmartAppCooldownEngine(private val context: Context) {
    
    private val repository = AppCooldownRepository(context)
    private val activeCooldowns = mutableMapOf<String, CooldownSession>()
    private var lastForegroundApp: String? = null
    private var lastForegroundTime: Long = 0
    
    data class CooldownSession(
        val packageName: String,
        val startTime: Long,
        val endTime: Long,
        val reason: String
    )
    
    suspend fun onAppOpened(packageName: String) {
        // Check if this app has an active cooldown
        val cooldownSession = activeCooldowns[packageName]
        if (cooldownSession != null && System.currentTimeMillis() < cooldownSession.endTime) {
            // Block the app from opening
            handleCooldownViolation(packageName, cooldownSession)
            return
        }
        
        // Remove expired cooldown
        activeCooldowns.remove(packageName)
        
        // Log event
        repository.logUsageEvent(
            packageName = packageName,
            eventType = EventType.OPENED
        )
        
        lastForegroundApp = packageName
        lastForegroundTime = System.currentTimeMillis()
    }
    
    suspend fun onAppClosed(packageName: String) {
        val rule = repository.getCooldownRule(packageName) ?: return
        if (!rule.enabled) return
        
        val sessionDuration = System.currentTimeMillis() - lastForegroundTime
        
        // Log close event
        repository.logUsageEvent(
            packageName = packageName,
            eventType = EventType.CLOSED,
            sessionDuration = sessionDuration
        )
        
        // Check if this app should trigger cooldown
        val recentCloses = repository.getRecentCloseEvents(
            packageName = packageName,
            windowMinutes = rule.triggerWindowMinutes
        )
        
        if (recentCloses.size >= rule.triggerThreshold) {
            // Trigger cooldown!
            startCooldown(rule)
        }
    }
    
    private suspend fun startCooldown(rule: AppCooldownRule) {
        val now = System.currentTimeMillis()
        val endTime = now + (rule.cooldownSeconds * 1000L)
        
        val session = CooldownSession(
            packageName = rule.packageName,
            startTime = now,
            endTime = endTime,
            reason = "Opened too frequently"
        )
        
        activeCooldowns[rule.packageName] = session
        
        // Update rule statistics
        repository.incrementCooldownTriggered(rule.id)
        
        // Show notification
        if (rule.showTimer) {
            showCooldownNotification(rule, session)
        }
        
        // Log to action history
        ActionLogger.log(
            ActionType.APP_COOLDOWN_TRIGGERED,
            "App Cooldown",
            "Cooldown Active",
            "${rule.appName} is on cooldown for ${rule.cooldownSeconds}s",
            targetApp = rule.packageName,
            triggerSource = TriggerSource.COOLDOWN
        )
    }
    
    private suspend fun handleCooldownViolation(packageName: String, session: CooldownSession) {
        val rule = repository.getCooldownRule(packageName) ?: return
        
        val remainingSeconds = ((session.endTime - System.currentTimeMillis()) / 1000).toInt()
        
        if (rule.requireConfirmation) {
            // Show confirmation dialog
            val bypassed = showCooldownConfirmationDialog(rule, remainingSeconds)
            
            if (bypassed) {
                activeCooldowns.remove(packageName)
                repository.incrementCooldownBypassed(rule.id)
                
                repository.logUsageEvent(
                    packageName = packageName,
                    eventType = EventType.COOLDOWN_BYPASSED
                )
                
                ActionLogger.log(
                    ActionType.APP_COOLDOWN_TRIGGERED,
                    "App Cooldown",
                    "Cooldown Bypassed",
                    "User bypassed cooldown for ${rule.appName}",
                    targetApp = packageName,
                    triggerSource = TriggerSource.USER_MANUAL
                )
            } else {
                // User chose to wait - exit the app
                exitApp(packageName)
            }
        } else {
            // Just block and exit
            showToast("${rule.appName} is on cooldown. Wait $remainingSeconds seconds.")
            exitApp(packageName)
        }
    }
    
    private fun exitApp(packageName: String) {
        // Send HOME intent to close the app
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        
        // Or use Accessibility Service to press back button
        // AccessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    private suspend fun showCooldownConfirmationDialog(
        rule: AppCooldownRule,
        remainingSeconds: Int
    ): Boolean {
        return suspendCoroutine { continuation ->
            val dialog = AlertDialog.Builder(context, R.style.Theme_Patchwork_Dialog)
                .setTitle("Take a Break from ${rule.appName}")
                .setMessage(
                    "You've been opening this app frequently. " +
                    "Cooldown ends in $remainingSeconds seconds.\n\n" +
                    "Do you really need to open it now?"
                )
                .setPositiveButton("I Really Need It") { _, _ ->
                    continuation.resume(true) // Bypass
                }
                .setNegativeButton("I'll Wait") { _, _ ->
                    continuation.resume(false) // Don't bypass
                }
                .setNeutralButton("Open Settings") { _, _ ->
                    openCooldownSettings(rule)
                    continuation.resume(false)
                }
                .setCancelable(false)
                .create()
            
            // Make dialog system alert (show over other apps)
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            dialog.show()
        }
    }
    
    private fun showCooldownNotification(rule: AppCooldownRule, session: CooldownSession) {
        val remainingMs = session.endTime - System.currentTimeMillis()
        val remainingSeconds = (remainingMs / 1000).toInt()
        
        val notification = NotificationCompat.Builder(context, COOLDOWN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cooldown)
            .setContentTitle("${rule.appName} on Cooldown")
            .setContentText("Available in $remainingSeconds seconds")
            .setProgress(rule.cooldownSeconds, remainingSeconds, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(rule.packageName.hashCode(), notification)
        
        // Schedule updates every second
        scheduleNotificationUpdates(rule, session)
    }
}
```

### Accessibility Service Integration
```kotlin
// In AccessibilityService

private val cooldownEngine = SmartAppCooldownEngine(this)

override fun onAccessibilityEvent(event: AccessibilityEvent) {
    when (event.eventType) {
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
            val packageName = event.packageName?.toString() ?: return
            
            if (packageName != lastPackage) {
                lifecycleScope.launch {
                    // App closed
                    lastPackage?.let { cooldownEngine.onAppClosed(it) }
                    
                    // App opened
                    cooldownEngine.onAppOpened(packageName)
                    
                    lastPackage = packageName
                }
            }
        }
    }
}
```

## 🎨 UI Design

### Cooldown Rules Screen
```kotlin
@Composable
fun SmartAppCooldownScreen(
    viewModel: SmartAppCooldownViewModel
) {
    val rules by viewModel.rules.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart App Cooldown") },
                subtitle = { Text("Break compulsive app habits") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAppPicker() }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                StatisticsCard(statistics)
            }
            
            item {
                InfoCard(
                    title = "How It Works",
                    description = "Set a mandatory delay before reopening apps you just closed. " +
                                 "Perfect for breaking the Instagram/Twitter loop."
                )
            }
            
            if (rules.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Timer,
                        title = "No Cooldown Rules",
                        description = "Add apps you want to prevent from compulsive reopening"
                    )
                }
            } else {
                items(rules) { rule ->
                    CooldownRuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleRule(it) },
                        onEdit = { viewModel.editRule(it) },
                        onDelete = { viewModel.deleteRule(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun CooldownRuleCard(
    rule: AppCooldownRule,
    onToggle: (AppCooldownRule) -> Unit,
    onEdit: (AppCooldownRule) -> Unit,
    onDelete: (AppCooldownRule) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(packageName = rule.packageName, size = 40.dp)
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.appName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Cooldown: ${rule.cooldownSeconds}s after ${rule.triggerThreshold} closes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(checked = rule.enabled, onCheckedChange = { onToggle(rule) })
            }
            
            if (rule.timesStopped > 0) {
                Spacer(Modifier.height(8.dp))
                Row {
                    StatChip(
                        icon = Icons.Default.Block,
                        label = "Stopped ${rule.timesStopped}×"
                    )
                    Spacer(Modifier.width(8.dp))
                    if (rule.timesBypassed > 0) {
                        StatChip(
                            icon = Icons.Default.Warning,
                            label = "Bypassed ${rule.timesBypassed}×"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(statistics: CooldownStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Week", style = MaterialTheme.typography.titleMedium)
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    icon = Icons.Default.Block,
                    value = statistics.totalStops.toString(),
                    label = "Apps Stopped"
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    value = "${statistics.totalSecondsDelayed / 60}m",
                    label = "Time Saved"
                )
                StatItem(
                    icon = Icons.Default.TrendingDown,
                    value = "${statistics.reductionPercent}%",
                    label = "Reduction"
                )
            }
        }
    }
}
```

## ✅ Testing Requirements
- [ ] Cooldown triggers after threshold exceeded
- [ ] Confirmation dialog shows correctly
- [ ] Bypass works when confirmed
- [ ] App exits correctly when cooldown active
- [ ] Notification updates correctly
- [ ] Cooldown expires correctly
- [ ] Multiple apps can have cooldowns simultaneously
- [ ] Statistics track correctly
- [ ] No false positives (system apps, Patchwork itself)

---

# 5. Idle App Auto-Action Engine

## 📋 Overview
Automatically performs actions on apps that haven't been used in X days (freeze, clear cache, revoke permissions, etc.). Keeps device clean and performant without manual intervention.

## 🎯 User Benefits
- **Automatic Cleanup**: No more manually managing unused apps
- **Battery Savings**: Freeze apps running in background
- **Storage Savings**: Auto-clear caches of unused apps
- **Privacy**: Revoke permissions from apps you're not using
- **Performance**: Reduce background processes automatically

## 🛠️ Technical Implementation

### Data Model
```kotlin
@Entity(tableName = "idle_app_rules")
data class IdleAppRule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String, // "Freeze social media after 7 days"
    val enabled: Boolean = true,
    
    // Trigger Conditions
    val idleDays: Int = 7, // Days without usage
    val excludeSystemApps: Boolean = true,
    val excludeRecentlyInstalled: Boolean = true, // Don't act on apps < 7 days old
    val minInstallDays: Int = 7,
    
    // Target Selection
    val targetScope: TargetScope = TargetScope.ALL_APPS,
    val specificPackages: List<String> = emptyList(), // If targetScope = SPECIFIC
    val categoryFilter: List<AppCategory> = emptyList(), // SOCIAL, GAMES, etc.
    
    // Actions to Perform
    val actionFreeze: Boolean = false,
    val actionClearCache: Boolean = false,
    val actionRevokePermissions: List<String> = emptyList(), // Which permissions
    val actionDisableNotifications: Boolean = false,
    val actionUninstall: Boolean = false, // Requires user confirmation
    
    // Behavior
    val requireConfirmation: Boolean = true,
    val showNotificationBefore: Boolean = true, // Warn 24h before action
    val allowWhitelist: Boolean = true,
    
    // Statistics
    val totalAppsProcessed: Int = 0,
    val lastRunTime: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis()
)

enum class TargetScope {
    ALL_APPS,
    SPECIFIC_APPS,
    CATEGORY
}

enum class AppCategory {
    SOCIAL,
    GAMES,
    PRODUCTIVITY,
    SHOPPING,
    ENTERTAINMENT,
    TOOLS,
    OTHER
}

@Entity(tableName = "idle_app_actions_log")
data class IdleAppActionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val ruleId: String,
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    
    val idleDays: Int,
    val actionsTaken: List<String>, // ["frozen", "cache_cleared", "location_revoked"]
    val success: Boolean,
    val errorMessage: String? = null
)
```

### Idle Detection Engine
```kotlin
class IdleAppAutoActionEngine(private val context: Context) {
    
    private val repository = IdleAppRepository(context)
    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
    private val packageManager = context.packageManager
    
    suspend fun runIdleDetection() {
        val rules = repository.getEnabledRules()
        
        rules.forEach { rule ->
            processRule(rule)
        }
    }
    
    private suspend fun processRule(rule: IdleAppRule) {
        val idleApps = findIdleApps(rule)
        
        if (idleApps.isEmpty()) {
            Log.d("IdleEngine", "No idle apps found for rule: ${rule.name}")
            return
        }
        
        // Show notification before taking action
        if (rule.showNotificationBefore) {
            showPreActionNotification(rule, idleApps)
            delay(24 * 60 * 60 * 1000L) // Wait 24 hours
        }
        
        // Show confirmation if required
        if (rule.requireConfirmation) {
            val confirmed = showConfirmationDialog(rule, idleApps)
            if (!confirmed) return
        }
        
        // Execute actions
        idleApps.forEach { appInfo ->
            executeActionsOnApp(rule, appInfo)
        }
        
        repository.updateRuleStats(rule.id, idleApps.size)
    }
    
    private fun findIdleApps(rule: IdleAppRule): List<AppInfo> {
        val now = System.currentTimeMillis()
        val idleThresholdMs = rule.idleDays * 24 * 60 * 60 * 1000L
        val cutoffTime = now - idleThresholdMs
        
        // Get usage stats
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cutoffTime,
            now
        )
        
        val usedPackages = usageStats
            .filter { it.lastTimeUsed > cutoffTime }
            .map { it.packageName }
            .toSet()
        
        // Get all installed apps
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return allApps.filter { appInfo ->
            val packageName = appInfo.packageName
            
            // Apply filters
            if (rule.excludeSystemApps && isSystemApp(appInfo)) return@filter false
            if (packageName in usedPackages) return@filter false
            if (rule.excludeRecentlyInstalled && isRecentlyInstalled(appInfo, rule.minInstallDays)) return@filter false
            if (isWhitelisted(packageName)) return@filter false
            
            // Apply target scope
            when (rule.targetScope) {
                TargetScope.ALL_APPS -> true
                TargetScope.SPECIFIC_APPS -> packageName in rule.specificPackages
                TargetScope.CATEGORY -> {
                    val category = getAppCategory(packageName)
                    category in rule.categoryFilter
                }
            }
        }.map { appInfo ->
            AppInfo(
                packageName = appInfo.packageName,
                appName = getAppName(appInfo),
                idleDays = getIdleDays(appInfo.packageName),
                category = getAppCategory(appInfo.packageName)
            )
        }
    }
    
    private suspend fun executeActionsOnApp(rule: IdleAppRule, appInfo: AppInfo) {
        val actionsTaken = mutableListOf<String>()
        var success = true
        var errorMessage: String? = null
        
        try {
            // Freeze app
            if (rule.actionFreeze) {
                val frozen = ShellUtils.execute("pm disable ${appInfo.packageName}")
                if (frozen) actionsTaken.add("frozen")
            }
            
            // Clear cache
            if (rule.actionClearCache) {
                val cleared = clearAppCache(appInfo.packageName)
                if (cleared) actionsTaken.add("cache_cleared")
            }
            
            // Revoke permissions
            if (rule.actionRevokePermissions.isNotEmpty()) {
                rule.actionRevokePermissions.forEach { permission ->
                    val revoked = ShellUtils.execute("pm revoke ${appInfo.packageName} $permission")
                    if (revoked) actionsTaken.add("${permission}_revoked")
                }
            }
            
            // Disable notifications
            if (rule.actionDisableNotifications) {
                val disabled = disableNotifications(appInfo.packageName)
                if (disabled) actionsTaken.add("notifications_disabled")
            }
            
            // Uninstall (requires user confirmation)
            if (rule.actionUninstall) {
                showUninstallDialog(appInfo)
                actionsTaken.add("uninstall_prompted")
            }
            
        } catch (e: Exception) {
            success = false
            errorMessage = e.message
            Log.e("IdleEngine", "Error processing ${appInfo.packageName}", e)
        }
        
        // Log action
        repository.logAction(
            ruleId = rule.id,
            packageName = appInfo.packageName,
            appName = appInfo.appName,
            idleDays = appInfo.idleDays,
            actionsTaken = actionsTaken,
            success = success,
            errorMessage = errorMessage
        )
        
        // Log to action history
        if (actionsTaken.isNotEmpty()) {
            ActionLogger.log(
                ActionType.APP_FROZEN, // or appropriate type
                "Idle App Engine",
                "Idle App Processed",
                "Processed ${appInfo.appName} (idle for ${appInfo.idleDays} days): ${actionsTaken.joinToString(", ")}",
                targetApp = appInfo.packageName,
                triggerSource = TriggerSource.IDLE_DETECTION
            )
        }
    }
    
    private fun getIdleDays(packageName: String): Int {
        val now = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - (90 * 24 * 60 * 60 * 1000L), // Last 90 days
            now
        )
        
        val appUsage = usageStats.find { it.packageName == packageName }
        return if (appUsage != null) {
            val daysSinceUsed = (now - appUsage.lastTimeUsed) / (24 * 60 * 60 * 1000)
            daysSinceUsed.toInt()
        } else {
            90 // Default to 90+ days if no usage found
        }
    }
    
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
    
    private fun isRecentlyInstalled(appInfo: ApplicationInfo, minDays: Int): Boolean {
        val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val installTime = packageInfo.firstInstallTime
        val daysSinceInstall = (System.currentTimeMillis() - installTime) / (24 * 60 * 60 * 1000)
        return daysSinceInstall < minDays
    }
}
```

### Scheduled Worker
```kotlin
class IdleAppDetectionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val engine = IdleAppAutoActionEngine(applicationContext)
            engine.runIdleDetection()
            Result.success()
        } catch (e: Exception) {
            Log.e("IdleWorker", "Error running idle detection", e)
            Result.retry()
        }
    }
}

// Schedule daily check
fun scheduleIdleDetection(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<IdleAppDetectionWorker>(
        repeatInterval = 1,
        repeatIntervalTimeUnit = TimeUnit.DAYS
    )
        .setInitialDelay(1, TimeUnit.HOURS)
        .setConstraints(
            Constraints.Builder()
                .setRequiresCharging(true) // Only when charging
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "idle_app_detection",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
```

## 🎨 UI Design

```kotlin
@Composable
fun IdleAppEngineScreen(
    viewModel: IdleAppEngineViewModel
) {
    val rules by viewModel.rules.collectAsState()
    val recentActions by viewModel.recentActions.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Idle App Auto-Actions") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createNewRule() }) {
                Icon(Icons.Default.Add, "Create Rule")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                InfoCard(
                    title = "Automatic App Cleanup",
                    description = "Define rules to automatically manage apps you haven't used in a while. " +
                                 "Freeze, clear cache, or revoke permissions automatically."
                )
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${recentActions.totalAppsProcessed} apps processed this week",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            items(rules) { rule ->
                IdleRuleCard(
                    rule = rule,
                    onToggle = { viewModel.toggleRule(it) },
                    onEdit = { viewModel.editRule(it) },
                    onRunNow = { viewModel.runRuleNow(it) }
                )
            }
        }
    }
}
```

## ✅ Testing Requirements
- [ ] Correctly identifies idle apps
- [ ] Respects exclusions (system apps, recently installed)
- [ ] Actions execute correctly
- [ ] Confirmation dialogs work
- [ ] Whitelist functionality works
- [ ] Scheduled worker runs reliably
- [ ] Permissions handled correctly
- [ ] No impact on actively used apps

---

# 6. Permission Abuse Notifier

## 📋 Overview
Monitors apps for excessive permission usage (mic always on, location tracking, clipboard snooping) and alerts the user. Raises awareness about privacy-invasive behaviors.

## 🎯 User Benefits
- **Privacy Protection**: Know when apps are spying on you
- **Awareness**: See which apps abuse permissions
- **Action**: Quick access to revoke permissions
- **Peace of Mind**: Continuous monitoring in background

## 🛠️ Technical Implementation

### Monitoring System
```kotlin
class PermissionAbuseMonitor(private val context: Context) {
    
    private val appOpsManager = context.getSystemService(AppOpsManager::class.java)
    private val clipboardManager = context.getSystemService(ClipboardManager::class.java)
    private val locationManager = context.getSystemService(LocationManager::class.java)
    
    private val thresholds = PermissionThresholds(
        micAccessesPerHour = 10,
        locationAccessesPerHour = 20,
        clipboardAccessesPerHour = 15,
        cameraAccessesPerHour = 5,
        continuousMicMinutes = 5, // Alert if mic used continuously for 5 min
        continuousLocationMinutes = 30
    )
    
    suspend fun monitorPermissionUsage() {
        monitorMicrophoneUsage()
        monitorLocationUsage()
        monitorClipboardUsage()
        monitorCameraUsage()
    }
    
    private suspend fun monitorMicrophoneUsage() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        
        val micUsage = getPermissionUsageStats(
            AppOpsManager.OPSTR_RECORD_AUDIO,
            oneHourAgo,
            now
        )
        
        micUsage.forEach { (packageName, accesses) ->
            if (accesses >= thresholds.micAccessesPerHour) {
                showAbuseNotification(
                    packageName = packageName,
                    permission = "Microphone",
                    message = "Accessed $accesses times in the last hour"
                )
            }
        }
    }
    
    private fun getPermissionUsageStats(
        opString: String,
        startTime: Long,
        endTime: Long
    ): Map<String, Int> {
        val usageMap = mutableMapOf<String, Int>()
        
        try {
            val usageStats = appOpsManager.getPackagesForOps(arrayOf(opString))
            
            usageStats?.forEach { ops ->
                val packageName = ops.packageName
                var count = 0
                
                ops.ops.forEach { op ->
                    if (op.lastAccessTime in startTime..endTime) {
                        count++
                    }
                }
                
                if (count > 0) {
                    usageMap[packageName] = count
                }
            }
        } catch (e: Exception) {
            Log.e("PermissionMonitor", "Error getting permission stats", e)
        }
        
        return usageMap
    }
    
    private fun showAbuseNotification(
        packageName: String,
        permission: String,
        message: String
    ) {
        val appName = getAppName(packageName)
        
        val intent = Intent(context, PermissionDetailsActivity::class.java).apply {
            putExtra("packageName", packageName)
            putExtra("permission", permission)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, PERMISSION_ABUSE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("$permission Abuse Detected")
            .setContentText("$appName: $message")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$appName has accessed $permission excessively. $message. Tap to review permissions.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_block,
                "Revoke",
                getRevokePermissionIntent(packageName, permission)
            )
            .addAction(
                R.drawable.ic_settings,
                "Settings",
                getAppSettingsIntent(packageName)
            )
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(packageName.hashCode(), notification)
        
        // Log to action history
        GlobalScope.launch {
            ActionLogger.log(
                ActionType.PERMISSION_GRANTED, // or new PERMISSION_ABUSE type
                "Privacy",
                "Permission Abuse Detected",
                "$appName: $permission - $message",
                targetApp = packageName,
                triggerSource = TriggerSource.SYSTEM_EVENT
            )
        }
    }
}

data class PermissionThresholds(
    val micAccessesPerHour: Int,
    val locationAccessesPerHour: Int,
    val clipboardAccessesPerHour: Int,
    val cameraAccessesPerHour: Int,
    val continuousMicMinutes: Int,
    val continuousLocationMinutes: Int
)
```

### Worker for Continuous Monitoring
```kotlin
class PermissionAbuseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val monitor = PermissionAbuseMonitor(applicationContext)
            monitor.monitorPermissionUsage()
            Result.success()
        } catch (e: Exception) {
            Log.e("PermissionWorker", "Error monitoring permissions", e)
            Result.retry()
        }
    }
}

// Schedule hourly monitoring
fun schedulePermissionMonitoring(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<PermissionAbuseWorker>(
        repeatInterval = 1,
        repeatIntervalTimeUnit = TimeUnit.HOURS
    ).build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "permission_abuse_monitoring",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
```

## ✅ Testing Requirements
- [ ] Correctly detects excessive permission usage
- [ ] Notifications show at correct thresholds
- [ ] Revoke action works correctly
- [ ] No false positives
- [ ] Battery impact minimal
- [ ] Works across Android versions

---

# 7. Foreground Service Watchdog

## 📋 Overview
Monitors long-running foreground services and alerts the user about apps that keep services running unnecessarily. Helps identify battery-draining apps.

## 🛠️ Technical Implementation

```kotlin
class ForegroundServiceWatchdog(private val context: Context) {
    
    private val activityManager = context.getSystemService(ActivityManager::class.java)
    private val runningServices = mutableMapOf<String, ServiceInfo>()
    
    data class ServiceInfo(
        val packageName: String,
        val serviceName: String,
        val startTime: Long,
        val notificationTitle: String?
    )
    
    suspend fun monitorForegroundServices() {
        val currentServices = activityManager.getRunningServices(Int.MAX_VALUE)
        
        currentServices.forEach { service ->
            if (service.foreground) {
                val key = "${service.service.packageName}:${service.service.className}"
                
                if (key !in runningServices) {
                    // New foreground service started
                    runningServices[key] = ServiceInfo(
                        packageName = service.service.packageName,
                        serviceName = service.service.className,
                        startTime = System.currentTimeMillis(),
                        notificationTitle = getNotificationTitle(service)
                    )
                } else {
                    // Check if service has been running too long
                    val serviceInfo = runningServices[key]!!
                    val runningMinutes = (System.currentTimeMillis() - serviceInfo.startTime) / (60 * 1000)
                    
                    if (runningMinutes >= 30) { // Alert after 30 minutes
                        showLongRunningServiceAlert(serviceInfo, runningMinutes.toInt())
                    }
                }
            }
        }
        
        // Remove services that stopped
        val activeKeys = currentServices.filter { it.foreground }
            .map { "${it.service.packageName}:${it.service.className}" }
        runningServices.keys.retainAll(activeKeys.toSet())
    }
    
    private fun showLongRunningServiceAlert(serviceInfo: ServiceInfo, minutes: Int) {
        val appName = getAppName(serviceInfo.packageName)
        
        val notification = NotificationCompat.Builder(context, SERVICE_WATCHDOG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Long-Running Service Detected")
            .setContentText("$appName has been running for $minutes minutes")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                R.drawable.ic_stop,
                "Stop Service",
                getStopServiceIntent(serviceInfo.packageName)
            )
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(serviceInfo.packageName.hashCode(), notification)
    }
}
```

---

# 8. Implementation Roadmap

## Phase 1: Foundation (Week 1)
**Goal: Trust & Visibility**

1. ✅ **Action History Timeline** (3 days)
   - Database schema
   - Logger utility
   - Basic UI
   - Integration points in existing features

2. ✅ **System State Snapshots** (4 days)
   - Data model
   - Capture/restore logic
   - UI for creating/managing snapshots
   - Quick Settings tile

## Phase 2: Core Behavior Control (Week 2-3)
**Goal: Deliver Signature Feature**

3. ✅ **App Behavior Controller** (7 days)
   - Data model
   - Behavior engine
   - Accessibility Service integration
   - Rule editor UI
   - Testing across multiple apps

## Phase 3: Habit & Automation (Week 4)
**Goal: User Empowerment**

4. ✅ **Smart App Cooldown** (3 days)
   - Cooldown engine
   - Confirmation dialogs
   - Statistics tracking

5. ✅ **Idle App Auto-Action Engine** (4 days)
   - Idle detection logic
   - Action executor
   - Scheduled worker
   - Rule configuration UI

## Phase 4: Privacy & Monitoring (Week 5)
**Goal: User Awareness & Protection**

6. ✅ **Permission Abuse Notifier** (3 days)
   - Permission monitoring
   - Threshold detection
   - Alert notifications

7. ✅ **Foreground Service Watchdog** (2 days)
   - Service monitoring
   - Long-running detection
   - User alerts

---

# Required Permissions Summary

```xml
<!-- Action History - None additional -->

<!-- System State Snapshots -->
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />

<!-- App Behavior Controller -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Smart App Cooldown -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Idle App Engine -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

<!-- Permission Abuse Notifier -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />

<!-- Foreground Service Watchdog -->
<uses-permission android:name="android.permission.GET_TASKS" />
```

---

# Universal Compatibility Notes

## All Features Compatible With:
- ✅ AOSP/Pixel devices
- ✅ Samsung One UI
- ✅ Xiaomi MIUI/HyperOS
- ✅ OnePlus OxygenOS
- ✅ Realme RealmeUI
- ✅ Motorola
- ✅ Nothing OS
- ✅ Any Android 8.0+ device with Shizuku

## Known Limitations:
- **App Behavior Controller - Screenshot Blocking**: Overlay method may not work on all launchers; effectiveness varies
- **System State Snapshots - Mobile Data Toggle**: Requires Shizuku on Android 9+
- **Idle App Engine - App Freezing**: Requires Shizuku or root on Android 8+

---

**End of Implementation Guide**

This guide provides comprehensive technical specifications for all 8 core features. Ready for development! 🚀

