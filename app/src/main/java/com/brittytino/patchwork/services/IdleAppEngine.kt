package com.brittytino.patchwork.services

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.IdleAppActionLog
import com.brittytino.patchwork.data.database.entity.IdleAppRule
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.database.entity.TriggerSource
import com.brittytino.patchwork.data.repository.IdleAppRepository
import com.brittytino.patchwork.utils.ActionLogger
import com.brittytino.patchwork.data.database.entity.ActionHistoryEntry
import com.brittytino.patchwork.utils.FreezeManager
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * Idle App Auto-Action Engine
 * Automatically freezes or kills apps that haven't been used for specified periods
 */
class IdleAppEngine private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "IdleAppEngine"
        private const val CHECK_INTERVAL_MS = 5 * 60 * 1000L // Check every 5 minutes
        
        @Volatile
        private var instance: IdleAppEngine? = null
        
        fun getInstance(context: Context): IdleAppEngine {
            return instance ?: synchronized(this) {
                instance ?: IdleAppEngine(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val repository = IdleAppRepository(AppDatabase.getInstance(context))
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    private var monitoringJob: Job? = null
    private var isRunning = false
    
    /**
     * Start monitoring idle apps
     */
    fun startMonitoring() {
        if (isRunning) {
            Log.d(TAG, "Monitoring already running")
            return
        }
        
        isRunning = true
        monitoringJob = scope.launch {
            while (isActive) {
                try {
                    checkIdleApps()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking idle apps", e)
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
        
        Log.d(TAG, "Started idle app monitoring")
    }
    
    /**
     * Stop monitoring idle apps
     */
    fun stopMonitoring() {
        isRunning = false
        monitoringJob?.cancel()
        monitoringJob = null
        Log.d(TAG, "Stopped idle app monitoring")
    }
    
    /**
     * Check all enabled rules and take actions on idle apps
     */
    private suspend fun checkIdleApps() {
        try {
            val rules = mutableListOf<IdleAppRule>()
            repository.getEnabledRules().collect { rulesList ->
                rules.clear()
                rules.addAll(rulesList)
            }
            
            if (rules.isEmpty()) {
                return
            }
            
            val now = System.currentTimeMillis()
            val endTime = now
            val startTime = now - TimeUnit.DAYS.toMillis(7) // Check last 7 days
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            rules.forEach { rule ->
                val lastUsedTime = usageStats
                    ?.firstOrNull { it.packageName == rule.packageName }
                    ?.lastTimeUsed
                    ?: 0L
                
                val idleTime = now - lastUsedTime
                val idleMinutes = idleTime / 60000
                
                // Check if app has been idle long enough
                if (idleMinutes >= rule.idleThresholdMinutes) {
                    executeAction(rule, idleMinutes)
                }
                
                // Update last checked timestamp
                repository.updateLastChecked(rule.packageName, now)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkIdleApps", e)
        }
    }
    
    /**
     * Execute the configured action on an idle app
     */
    private suspend fun executeAction(rule: IdleAppRule, idleMinutes: Long) {
        try {
            val action = rule.action
            val packageName = rule.packageName
            val appName = rule.appName
            
            var success = false
            var errorMessage: String? = null
            
            when (action) {
                IdleAppRule.Action.FREEZE -> {
                    success = FreezeManager.freezeApp(context, packageName)
                    if (success) {
                        Log.d(TAG, "Froze idle app: $appName (idle for ${idleMinutes}m)")
                    } else {
                        errorMessage = "Failed to freeze app"
                    }
                }
                
                IdleAppRule.Action.KILL -> {
                    success = killApp(packageName)
                    if (success) {
                        Log.d(TAG, "Killed idle app: $appName (idle for ${idleMinutes}m)")
                    } else {
                        errorMessage = "Failed to kill app"
                    }
                }
                
                IdleAppRule.Action.CLEAR_CACHE -> {
                    success = clearAppCache(packageName)
                    if (success) {
                        Log.d(TAG, "Cleared cache for idle app: $appName")
                    } else {
                        errorMessage = "Failed to clear cache"
                    }
                }
                
                IdleAppRule.Action.NOTIFY -> {
                    success = true
                    showIdleNotification(appName, idleMinutes)
                    Log.d(TAG, "Notified about idle app: $appName")
                }
            }
            
            // Log the action
            val log = IdleAppActionLog(
                packageName = packageName,
                appName = appName,
                action = action,
                timestamp = System.currentTimeMillis(),
                idleTimeMinutes = idleMinutes.toInt(),
                success = success,
                errorMessage = errorMessage
            )
            repository.logAction(log)
            
            // Update action count
            if (success) {
                repository.incrementActionCount(packageName)
            }
            
            // Log to action history
            ActionLogger.log(
                type = ActionType.IDLE_APP_ACTION,
                category = "Idle Apps",
                title = "Idle App Action",
                description = "$appName was ${action.name.lowercase()} after ${idleMinutes}m of inactivity",
                targetApp = packageName,
                triggerSource = TriggerSource.IDLE_APP_ENGINE,
                success = success,
                errorMessage = errorMessage
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action for ${rule.appName}", e)
        }
    }
    
    /**
     * Force stop an app
     */
    private fun killApp(packageName: String): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.killBackgroundProcesses(packageName)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to kill app: $packageName", e)
            false
        }
    }
    
    /**
     * Clear app cache (requires root/system permissions)
     */
    private fun clearAppCache(packageName: String): Boolean {
        return try {
            // Note: This requires system permissions or root
            // For now, return false as it's not easily achievable
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache: $packageName", e)
            false
        }
    }
    
    /**
     * Show notification about idle app
     */
    private fun showIdleNotification(appName: String, idleMinutes: Long) {
        // TODO: Implement notification showing
        // For now, just log it
        Log.d(TAG, "Would show notification: $appName idle for ${idleMinutes}m")
    }
    
    /**
     * Get idle statistics for an app
     */
    suspend fun getAppIdleStats(packageName: String): IdleStats? {
        return try {
            val now = System.currentTimeMillis()
            val startTime = now - TimeUnit.DAYS.toMillis(7)
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                now
            )
            
            val stat = usageStats?.firstOrNull { it.packageName == packageName }
            if (stat != null) {
                val idleTime = now - stat.lastTimeUsed
                val idleMinutes = idleTime / 60000
                
                IdleStats(
                    lastUsedTimestamp = stat.lastTimeUsed,
                    idleMinutes = idleMinutes,
                    totalTimeInForeground = stat.totalTimeInForeground
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting idle stats", e)
            null
        }
    }
    
    /**
     * Manually trigger check for a specific app
     */
    suspend fun checkAppNow(packageName: String) {
        val rule = repository.getRuleForApp(packageName)
        if (rule != null && rule.enabled) {
            val stats = getAppIdleStats(packageName)
            if (stats != null && stats.idleMinutes >= rule.idleThresholdMinutes) {
                executeAction(rule, stats.idleMinutes)
            }
        }
    }
    
    data class IdleStats(
        val lastUsedTimestamp: Long,
        val idleMinutes: Long,
        val totalTimeInForeground: Long
    )
}
