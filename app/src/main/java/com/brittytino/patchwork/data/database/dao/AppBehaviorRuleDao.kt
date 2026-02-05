package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.AppBehaviorRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AppBehaviorRuleDao {
    @Query("SELECT * FROM app_behavior_rules WHERE enabled = 1")
    fun getEnabledRules(): Flow<List<AppBehaviorRule>>
    
    @Query("SELECT * FROM app_behavior_rules")
    fun getAllRules(): Flow<List<AppBehaviorRule>>
    
    @Query("SELECT * FROM app_behavior_rules WHERE packageName = :packageName")
    suspend fun getRuleForApp(packageName: String): AppBehaviorRule?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AppBehaviorRule)
    
    @Update
    suspend fun updateRule(rule: AppBehaviorRule)
    
    @Delete
    suspend fun deleteRule(rule: AppBehaviorRule)
    
    @Query("UPDATE app_behavior_rules SET lastAppliedAt = :timestamp, applyCount = applyCount + 1 WHERE id = :id")
    suspend fun markRuleApplied(id: String, timestamp: Long)
}
