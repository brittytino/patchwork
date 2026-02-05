package com.brittytino.patchwork.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brittytino.patchwork.data.database.entity.SystemSnapshot
import com.brittytino.patchwork.data.repository.SystemSnapshotRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SystemSnapshotViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SystemSnapshotRepository(application)
    
    val snapshots: StateFlow<List<SystemSnapshot>> = repository.getAllSnapshots()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val quickAccessSnapshots: StateFlow<List<SystemSnapshot>> = repository.getQuickAccessSnapshots()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedSnapshot = MutableStateFlow<SystemSnapshot?>(null)
    val selectedSnapshot = _selectedSnapshot.asStateFlow()
    
    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog = _showCreateDialog.asStateFlow()
    
    fun createSnapshot(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createSnapshotFromCurrent(name, description)
            _showCreateDialog.value = false
        }
    }
    
    fun restoreSnapshot(snapshot: SystemSnapshot) {
        viewModelScope.launch {
            repository.restoreSnapshot(snapshot)
        }
    }
    
    fun toggleQuickAccess(snapshot: SystemSnapshot) {
        viewModelScope.launch {
            repository.updateSnapshot(
                snapshot.copy(isQuickAccess = !snapshot.isQuickAccess)
            )
        }
    }
    
    fun updateSnapshot(snapshot: SystemSnapshot) {
        viewModelScope.launch {
            repository.updateSnapshot(snapshot)
        }
    }
    
    fun deleteSnapshot(snapshot: SystemSnapshot) {
        viewModelScope.launch {
            repository.deleteSnapshot(snapshot)
        }
    }
    
    fun showSnapshotDetails(snapshot: SystemSnapshot) {
        _selectedSnapshot.value = snapshot
    }
    
    fun hideSnapshotDetails() {
        _selectedSnapshot.value = null
    }
    
    fun showCreateDialog() {
        _showCreateDialog.value = true
    }
    
    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }
}
