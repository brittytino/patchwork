package com.brittytino.patchwork.data.repository

import com.brittytino.patchwork.data.database.dao.ActionHistoryDao
import com.brittytino.patchwork.data.database.entity.ActionHistoryEntry
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.database.entity.TriggerSource
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ActionHistoryRepository(private val dao: ActionHistoryDao) {
    
    fun getRecentActions(limit: Int = 100): Flow<List<ActionHistoryEntry>> = 
        dao.getRecentActions(limit)
    
    fun getActionsSince(startTime: Long): Flow<List<ActionHistoryEntry>> = 
        dao.getActionsSince(startTime)
    
    fun getActionsByCategory(category: String): Flow<List<ActionHistoryEntry>> = 
        dao.getActionsByCategory(category)
    
    fun getActionsByApp(packageName: String): Flow<List<ActionHistoryEntry>> = 
        dao.getActionsByApp(packageName)
    
    fun getActionsByType(type: ActionType): Flow<List<ActionHistoryEntry>> = 
        dao.getActionsByType(type)
    
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
    
    suspend fun clearAll() {
        dao.clearAll()
    }
    
    suspend fun getCount(): Int = dao.getCount()
}
