package com.example.qrapplication.barcode

import androidx.lifecycle.ViewModel
import com.example.qrapplication.model.ContentType
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class ScannerState {
    object Idle : ScannerState()
    object Scanning : ScannerState()
    data class Success(val barcode: Barcode, val contentType: ContentType) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

class BarcodeScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    private var lastScanTimestamp = 0L
    private var lastScannedValue: String? = null

    private val debounceIntervalMs = 3000L

    fun startScanning() {
        _state.update { ScannerState.Scanning }
    }

    fun onBarcodeDetected(barcode: Barcode) {
        val now = System.currentTimeMillis()
        val rawValue = barcode.rawValue

        if (rawValue == null || rawValue.isBlank()) return

        if (rawValue == lastScannedValue && (now - lastScanTimestamp) < debounceIntervalMs) return

        lastScanTimestamp = now
        lastScannedValue = rawValue

        val contentType = ContentTypeDetector.detect(barcode)
        _state.update { ScannerState.Success(barcode, contentType) }
    }

    fun reset() {
        lastScannedValue = null
        _state.update { ScannerState.Scanning }
    }

    fun onError(message: String) {
        _state.update { ScannerState.Error(message) }
    }
}
