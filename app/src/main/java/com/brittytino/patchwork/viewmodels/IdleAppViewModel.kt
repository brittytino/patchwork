package com.brittytino.patchwork.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.IdleAppRule
import com.brittytino.patchwork.data.repository.IdleAppRepository
import com.brittytino.patchwork.services.IdleAppEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IdleAppViewModel(application: Application) : AndroidViewModel(application) {
    
    data class AppInfo(
        val packageName: String,
        val appName: String,
        val icon: Drawable
    )
    
    private val repository = IdleAppRepository(AppDatabase.getInstance(application))
    private val engine = IdleAppEngine.getInstance(application)
    
    // State
    val rules = repository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val recentLogs = repository.getRecentLogs(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()
    
    private val _showAppPicker = MutableStateFlow(false)
    val showAppPicker: StateFlow<Boolean> = _showAppPicker.asStateFlow()
    
    private val _showRuleEditor = MutableStateFlow(false)
    val showRuleEditor: StateFlow<Boolean> = _showRuleEditor.asStateFlow()
    
    private val _editingRule = MutableStateFlow<IdleAppRule?>(null)
    val editingRule: StateFlow<IdleAppRule?> = _editingRule.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private val _idleStats = MutableStateFlow<Map<String, IdleAppEngine.IdleStats>>(emptyMap())
    val idleStats: StateFlow<Map<String, IdleAppEngine.IdleStats>> = _idleStats.asStateFlow()
    
    init {
        loadInstalledApps()
        loadIdleStats()
    }
    
    // ==================== App Loading ====================
    
    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val pm = getApplication<Application>().packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
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
            val rule = IdleAppRule(
                packageName = app.packageName,
                appName = app.appName,
                enabled = true,
                idleThresholdMinutes = 180, // 3 hours default
                action = IdleAppRule.Action.NOTIFY,
                actionCount = 0,
                lastCheckedAt = null
            )
            repository.insertRule(rule)
            
            // Open editor for the new rule
            _editingRule.value = rule
            _showRuleEditor.value = true
        }
    }
    
    fun toggleRule(rule: IdleAppRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(enabled = !rule.enabled))
        }
    }
    
    fun editRule(rule: IdleAppRule) {
        _editingRule.value = rule
        _showRuleEditor.value = true
    }
    
    fun updateRule(rule: IdleAppRule) {
        viewModelScope.launch {
            repository.updateRule(rule)
        }
    }
    
    fun deleteRule(rule: IdleAppRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }
    
    // ==================== Monitoring Control ====================
    
    fun startMonitoring() {
        engine.startMonitoring()
        _isMonitoring.value = true
    }
    
    fun stopMonitoring() {
        engine.stopMonitoring()
        _isMonitoring.value = false
    }
    
    fun toggleMonitoring() {
        if (_isMonitoring.value) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }
    
    // ==================== Statistics ====================
    
    private fun loadIdleStats() {
        viewModelScope.launch {
            rules.collect { rulesList ->
                val statsMap = mutableMapOf<String, IdleAppEngine.IdleStats>()
                rulesList.forEach { rule ->
                    val stats = engine.getAppIdleStats(rule.packageName)
                    if (stats != null) {
                        statsMap[rule.packageName] = stats
                    }
                }
                _idleStats.value = statsMap
            }
        }
    }
    
    fun refreshStats() {
        loadIdleStats()
    }
    
    fun checkAppNow(packageName: String) {
        viewModelScope.launch {
            engine.checkAppNow(packageName)
            refreshStats()
        }
    }
}
