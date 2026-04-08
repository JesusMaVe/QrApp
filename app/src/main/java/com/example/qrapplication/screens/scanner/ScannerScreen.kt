package com.example.qrapplication.screens.scanner

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrapplication.barcode.ActionHandler
import com.example.qrapplication.barcode.BarcodeScannerViewModel
import com.example.qrapplication.barcode.HapticManager
import com.example.qrapplication.barcode.ScannerState
import com.example.qrapplication.data.ScanRepository
import com.example.qrapplication.model.ScanRecord
import com.example.qrapplication.scanner.components.CameraPreview
import com.example.qrapplication.scanner.components.ScanOverlay
import com.example.qrapplication.screens.scanner.components.ResultBottomSheet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    repository: ScanRepository,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val isFlashOn by viewModel.isFlashlightOn.collectAsState()
    val lastFormat by viewModel.lastFormat.collectAsState()

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.startScanning()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            !cameraPermissionState.status.isGranted -> {
                PermissionDeniedContent(
                    shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            else -> {
                ScannerContent(
                    state = state,
                    isFlashOn = isFlashOn,
                    lastFormat = lastFormat,
                    onToggleFlash = { viewModel.toggleFlashlight() },
                    onBarcodeDetected = { barcode ->
                        viewModel.onBarcodeDetected(barcode)
                        HapticManager.vibrate(context)
                    },
                    onDismissResult = {
                        viewModel.reset()
                    },
                    onAction = { barcode, contentType ->
                        val content = barcode.rawValue ?: return@ScannerContent
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.saveScan(
                                ScanRecord(
                                    content = content,
                                    contentType = contentType.name
                                )
                            )
                        }
                        ActionHandler.execute(context, content, contentType)
                        viewModel.reset()
                    }
                )
            }
        }
    }
}

@Composable
private fun ScannerContent(
    state: ScannerState,
    isFlashOn: Boolean,
    lastFormat: Int,
    onToggleFlash: () -> Unit,
    onBarcodeDetected: (com.google.mlkit.vision.barcode.common.Barcode) -> Unit,
    onDismissResult: () -> Unit,
    onAction: (com.google.mlkit.vision.barcode.common.Barcode, com.example.qrapplication.model.ContentType) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onBarcodeDetected = onBarcodeDetected,
            isFlashlightOn = isFlashOn
        )

        ScanOverlay(barcodeFormat = lastFormat)

        // Flashlight Toggle
        IconButton(
            onClick = onToggleFlash,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Linterna"
            )
        }

        // Contextual Hint
        Text(
            text = "Encuadra el código para escanear",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (state is ScannerState.Success) {
            ResultBottomSheet(
                contentType = state.contentType,
                content = state.barcode.rawValue ?: "",
                onDismiss = onDismissResult,
                onAction = { onAction(state.barcode, state.contentType) }
            )
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Se necesita acceso a la camara",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (shouldShowRationale) {
                "La camara es necesaria para escanear codigos QR y de barras."
            } else {
                "Sin permiso de camara no es posible escanear codigos."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(onClick = onRequestPermission) {
            Text("Conceder permiso")
        }

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Abrir configuracion")
        }
    }
}
