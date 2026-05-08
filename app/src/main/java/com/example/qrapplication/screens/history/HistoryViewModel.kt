package com.example.qrapplication.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrapplication.data.ScanRepository
import com.example.qrapplication.model.QrFolder
import com.example.qrapplication.model.ScanRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: ScanRepository
) : ViewModel() {

    val scans: StateFlow<List<ScanRecord>> = repository.scans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val folders: StateFlow<List<QrFolder>> = repository.folders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteScan(id: String) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
        }
    }

    fun moveScanToFolder(scanId: String, folderId: String?) {
        viewModelScope.launch {
            repository.moveScanToFolder(scanId, folderId)
        }
    }
}
