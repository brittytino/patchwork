package com.brittytino.patchwork.data.repository

import android.content.Context
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.dao.AppBehaviorRuleDao
import com.brittytino.patchwork.data.database.entity.AppBehaviorRule
import kotlinx.coroutines.flow.Flow

class AppBehaviorRuleRepository(context: Context) {
    
    private val dao: AppBehaviorRuleDao
    
    init {
        val database = AppDatabase.getInstance(context)
        dao = database.appBehaviorRuleDao()
    }
    
    fun getEnabledRules(): Flow<List<AppBehaviorRule>> = dao.getEnabledRules()
    
    fun getAllRules(): Flow<List<AppBehaviorRule>> = dao.getAllRules()
    
    suspend fun getRuleForApp(packageName: String): AppBehaviorRule? = 
        dao.getRuleForApp(packageName)
    
    suspend fun insertRule(rule: AppBehaviorRule) {
        dao.insertRule(rule)
    }
    
    suspend fun updateRule(rule: AppBehaviorRule) {
        dao.updateRule(rule)
    }
    
    suspend fun deleteRule(rule: AppBehaviorRule) {
        dao.deleteRule(rule)
    }
    
    suspend fun markRuleApplied(id: String) {
        dao.markRuleApplied(id, System.currentTimeMillis())
    }
}
