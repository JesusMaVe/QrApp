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

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_DATA_MATRIX
        )
        .build()

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val imageWidth = mediaImage.width.toFloat()
            val imageHeight = mediaImage.height.toFloat()
            
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val detectedBarcode = barcodes.first()
                        detectedBarcode.rawValue?.let { value ->
                            if (value.isNotBlank()) {
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
}