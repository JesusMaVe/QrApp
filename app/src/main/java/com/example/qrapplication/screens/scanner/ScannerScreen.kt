package com.example.qrapplication.screens.scanner

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrapplication.barcode.BarcodeScannerViewModel
import com.example.qrapplication.barcode.ScannerState
import com.example.qrapplication.scanner.components.CameraPreview
import com.example.qrapplication.scanner.components.ScanOverlay
import com.example.qrapplication.screens.scanner.components.ResultBottomSheet
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onScanResult: (String) -> Unit,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

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
                    onBarcodeDetected = { barcode ->
                        viewModel.onBarcodeDetected(barcode)
                    },
                    onDismissResult = {
                        viewModel.reset()
                    },
                    onAction = { rawValue ->
                        onScanResult(rawValue)
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
    onBarcodeDetected: (com.google.mlkit.vision.barcode.common.Barcode) -> Unit,
    onDismissResult: () -> Unit,
    onAction: (String) -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(onBarcodeDetected = onBarcodeDetected)

        ScanOverlay()

        if (state is ScannerState.Success) {
            ResultBottomSheet(
                contentType = state.contentType,
                content = state.barcode.rawValue ?: "",
                onDismiss = onDismissResult,
                onAction = { onAction(state.barcode.rawValue ?: "") }
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
