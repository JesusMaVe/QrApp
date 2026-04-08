package com.example.qrapplication.scanner.components

import android.content.Context
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qrapplication.barcode.BarcodeAnalyzer
import com.example.qrapplication.barcode.DetectedBarcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    isFlashlightOn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Maintain a reference to the camera object
    val camera = remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    // Toggle flashlight whenever isFlashlightOn changes
    LaunchedEffect(isFlashlightOn) {
        camera.value?.cameraControl?.enableTorch(isFlashlightOn)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { previewView ->
            bindCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                executor = cameraExecutor,
                onBarcodeDetected = onBarcodeDetected
            ) { boundCamera ->
                camera.value = boundCamera
            }
        }
    )
}

private fun bindCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    executor: ExecutorService,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onCameraBound: (androidx.camera.core.Camera) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val analyzer = BarcodeAnalyzer(
            onBarcodeDetected = onBarcodeDetected
        )

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, analyzer)
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            onCameraBound(camera)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}