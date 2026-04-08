package com.example.qrapplication.barcode

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class ImageAnalyzer(
    private val onSuccess: (Barcode) -> Unit,
    private val onFailure: (Exception) -> Unit
) {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
        .build()

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

    fun process(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    barcode.rawValue?.let { value ->
                        if (value.isNotBlank()) {
                            onSuccess(barcode)
                        } else {
                            onFailure(Exception("Codigo vacio"))
                        }
                    }
                } else {
                    onFailure(Exception("No se detectaron codigos en la imagen"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}