package com.example.qrapplication.screens.generator

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.qrapplication.data.GeneratorRepository
import com.example.qrapplication.model.GeneratedQr
import com.example.qrapplication.qr.QrGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GeneratorState(
    val inputText: String = "",
    val currentQrBitmap: Bitmap? = null,
    val history: List<GeneratedQr> = emptyList(),
    val isSaved: Boolean = false
)

class GeneratorViewModel(
    private val repository: GeneratorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.generatedQrs.collect { qrList ->
                _state.value = _state.value.copy(history = qrList)
            }
        }
    }

    fun onInputChanged(text: String) {
        val bitmap = if (text.isNotBlank()) QrGenerator.generate(text) else null
        _state.value = _state.value.copy(
            inputText = text,
            currentQrBitmap = bitmap,
            isSaved = false
        )
    }

    fun saveToHistory() {
        val content = _state.value.inputText
        if (content.isNotBlank()) {
            viewModelScope.launch {
                repository.saveQr(content)
                _state.value = _state.value.copy(isSaved = true)
            }
        }
    }

    fun loadFromHistory(qr: GeneratedQr) {
        val bitmap = QrGenerator.generate(qr.content)
        _state.value = _state.value.copy(
            inputText = qr.content,
            currentQrBitmap = bitmap,
            isSaved = true
        )
    }

    fun deleteFromHistory(id: String) {
        viewModelScope.launch {
            repository.deleteQr(id)
        }
    }
}

class GeneratorViewModelFactory(
    private val repository: GeneratorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeneratorViewModel(repository) as T
    }
}