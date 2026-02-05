package com.brittytino.patchwork.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brittytino.patchwork.data.database.entity.AppBehaviorRule
import com.brittytino.patchwork.data.repository.AppBehaviorRuleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppBehaviorViewModel(application: Application) : AndroidViewModel(application) {
    
    data class AppInfo(
        val packageName: String,
        val appName: String,
        val icon: android.graphics.drawable.Drawable?
    )
    
    private val repository = AppBehaviorRuleRepository(application)
    private val packageManager = application.packageManager
    
    val rules: StateFlow<List<AppBehaviorRule>> = repository.getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps = _installedApps.asStateFlow()
    
    private val _selectedRule = MutableStateFlow<AppBehaviorRule?>(null)
    val selectedRule = _selectedRule.asStateFlow()
    
    private val _showAppPicker = MutableStateFlow(false)
    val showAppPicker = _showAppPicker.asStateFlow()
    
    init {
        loadInstalledApps()
    }
    
    private fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { app ->
                        // Filter out system apps without launcher
                        val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                        val hasLauncher = packageManager.getLaunchIntentForPackage(app.packageName) != null
                        (isUserApp || hasLauncher) && app.packageName != "com.brittytino.patchwork"
                    }
                    .map { app ->
                        AppInfo(
                            packageName = app.packageName,
                            appName = packageManager.getApplicationLabel(app).toString(),
                            icon = try {
                                packageManager.getApplicationIcon(app.packageName)
                            } catch (e: Exception) {
                                null
                            }
                        )
                    }
                    .sortedBy { it.appName.lowercase() }
                
                _installedApps.value = apps
            } catch (e: Exception) {
                android.util.Log.e("AppBehaviorVM", "Error loading apps", e)
            }
        }
    }
    
    fun createRuleForApp(app: AppInfo) {
        viewModelScope.launch {
            val rule = AppBehaviorRule(
                packageName = app.packageName,
                appName = app.appName,
                enabled = true
            )
            repository.insertRule(rule)
            _selectedRule.value = rule
        }
    }
    
    fun toggleRule(rule: AppBehaviorRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(enabled = !rule.enabled))
        }
    }
    
    fun updateRule(rule: AppBehaviorRule) {
        viewModelScope.launch {
            repository.updateRule(rule)
            _selectedRule.value = null
        }
    }
    
    fun deleteRule(rule: AppBehaviorRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }
    
    fun editRule(rule: AppBehaviorRule) {
        _selectedRule.value = rule
    }
    
    fun closeEditor() {
        _selectedRule.value = null
    }
    
    fun showAppPicker() {
        _showAppPicker.value = true
    }
    
    fun hideAppPicker() {
        _showAppPicker.value = false
    }
}
