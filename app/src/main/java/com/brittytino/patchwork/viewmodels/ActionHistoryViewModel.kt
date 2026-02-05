package com.brittytino.patchwork.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brittytino.patchwork.data.database.AppDatabase
import com.brittytino.patchwork.data.database.entity.ActionHistoryEntry
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.data.repository.ActionHistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ActionHistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ActionHistoryRepository
    
    init {
        val database = AppDatabase.getInstance(application)
        repository = ActionHistoryRepository(database.actionHistoryDao())
    }
    
    private val _selectedFilter = MutableStateFlow<FilterType>(FilterType.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()
    
    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL_TIME)
    val selectedTimeRange = _selectedTimeRange.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    val actions: StateFlow<List<ActionHistoryEntry>> = combine(
        repository.getRecentActions(1000),
        _selectedFilter,
        _selectedTimeRange,
        _searchQuery
    ) { allActions, filter, timeRange, query ->
        var filtered = allActions
        
        // Apply time range filter
        if (timeRange != TimeRange.ALL_TIME) {
            val cutoffTime = System.currentTimeMillis() - timeRange.milliseconds
            filtered = filtered.filter { it.timestamp >= cutoffTime }
        }
        
        // Apply category/type filter
        filtered = when (filter) {
            FilterType.ALL -> filtered
            FilterType.SYSTEM -> filtered.filter { it.category == "System" }
            FilterType.APPS -> filtered.filter { it.category == "App Management" || it.category == "App Behavior" }
            FilterType.AUTOMATION -> filtered.filter { it.category == "Automation" }
            FilterType.SECURITY -> filtered.filter { it.category == "Privacy" || it.category == "Security" }
            is FilterType.ByType -> filtered.filter { it.actionType == filter.type }
        }
        
        // Apply search query
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.targetApp?.contains(query, ignoreCase = true) == true
            }
        }
        
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val totalCount = repository.getRecentActions(10000)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
    }
    
    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
    
    fun cleanupOldLogs(daysToKeep: Int = 30) {
        viewModelScope.launch {
            repository.cleanupOldLogs(daysToKeep)
        }
    }
}

sealed class FilterType {
    data object ALL : FilterType()
    data object SYSTEM : FilterType()
    data object APPS : FilterType()
    data object AUTOMATION : FilterType()
    data object SECURITY : FilterType()
    data class ByType(val type: ActionType) : FilterType()
}

enum class TimeRange(val milliseconds: Long) {
    LAST_HOUR(60 * 60 * 1000L),
    TODAY(24 * 60 * 60 * 1000L),
    LAST_7_DAYS(7 * 24 * 60 * 60 * 1000L),
    LAST_30_DAYS(30 * 24 * 60 * 60 * 1000L),
    ALL_TIME(0L)
}
