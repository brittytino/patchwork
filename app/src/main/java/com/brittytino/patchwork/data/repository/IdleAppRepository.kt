package com.brittytino.patchwork.data.repository

import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.IdleAppRule
import com.brittytino.patchwork.data.database.entity.IdleAppActionLog
import kotlinx.coroutines.flow.Flow

class IdleAppRepository(private val database: AppDatabase) {
    
    private val ruleDao = database.idleAppRuleDao()
    private val logDao = database.idleAppActionLogDao()
    
    // ==================== Rules ====================
    
    fun getAllRules(): Flow<List<IdleAppRule>> {
        return ruleDao.getAllRules()
    }
    
    fun getEnabledRules(): Flow<List<IdleAppRule>> {
        return ruleDao.getEnabledRules()
    }
    
    suspend fun getRuleForApp(packageName: String): IdleAppRule? {
        return ruleDao.getRuleForApp(packageName)
    }
    
    suspend fun insertRule(rule: IdleAppRule): Long {
        return ruleDao.insertRule(rule)
    }
    
    suspend fun updateRule(rule: IdleAppRule) {
        ruleDao.updateRule(rule)
    }
    
    suspend fun deleteRule(rule: IdleAppRule) {
        ruleDao.deleteRule(rule)
    }
    
    suspend fun updateLastChecked(packageName: String, timestamp: Long) {
        val rule = getRuleForApp(packageName) ?: return
        ruleDao.updateRule(rule.copy(lastCheckedAt = timestamp))
    }
    
    suspend fun incrementActionCount(packageName: String) {
        val rule = getRuleForApp(packageName) ?: return
        ruleDao.updateRule(rule.copy(actionCount = rule.actionCount + 1))
    }
    
    // ==================== Action Logs ====================
    
    suspend fun logAction(log: IdleAppActionLog) {
        logDao.insertLog(log)
    }
    
    fun getLogsForApp(packageName: String): Flow<List<IdleAppActionLog>> {
        return logDao.getLogsForApp(packageName)
    }
    
    fun getRecentLogs(limit: Int = 50): Flow<List<IdleAppActionLog>> {
        return logDao.getRecentLogs(limit)
    }
    
    suspend fun cleanupOldLogs(beforeTimestamp: Long) {
        logDao.deleteLogsBefore(beforeTimestamp)
    }
    
    // ==================== Statistics ====================
    
    suspend fun getTotalActionsToday(): Int {
        // TODO: Add proper query with timestamp filter
        return 0
    }
    
    suspend fun getLastActionForApp(packageName: String): IdleAppActionLog? {
        // TODO: Add proper query to get most recent log
        return null
    }
}
