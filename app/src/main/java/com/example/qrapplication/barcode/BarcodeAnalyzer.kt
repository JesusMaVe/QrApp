package com.example.qrapplication.barcode

import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

data class DetectedBarcode(
    val barcode: Barcode,
    val isInFrame: Boolean,
    val normalizedRect: RectF? // screen-relative coordinates (0-1)
)

class BarcodeAnalyzer(
    private val onBarcodeDetected: (DetectedBarcode) -> Unit,
    private val frameBounds: RectF = RectF(0.2f, 0.2f, 0.8f, 0.6f)
) : ImageAnalysis.Analyzer {

    companion object {
        // Adaptive frame interval based on device performance
        // Higher FPS for faster devices, lower for slower ones
        private const val DEFAULT_FRAME_INTERVAL_MS = 150L
        private const val MIN_FRAME_INTERVAL_MS = 80L
        private const val MAX_FRAME_INTERVAL_MS = 300L

        // Reusable scanner instance - single instance for all analyzers
        @Volatile
        private var sharedScanner: BarcodeScanner? = null

        // Singleton scanner for ML Kit reuse
        private val scannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()

        fun getSharedScanner(): BarcodeScanner {
            return sharedScanner ?: synchronized(this) {
                sharedScanner ?: BarcodeScanning.getClient(scannerOptions).also {
                    sharedScanner = it
                }
            }
        }
    }

    private val scanner: BarcodeScanner = getSharedScanner()

    // Adaptive frame interval - starts conservative, adjusts based on processing time
    @Volatile
    private var frameIntervalMs = DEFAULT_FRAME_INTERVAL_MS

    private var lastAnalyzedTimestamp = 0L
    private var processingStartTime = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        val interval = frameIntervalMs

        if (now - lastAnalyzedTimestamp < interval) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            lastAnalyzedTimestamp = now
            processingStartTime = now

            val imageWidth = mediaImage.width.toFloat()
            val imageHeight = mediaImage.height.toFloat()

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Adapt frame interval based on processing time
                    adjustFrameInterval()

                    if (barcodes.isNotEmpty()) {
                        val detectedBarcode = barcodes.first()
                        detectedBarcode.rawValue?.let { value ->
                            if (value.isNotBlank()) {
                                processBarcode(detectedBarcode, imageWidth, imageHeight)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("BarcodeAnalyzer", "ML Kit failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun adjustFrameInterval() {
        val processingTime = System.currentTimeMillis() - processingStartTime
        // Reduce interval if processing is fast (increase FPS)
        // Increase interval if processing is slow (reduce CPU usage)
        val newInterval = when {
            processingTime < 50L -> maxOf(MIN_FRAME_INTERVAL_MS, frameIntervalMs - 20L)
            processingTime > 100L -> minOf(MAX_FRAME_INTERVAL_MS, frameIntervalMs + 30L)
            else -> frameIntervalMs
        }
        frameIntervalMs = newInterval
    }

    private fun processBarcode(
        detectedBarcode: Barcode,
        imageWidth: Float,
        imageHeight: Float
    ) {
        val cornerPoints = detectedBarcode.cornerPoints
        val isInFrame: Boolean
        val normalizedRect: RectF?

        if (cornerPoints != null && cornerPoints.size >= 4) {
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE

            for (point in cornerPoints) {
                minX = minOf(minX, point.x.toFloat())
                minY = minOf(minY, point.y.toFloat())
                maxX = maxOf(maxX, point.x.toFloat())
                maxY = maxOf(maxY, point.y.toFloat())
            }

            // Calculate center and size
            val centerX = (minX + maxX) / 2f / imageWidth
            val centerY = (minY + maxY) / 2f / imageHeight
            val width = (maxX - minX) / imageWidth
            val height = (maxY - minY) / imageHeight

            // Scale up for visual (the QR corners may not include quiet zone)
            val scaledWidth = width * 1.8f
            val scaledHeight = height * 1.8f

            val left = (centerX - scaledWidth / 2f).coerceIn(0f, 1f)
            val top = (centerY - scaledHeight / 2f).coerceIn(0f, 1f)
            val right = (centerX + scaledWidth / 2f).coerceIn(0f, 1f)
            val bottom = (centerY + scaledHeight / 2f).coerceIn(0f, 1f)

            val barcodeRect = RectF(left, top, right, bottom)
            isInFrame = !barcodeRect.isEmpty && barcodeRect.intersect(frameBounds)
            normalizedRect = barcodeRect
        } else {
            val boundingBox = detectedBarcode.boundingBox
            if (boundingBox != null) {
                val left = boundingBox.left.toFloat() / imageWidth
                val top = boundingBox.top.toFloat() / imageHeight
                val right = boundingBox.right.toFloat() / imageWidth
                val bottom = boundingBox.bottom.toFloat() / imageHeight

                val barcodeRect = RectF(left, top, right, bottom)
                isInFrame = !barcodeRect.isEmpty && barcodeRect.intersect(frameBounds)
                normalizedRect = barcodeRect
            } else {
                isInFrame = false
                normalizedRect = null
            }
        }

        onBarcodeDetected(
            DetectedBarcode(
                barcode = detectedBarcode,
                isInFrame = isInFrame,
                normalizedRect = normalizedRect
            )
        )
    }
}