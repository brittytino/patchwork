package com.brittytino.patchwork.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.AppCooldownRule
import com.brittytino.patchwork.data.repository.AppCooldownRepository
import com.brittytino.patchwork.services.AppCooldownEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppCooldownViewModel(application: Application) : AndroidViewModel(application) {
    
    data class AppInfo(
        val packageName: String,
        val appName: String,
        val icon: Drawable
    )
    
    private val repository = AppCooldownRepository(AppDatabase.getInstance(application))
    private val engine = AppCooldownEngine.getInstance(application)
    
    // State
    val rules = repository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()
    
    private val _showAppPicker = MutableStateFlow(false)
    val showAppPicker: StateFlow<Boolean> = _showAppPicker.asStateFlow()
    
    private val _showRuleEditor = MutableStateFlow(false)
    val showRuleEditor: StateFlow<Boolean> = _showRuleEditor.asStateFlow()
    
    private val _editingRule = MutableStateFlow<AppCooldownRule?>(null)
    val editingRule: StateFlow<AppCooldownRule?> = _editingRule.asStateFlow()
    
    private val _appStats = MutableStateFlow<Map<String, AppCooldownEngine.AppStats>>(emptyMap())
    val appStats: StateFlow<Map<String, AppCooldownEngine.AppStats>> = _appStats.asStateFlow()
    
    init {
        loadInstalledApps()
        loadAppStats()
    }
    
    // ==================== App Loading ====================
    
    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val pm = getApplication<Application>().packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // Filter out system apps
                    .map { appInfo ->
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo)
                        )
                    }
                    .sortedBy { it.appName }
                
                _installedApps.value = apps
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // ==================== Dialog Management ====================
    
    fun showAppPickerDialog() {
        _showAppPicker.value = true
    }
    
    fun hideAppPickerDialog() {
        _showAppPicker.value = false
    }
    
    fun showRuleEditorDialog() {
        _showRuleEditor.value = true
    }
    
    fun hideRuleEditorDialog() {
        _showRuleEditor.value = false
        _editingRule.value = null
    }
    
    // ==================== Rule Management ====================
    
    fun createRuleForApp(app: AppInfo) {
        viewModelScope.launch {
            val rule = AppCooldownRule(
                packageName = app.packageName,
                appName = app.appName,
                enabled = true,
                cooldownPeriodMinutes = 30,
                maxDailyOpens = null,
                maxHourlyOpens = null,
                showWarningDialog = true,
                blockLaunch = false
            )
            repository.insertRule(rule)
            
            // Open editor for the new rule
            _editingRule.value = rule
            _showRuleEditor.value = true
        }
    }
    
    fun toggleRule(rule: AppCooldownRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(enabled = !rule.enabled))
        }
    }
    
    fun editRule(rule: AppCooldownRule) {
        _editingRule.value = rule
        _showRuleEditor.value = true
    }
    
    fun updateRule(rule: AppCooldownRule) {
        viewModelScope.launch {
            repository.updateRule(rule)
        }
    }
    
    fun deleteRule(rule: AppCooldownRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }
    
    // ==================== Statistics ====================
    
    private fun loadAppStats() {
        viewModelScope.launch {
            rules.collect { rulesList ->
                val statsMap = mutableMapOf<String, AppCooldownEngine.AppStats>()
                rulesList.forEach { rule ->
                    val stats = engine.getAppStats(rule.packageName)
                    statsMap[rule.packageName] = stats
                }
                _appStats.value = statsMap
            }
        }
    }
    
    fun refreshStats() {
        loadAppStats()
    }
    
    fun getRemainingCooldown(packageName: String, callback: (Long) -> Unit) {
        viewModelScope.launch {
            val remaining = engine.getRemainingCooldown(packageName)
            callback(remaining)
        }
    }
}
