package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.ActionHistoryEntry
import com.brittytino.patchwork.data.database.entity.ActionType
import kotlinx.coroutines.flow.Flow

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
