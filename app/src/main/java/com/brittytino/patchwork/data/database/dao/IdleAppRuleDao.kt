package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.IdleAppRule
import kotlinx.coroutines.flow.Flow

@Dao
interface IdleAppRuleDao {
    @Query("SELECT * FROM idle_app_rules WHERE enabled = 1")
    fun getEnabledRules(): Flow<List<IdleAppRule>>
    
    @Query("SELECT * FROM idle_app_rules")
    fun getAllRules(): Flow<List<IdleAppRule>>
    
    @Query("SELECT * FROM idle_app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleForApp(packageName: String): IdleAppRule?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: IdleAppRule): Long
    
    @Update
    suspend fun updateRule(rule: IdleAppRule)
    
    @Delete
    suspend fun deleteRule(rule: IdleAppRule)
}
