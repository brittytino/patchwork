package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.SystemSnapshot
import kotlinx.coroutines.flow.Flow

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
