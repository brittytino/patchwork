package com.brittytino.patchwork.data.database.dao

import androidx.room.*
import com.brittytino.patchwork.data.database.entity.AppCooldownRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCooldownRuleDao {
    @Query("SELECT * FROM app_cooldown_rules WHERE enabled = 1")
    fun getEnabledRules(): Flow<List<AppCooldownRule>>
    
    @Query("SELECT * FROM app_cooldown_rules")
    fun getAllRules(): Flow<List<AppCooldownRule>>
    
    @Query("SELECT * FROM app_cooldown_rules WHERE packageName = :packageName")
    suspend fun getRuleForApp(packageName: String): AppCooldownRule?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AppCooldownRule): Long
    
    @Update
    suspend fun updateRule(rule: AppCooldownRule)
    
    @Delete
    suspend fun deleteRule(rule: AppCooldownRule)
    
    @Query("UPDATE app_cooldown_rules SET timesStopped = timesStopped + 1, lastTriggered = :timestamp WHERE id = :id")
    suspend fun incrementCooldownTriggered(id: String, timestamp: Long)
    
    @Query("UPDATE app_cooldown_rules SET timesBypassed = timesBypassed + 1 WHERE id = :id")
    suspend fun incrementCooldownBypassed(id: String)
}
