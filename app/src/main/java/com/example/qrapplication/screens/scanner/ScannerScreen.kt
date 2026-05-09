package com.example.qrapplication.screens.scanner

import android.content.Intent
import android.net.Uri
import android.os.VibratorManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrapplication.barcode.ActionHandler
import com.example.qrapplication.barcode.BarcodeScannerViewModel
import com.example.qrapplication.barcode.ContentTypeDetector
import com.example.qrapplication.barcode.DetectedBarcode
import com.example.qrapplication.barcode.ImageAnalyzer
import com.example.qrapplication.barcode.ScannerState
import com.example.qrapplication.barcode.TrackingState
import com.example.qrapplication.data.ScanRepository
import com.example.qrapplication.model.QrFolder
import com.example.qrapplication.model.ScanRecord
import com.example.qrapplication.scanner.components.CameraPreview
import com.example.qrapplication.scanner.components.ScanOverlay
import com.example.qrapplication.screens.scanner.components.ResultBottomSheet
import com.example.qrapplication.screens.history.components.FolderDialog
import com.example.qrapplication.ui.theme.ScannerColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    repository: ScanRepository,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val isFlashOn by viewModel.isFlashlightOn.collectAsState()
    val isBurstMode by viewModel.isBurstMode.collectAsState()
    val trackingState by viewModel.trackingState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Folder selection state
    var showFolderDialog by remember { mutableStateOf(false) }
    var pendingScanContent by remember { mutableStateOf<String?>(null) }
    var pendingScanContentType by remember { mutableStateOf<com.example.qrapplication.model.ContentType?>(null) }

    val folders by remember { mutableStateOf(repository.folders) }
    val foldersSnapshot by repository.folders.collectAsState(initial = emptyList())

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = android.graphics.BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(uri)
                )
                if (bitmap != null) {
                    val analyzer = ImageAnalyzer(
                        onSuccess = { barcode ->
                            val contentType = ContentTypeDetector.detect(barcode)
                            viewModel.setGalleryResult(barcode, contentType)
                        },
                        onFailure = { e ->
                            Toast.makeText(context, e.message ?: "No se detecto codigo", Toast.LENGTH_SHORT).show()
                        }
                    )
                    analyzer.process(bitmap)
                } else {
                    Toast.makeText(context, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.startScanning()
        }
    }

    LaunchedEffect(state, isBurstMode) {
        if (isBurstMode && state is ScannerState.Success) {
            val successState = state as ScannerState.Success
            val content = successState.barcode.rawValue ?: return@LaunchedEffect
            
            coroutineScope.launch {
                repository.saveScan(
                    ScanRecord(
                        content = content,
                        contentType = successState.contentType.name
                    )
                )
            }
            vibrate(context)
            delay(800)
            viewModel.reset()
        }
    }

    fun launchGallery() {
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
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
                    isBurstMode = isBurstMode,
                    trackingState = trackingState,
                    onToggleFlash = { viewModel.toggleFlashlight() },
                    onToggleBurstMode = { viewModel.toggleBurstMode() },
                    onLaunchGallery = { launchGallery() },
                    onBarcodeDetected = { detectedBarcode: DetectedBarcode ->
                        try {
                            viewModel.onBarcodeDetected(detectedBarcode)
                            if (detectedBarcode.isInFrame) {
                                if (isBurstMode) {
                                    vibrate(context)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ScannerScreen", "Scan failed", e)
                        }
                    },
                    onDismissResult = {
                        viewModel.reset()
                    },
                    onAction = { barcode, contentType ->
                        val content = barcode.rawValue ?: return@ScannerContent
                        // Store pending scan and show folder dialog
                        pendingScanContent = content
                        pendingScanContentType = contentType
                        showFolderDialog = true
                    }
                )
            }
        }
    }

    // Folder selection dialog
    if (showFolderDialog && pendingScanContent != null && pendingScanContentType != null) {
        FolderDialog(
            folders = foldersSnapshot,
            onFolderSelected = { folder ->
                val content = pendingScanContent!!
                val contentType = pendingScanContentType!!
                coroutineScope.launch {
                    repository.saveScan(
                        ScanRecord(
                            content = content,
                            contentType = contentType.name,
                            folderId = folder?.id
                        )
                    )
                    // Execute original action
                    ActionHandler.execute(context, content, contentType)
                }
                viewModel.reset()
                showFolderDialog = false
                pendingScanContent = null
                pendingScanContentType = null
            },
            onCreateFolder = { name ->
                coroutineScope.launch {
                    repository.createFolder(name)
                }
            },
            onRenameFolder = { },
            onDeleteFolder = { },
            onDismiss = {
                showFolderDialog = false
                pendingScanContent = null
                pendingScanContentType = null
                viewModel.reset()
            }
        )
    }
}

private fun vibrate(context: android.content.Context) {
    try {
        @Suppress("DEPRECATION")
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("Haptic", "Vibration failed", e)
    }
}

@Composable
private fun ScannerContent(
    state: ScannerState,
    isFlashOn: Boolean,
    isBurstMode: Boolean,
    trackingState: TrackingState,
    onToggleFlash: () -> Unit,
    onToggleBurstMode: () -> Unit,
    onLaunchGallery: () -> Unit,
    onBarcodeDetected: (DetectedBarcode) -> Unit,
    onDismissResult: () -> Unit,
    onAction: (com.google.mlkit.vision.barcode.common.Barcode, com.example.qrapplication.model.ContentType) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val iconButtonBg = if (isDark) ScannerColors.IconButtonBgDark else ScannerColors.IconButtonBgLight
    val hintBg = if (isDark) ScannerColors.HintBgDark else ScannerColors.HintBgLight
    val overlayGradient = if (isDark) {
        listOf(
            ScannerColors.OverlayDark.copy(alpha = 0.7f),
            Color.Transparent,
            Color.Transparent,
            ScannerColors.OverlayDark.copy(alpha = 0.7f)
        )
    } else {
        listOf(
            Color.Black.copy(alpha = 0.5f),
            Color.Transparent,
            Color.Transparent,
            Color.Black.copy(alpha = 0.5f)
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onBarcodeDetected = onBarcodeDetected,
            isFlashlightOn = isFlashOn
        )

        ScanOverlay(
            trackingState = trackingState
        )

        // Top gradient for better UI visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = overlayGradient
                    )
                )
        )

        // Action buttons row
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gallery Button
            IconButton(
                onClick = onLaunchGallery,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconButtonBg,
                        MaterialTheme.shapes.medium
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Galería"
                )
            }

            // Burst Mode Toggle
            IconButton(
                onClick = onToggleBurstMode,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isBurstMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else iconButtonBg,
                        MaterialTheme.shapes.medium
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Modo ráfaga"
                )
            }

            // Flashlight Toggle
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconButtonBg,
                        MaterialTheme.shapes.medium
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Linterna"
                )
            }
        }

        // Hint text
        val hintText = when {
            isBurstMode && trackingState.isTracking && trackingState.isInFrame -> "Escaneando..."
            trackingState.isTracking && !trackingState.isInFrame -> "Mueve el código al marco"
            trackingState.isTracking && trackingState.isInFrame -> "Código detectado"
            isBurstMode -> "Modo ráfaga activo"
            else -> "Apunta al código QR"
        }

        Text(
            text = hintText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .background(
                    hintBg,
                    MaterialTheme.shapes.large
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )

        if (state is ScannerState.Success && !isBurstMode) {
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