package com.example.qrapplication.barcode

import android.graphics.RectF
import androidx.lifecycle.ViewModel
import com.example.qrapplication.model.ContentType
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicReference

sealed class ScannerState {
    object Idle : ScannerState()
    object Scanning : ScannerState()
    data class Success(val barcode: Barcode, val contentType: ContentType) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

data class TrackingState(
    val isTracking: Boolean = false,
    val normalizedRect: RectF? = null,
    val isInFrame: Boolean = false
)

class BarcodeScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isBurstMode = MutableStateFlow(false)
    val isBurstMode: StateFlow<Boolean> = _isBurstMode.asStateFlow()

    private val _lastFormat = MutableStateFlow(Barcode.FORMAT_QR_CODE)
    val lastFormat: StateFlow<Int> = _lastFormat.asStateFlow()

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    val frameBounds = AtomicReference(RectF(0.3f, 0.3f, 0.7f, 0.7f))

    private var lastScanTimestamp = 0L
    private var lastScannedValue: String? = null

    private val debounceIntervalMs = 3000L
    private val burstDebounceIntervalMs = 500L

    fun startScanning() {
        _state.update { ScannerState.Scanning }
        _trackingState.update { TrackingState() }
    }

    fun onBarcodeDetected(detectedBarcode: DetectedBarcode) {
        val barcode = detectedBarcode.barcode
        _lastFormat.update { barcode.format }

        _trackingState.update {
            TrackingState(
                isTracking = true,
                normalizedRect = detectedBarcode.normalizedRect,
                isInFrame = detectedBarcode.isInFrame
            )
        }

        if (!detectedBarcode.isInFrame) {
            return
        }

        val now = System.currentTimeMillis()
        val rawValue = barcode.rawValue

        if (rawValue == null || rawValue.isBlank()) return

        val debounce = if (_isBurstMode.value) burstDebounceIntervalMs else debounceIntervalMs

        if (rawValue == lastScannedValue && (now - lastScanTimestamp) < debounce) return

        lastScanTimestamp = now
        lastScannedValue = rawValue

        val contentType = ContentTypeDetector.detect(barcode)
        _state.update { ScannerState.Success(barcode, contentType) }
    }

    fun updateFrameBounds(normalizedRect: RectF) {
        frameBounds.set(normalizedRect)
    }

    fun toggleFlashlight() {
        _isFlashlightOn.update { !it }
    }

    fun toggleBurstMode() {
        _isBurstMode.update { !it }
    }

    fun setGalleryResult(barcode: Barcode, contentType: ContentType) {
        lastScannedValue = barcode.rawValue
        lastScanTimestamp = System.currentTimeMillis()
        _state.update { ScannerState.Success(barcode, contentType) }
    }

    fun reset() {
        lastScannedValue = null
        _state.update { ScannerState.Scanning }
        _trackingState.update { TrackingState() }
    }

    fun onError(message: String) {
        _state.update { ScannerState.Error(message) }
    }
}