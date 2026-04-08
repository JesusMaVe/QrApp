package com.example.qrapplication.barcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class ImagePickerManager(
    private val context: Context,
    private val onImageSelected: (Bitmap) -> Unit,
    private val onError: (String) -> Unit
) {
    private var pendingUri: Uri? = null

    fun launch(launcher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>) {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun handleResult(uri: Uri?) {
        if (uri != null) {
            try {
                val bitmap = loadBitmap(uri)
                if (bitmap != null) {
                    onImageSelected(bitmap)
                } else {
                    onError("No se pudo cargar la imagen")
                }
            } catch (e: Exception) {
                onError("Error al procesar imagen")
            }
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                
                val targetWidth = 1920
                val targetHeight = 1920
                val widthRatio = options.outWidth.toFloat() / targetWidth
                val heightRatio = options.outHeight.toFloat() / targetHeight
                val sampleSize = maxOf(widthRatio, heightRatio).toInt().coerceAtLeast(1)
                
                options.inJustDecodeBounds = false
                options.inSampleSize = sampleSize
                
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ImagePicker", "Error loading bitmap", e)
            null
        }
    }
}