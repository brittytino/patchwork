package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.AppUsageEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageEventDao {
    @Query("SELECT * FROM app_usage_events WHERE packageName = :packageName AND timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getEventsForAppSince(packageName: String, sinceTimestamp: Long): List<AppUsageEvent>
    
    @Query("SELECT * FROM app_usage_events WHERE packageName = :packageName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEventForApp(packageName: String): List<AppUsageEvent>
    
    @Query("SELECT * FROM app_usage_events WHERE timestamp >= :sinceTimestamp")
    fun getEventsSince(sinceTimestamp: Long): Flow<List<AppUsageEvent>>
    
    @Query("SELECT COUNT(*) FROM app_usage_events WHERE packageName = :packageName AND timestamp BETWEEN :windowStart AND :windowEnd")
    suspend fun countEventsInWindow(packageName: String, windowStart: Long, windowEnd: Long): Int
    
    @Insert
    suspend fun insertEvent(event: AppUsageEvent)
    
    @Query("DELETE FROM app_usage_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteEventsBefore(beforeTimestamp: Long)
}
