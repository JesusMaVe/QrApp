# QR Scanner - Diseño Nativo Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rediseñar el scanner QR para que tenga un estilo nativo de Android con frame tradicional, colores Material Design 3, y animaciones fluidas.

**Architecture:** Modificar componentes existentes del scanner (ScanOverlay, ScannerScreen, ResultBottomSheet, Theme) para adoptar el estilo Android clásico con colores del sistema y animaciones Material.

**Tech Stack:** Jetpack Compose, Material 3, CameraX, ML Kit

---

## Task 1: Agregar Android Green Palette

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/ui/theme/Color.kt`

- [ ] **Step 1: Leer Color.kt actual**

```kotlin
// app/src/main/java/com/example/qrapplication/ui/theme/Color.kt
```

- [ ] **Step 2: Agregar Android Green palette**

```kotlin
// Android Green Palette
val AndroidGreen50 = Color(0xFFE8F5E9)
val AndroidGreen100 = Color(0xFFC8E6C9)
val AndroidGreen200 = Color(0xFFA5D6A7)
val AndroidGreen300 = Color(0xFF81C784)
val AndroidGreen400 = Color(0xFF66BB6A)
val AndroidGreen500 = Color(0xFF00C853)  // Primary
val AndroidGreen600 = Color(0xFF43A047)
val AndroidGreen700 = Color(0xFF388E3C)
val AndroidGreen800 = Color(0xFF2E7D32)
val AndroidGreen900 = Color(0xFF1B5E20)
val AndroidGreenA400 = Color(0xFF00E676)  // Bright accent
val AndroidGreenA700 = Color(0xFF00C853)

// Warning/Secondary
val AndroidOrange500 = Color(0xFFFF9800)
val AndroidOrange700 = Color(0xFFF57C00)

// Error
val AndroidRed500 = Color(0xFFF44336)
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/ui/theme/Color.kt
git commit -m "feat: add Android Green palette colors"
```

---

## Task 2: Actualizar Theme.kt con colores del sistema

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/ui/theme/Theme.kt:1-58`

- [ ] **Step 1: Leer Theme.kt actual**

```kotlin
// app/src/main/java/com/example/qrapplication/ui/theme/Theme.kt
```

- [ ] **Step 2: Agregar import de AndroidGreen**

```kotlin
import com.example.qrapplication.ui.theme.AndroidGreen500
import com.example.qrapplication.ui.theme.AndroidGreenA400
import com.example.qrapplication.ui.theme.AndroidOrange500
import com.example.qrapplication.ui.theme.AndroidRed500
```

- [ ] **Step 3: Actualizar LightColorScheme con colores Android**

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = AndroidGreen500,
    onPrimary = Color.White,
    primaryContainer = AndroidGreen50,
    onPrimaryContainer = AndroidGreen900,
    secondary = AndroidOrange500,
    onSecondary = Color.White,
    error = AndroidRed500,
    onError = Color.White,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)
```

- [ ] **Step 4: Actualizar DarkColorScheme**

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = AndroidGreenA400,
    onPrimary = Color.Black,
    primaryContainer = AndroidGreen700,
    onPrimaryContainer = AndroidGreen50,
    secondary = AndroidOrange500,
    onSecondary = Color.Black,
    error = AndroidRed500,
    onError = Color.White,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/ui/theme/Theme.kt
git commit -m "feat: use Android Green theme colors"
```

---

## Task 3: Rediseñar ScanOverlay con estilo Android

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/scanner/components/ScanOverlay.kt:1-62`

- [ ] **Step 1: Leer ScanOverlay.kt actual**

```kotlin
// app/src/main/java/com/example/qrapplication/scanner/components/ScanOverlay.kt
```

- [ ] **Step 2: Reescribir con frame estilo Android**

```kotlin
package com.example.qrapplication.scanner.components

import android.graphics.RectF
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.example.qrapplication.barcode.TrackingState
import com.example.qrapplication.ui.theme.AndroidGreen500
import com.example.qrapplication.ui.theme.AndroidGreenA400
import com.example.qrapplication.ui.theme.AndroidOrange500

@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    trackingState: TrackingState = TrackingState()
) {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Color based on state
    val frameColor = when {
        trackingState.isTracking && trackingState.isInFrame -> AndroidGreenA400  // Bright green when detected
        trackingState.isTracking -> AndroidOrange500  // Orange when tracking but not in frame
        else -> AndroidGreen500.copy(alpha = 0.6f)  // Dim green when idle
    }

    // Animate color changes
    val animatedColor by animateColorAsState(
        targetValue = frameColor,
        animationSpec = tween(150),
        label = "color"
    )

    val displayAlpha = if (trackingState.isTracking) pulseAlpha else 0.6f

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Centered frame (50% of smaller dimension)
            val frameSize = minOf(canvasWidth, canvasHeight) * 0.65f
            val frameLeft = (canvasWidth - frameSize) / 2
            val frameTop = (canvasHeight - frameSize) / 2
            
            val cornerRadius = 16.dp.toPx()
            val cornerLength = 28.dp.toPx()
            val strokeWidth = 3.dp.toPx()

            // Draw scan frame with rounded corners
            val path = Path().apply {
                // Top-left corner
                moveTo(frameLeft + cornerRadius, frameTop)
                lineTo(frameLeft, frameTop + cornerLength)
                moveTo(frameLeft, frameTop + cornerRadius)
                lineTo(frameLeft + cornerLength, frameTop)
                
                // Top-right corner  
                moveTo(frameLeft + frameSize - cornerRadius, frameTop)
                lineTo(frameLeft + frameSize, frameTop + cornerLength)
                moveTo(frameLeft + frameSize - cornerLength, frameTop)
                lineTo(frameLeft + frameSize, frameTop)
                
                // Bottom-left corner
                moveTo(frameLeft, frameTop + frameSize - cornerLength)
                lineTo(frameLeft + cornerRadius, frameTop + frameSize)
                moveTo(frameLeft, frameTop + frameSize - cornerRadius)
                lineTo(frameLeft + cornerLength, frameTop + frameSize)
                
                // Bottom-right corner
                moveTo(frameLeft + frameSize - cornerLength, frameTop + frameSize)
                lineTo(frameLeft + frameSize, frameTop + frameSize - cornerRadius)
                moveTo(frameLeft + frameSize - cornerRadius, frameTop + frameSize)
                lineTo(frameLeft + frameSize, frameTop + frameSize - cornerLength)
            }

            // Draw frame corners
            drawPath(
                path = path,
                color = animatedColor.copy(alpha = displayAlpha),
                style = Stroke(width = strokeWidth)
            )

            // Draw tracking rectangle if detected
            if (trackingState.isTracking && trackingState.normalizedRect != null) {
                val trackRect = trackingState.normalizedRect
                val trackLeft = trackRect.left * canvasWidth
                val trackTop = trackRect.top * canvasHeight
                val trackWidth = trackRect.width() * canvasWidth
                val trackHeight = trackRect.height() * canvasHeight

                // Fill tracking area
                drawRoundRect(
                    color = animatedColor.copy(alpha = displayAlpha * 0.15f),
                    topLeft = Offset(trackLeft, trackTop),
                    size = Size(trackWidth, trackHeight),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )

                // Draw tracking outline
                drawRoundRect(
                    color = animatedColor.copy(alpha = displayAlpha),
                    topLeft = Offset(trackLeft, trackTop),
                    size = Size(trackWidth, trackHeight),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/scanner/components/ScanOverlay.kt
git commit -m "feat: redesign scan overlay with Android style frame"
```

---

## Task 4: Actualizar ScannerScreen UI

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/scanner/ScannerScreen.kt:200-320`

- [ ] **Step 1: Leer ScannerScreen.kt sección ScannerContent**

```kotlin
// app/src/main/java/com/example/qrapplication/screens/scanner/ScannerScreen.kt
// Buscar: private fun ScannerContent
```

- [ ] **Step 2: Agregar imports necesarios**

```kotlin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
```

- [ ] **Step 3: Actualizar ScannerContent con gradiente y UI Android**

Reemplazar la función ScannerContent para incluir:
- Gradiente en la parte superior
- Botones flotantes con estilo Material
- Hint con fondo translúcido y blur

```kotlin
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
    onAction: (Barcode, ContentType) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            onBarcodeDetected = onBarcodeDetected,
            isFlashlightOn = isFlashOn
        )

        // Scan overlay
        ScanOverlay(trackingState = trackingState)

        // Top gradient for UI visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // Top action buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gallery Button
            IconButton(
                onClick = onLaunchGallery,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.15f),
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
                        else Color.White.copy(alpha = 0.15f),
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
                        Color.White.copy(alpha = 0.15f),
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

        // Hint text with translucent background
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    MaterialTheme.shapes.large
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )

        // Result BottomSheet
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
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/scanner/ScannerScreen.kt
git commit -m "feat: update scanner UI with Android Material style"
```

---

## Task 5: Mejorar ResultBottomSheet

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/scanner/components/ResultBottomSheet.kt:1-75`

- [ ] **Step 1: Leer ResultBottomSheet.kt**

```kotlin
// app/src/main/java/com/example/qrapplication/screens/scanner/components/ResultBottomSheet.kt
```

- [ ] **Step 2: Actualizar estilo con Material 3**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(
    contentType: ContentType,
    content: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Content type label
            Text(
                text = contentType.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Content value
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary action button
                Button(
                    onClick = onAction,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = contentType.actionLabel)
                }

                // Secondary dismiss button
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/scanner/components/ResultBottomSheet.kt
git commit -m "feat: improve ResultBottomSheet Material 3 styling"
```

---

## Task 6: Build y Verificar

**Files:**
- N/A

- [ ] **Step 1: Build debug APK**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Commit final**

```bash
git add -A
git commit -m "feat: redesign QR scanner with native Android styling"
```

---

## Resumen de tareas completadas

1. [ ] Android Green palette agregada
2. [ ] Theme actualizado con colores del sistema
3. [ ] ScanOverlay con frame estilo Android
4. [ ] ScannerScreen con UI mejoras
5. [ ] ResultBottomSheet con Material 3
6. [ ] Build exitoso y verificado