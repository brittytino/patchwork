package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.IdleAppActionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface IdleAppActionLogDao {
    @Query("SELECT * FROM idle_app_actions_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<IdleAppActionLog>>
    
    @Query("SELECT * FROM idle_app_actions_log WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getLogsForApp(packageName: String): Flow<List<IdleAppActionLog>>
    
    @Insert
    suspend fun insertLog(log: IdleAppActionLog)
    
    @Query("DELETE FROM idle_app_actions_log WHERE timestamp < :beforeTimestamp")
    suspend fun deleteLogsBefore(beforeTimestamp: Long)
}
