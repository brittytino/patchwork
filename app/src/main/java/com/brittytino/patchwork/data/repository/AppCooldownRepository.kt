package com.brittytino.patchwork.data.repository

import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.AppCooldownRule
import com.brittytino.patchwork.data.database.entity.AppUsageEvent
import kotlinx.coroutines.flow.Flow

class AppCooldownRepository(private val database: AppDatabase) {
    
    private val cooldownDao = database.appCooldownRuleDao()
    private val usageDao = database.appUsageEventDao()
    
    // ==================== Cooldown Rules ====================
    
    fun getAllRules(): Flow<List<AppCooldownRule>> {
        return cooldownDao.getAllRules()
    }
    
    fun getEnabledRules(): Flow<List<AppCooldownRule>> {
        return cooldownDao.getEnabledRules()
    }
    
    suspend fun getRuleForApp(packageName: String): AppCooldownRule? {
        return cooldownDao.getRuleForApp(packageName)
    }
    
    suspend fun insertRule(rule: AppCooldownRule): Long {
        return cooldownDao.insertRule(rule)
    }
    
    suspend fun updateRule(rule: AppCooldownRule) {
        cooldownDao.updateRule(rule)
    }
    
    suspend fun deleteRule(rule: AppCooldownRule) {
        cooldownDao.deleteRule(rule)
    }
    
    // ==================== Usage Events ====================
    
    suspend fun recordUsageEvent(event: AppUsageEvent) {
        usageDao.insertEvent(event)
    }
    
    suspend fun getRecentUsageForApp(packageName: String, sinceTimestamp: Long): List<AppUsageEvent> {
        return usageDao.getEventsForAppSince(packageName, sinceTimestamp)
    }
    
    suspend fun getLastUsageForApp(packageName: String): AppUsageEvent? {
        val events = usageDao.getLastEventForApp(packageName)
        return events.firstOrNull()
    }
    
    fun getUsageEventsSince(sinceTimestamp: Long): Flow<List<AppUsageEvent>> {
        return usageDao.getEventsSince(sinceTimestamp)
    }
    
    suspend fun cleanupOldEvents(beforeTimestamp: Long) {
        usageDao.deleteEventsBefore(beforeTimestamp)
    }
    
    // ==================== Statistics ====================
    
    suspend fun getAppOpenCountInWindow(packageName: String, windowStart: Long, windowEnd: Long): Int {
        return usageDao.countEventsInWindow(packageName, windowStart, windowEnd)
    }
    
    suspend fun getTotalScreenTime(packageName: String, sinceTimestamp: Long): Long {
        val events = usageDao.getEventsForAppSince(packageName, sinceTimestamp)
        return events.sumOf { it.durationMs ?: 0 }
    }
    
    // ==================== Cooldown Logic Helpers ====================
    
    suspend fun isAppInCooldown(packageName: String): Boolean {
        val rule = getRuleForApp(packageName) ?: return false
        if (!rule.enabled) return false
        
        val lastEvent = getLastUsageForApp(packageName) ?: return false
        val timeSinceLastUse = System.currentTimeMillis() - lastEvent.timestamp
        
        return timeSinceLastUse < rule.cooldownPeriodMinutes * 60 * 1000
    }
    
    suspend fun getRemainingCooldownMs(packageName: String): Long {
        val rule = getRuleForApp(packageName) ?: return 0L
        if (!rule.enabled) return 0L
        
        val lastEvent = getLastUsageForApp(packageName) ?: return 0L
        val cooldownMs = rule.cooldownPeriodMinutes * 60 * 1000L
        val timeSinceLastUse = System.currentTimeMillis() - lastEvent.timestamp
        
        return (cooldownMs - timeSinceLastUse).coerceAtLeast(0L)
    }
    
    suspend fun shouldBlockAppLaunch(packageName: String): Pair<Boolean, String?> {
        val rule = getRuleForApp(packageName) ?: return Pair(false, null)
        if (!rule.enabled) return Pair(false, null)
        
        val now = System.currentTimeMillis()
        val lastEvent = getLastUsageForApp(packageName)
        
        // Check cooldown period
        if (lastEvent != null) {
            val timeSinceLastUse = now - lastEvent.timestamp
            val cooldownMs = rule.cooldownPeriodMinutes * 60 * 1000L
            
            if (timeSinceLastUse < cooldownMs) {
                val remainingMinutes = (cooldownMs - timeSinceLastUse) / 60000
                return Pair(true, "Cooldown active. Wait ${remainingMinutes}m")
            }
        }
        
        // Check daily limit
        if (rule.maxDailyOpens != null) {
            val dayStart = now - (now % 86400000) // Start of today
            val todayCount = getAppOpenCountInWindow(packageName, dayStart, now)
            
            if (todayCount >= rule.maxDailyOpens) {
                return Pair(true, "Daily limit reached (${rule.maxDailyOpens} opens)")
            }
        }
        
        // Check hourly limit
        if (rule.maxHourlyOpens != null) {
            val hourStart = now - 3600000 // Last hour
            val hourlyCount = getAppOpenCountInWindow(packageName, hourStart, now)
            
            if (hourlyCount >= rule.maxHourlyOpens) {
                return Pair(true, "Hourly limit reached (${rule.maxHourlyOpens} opens)")
            }
        }
        
        return Pair(false, null)
    }
}
