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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    // Infinite transition for pulse animation
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

    // Color based on tracking state
    val frameColor = when {
        trackingState.isTracking && trackingState.isInFrame -> AndroidGreenA400  // Bright green when in frame
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

            // Centered frame (65% of smaller dimension - Android classic style)
            val frameSize = minOf(canvasWidth, canvasHeight) * 0.65f
            val frameLeft = (canvasWidth - frameSize) / 2
            val frameTop = (canvasHeight - frameSize) / 2

            val cornerRadius = 16.dp.toPx()
            val cornerLength = 28.dp.toPx()
            val strokeWidth = 3.dp.toPx()

            // Draw frame corners using Path (Android classic style)
            val cornerPath = Path().apply {
                // Top-left corner
                moveTo(frameLeft, frameTop + cornerLength)
                lineTo(frameLeft, frameTop + cornerRadius)
                quadraticTo(frameLeft, frameTop + cornerRadius, frameLeft + cornerRadius, frameTop)
                lineTo(frameLeft + cornerLength, frameTop)
                
                // Top-right corner
                moveTo(frameLeft + frameSize - cornerLength, frameTop)
                lineTo(frameLeft + frameSize - cornerRadius, frameTop)
                quadraticTo(frameLeft + frameSize, frameTop + cornerRadius, frameLeft + frameSize, frameTop + cornerRadius)
                lineTo(frameLeft + frameSize, frameTop + cornerLength)
                
                // Bottom-left corner
                moveTo(frameLeft, frameTop + frameSize - cornerLength)
                lineTo(frameLeft, frameTop + frameSize - cornerRadius)
                quadraticTo(frameLeft + cornerRadius, frameTop + frameSize, frameLeft + cornerRadius, frameTop + frameSize)
                lineTo(frameLeft + cornerLength, frameTop + frameSize)
                
                // Bottom-right corner
                moveTo(frameLeft + frameSize - cornerLength, frameTop + frameSize)
                lineTo(frameLeft + frameSize - cornerRadius, frameTop + frameSize)
                quadraticTo(frameLeft + frameSize, frameTop + frameSize - cornerRadius, frameLeft + frameSize, frameTop + frameSize - cornerRadius)
                lineTo(frameLeft + frameSize, frameTop + frameSize - cornerLength)
            }

            // Draw frame corners
            drawPath(
                path = cornerPath,
                color = animatedColor.copy(alpha = displayAlpha),
                style = Stroke(width = strokeWidth)
            )

            // Draw rounded rectangle outline as secondary frame
            drawRoundRect(
                color = animatedColor.copy(alpha = displayAlpha * 0.3f),
                topLeft = Offset(frameLeft, frameTop),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw tracking rectangle if barcode detected
            if (trackingState.isTracking && trackingState.normalizedRect != null) {
                val trackRect = trackingState.normalizedRect
                val trackLeft = trackRect.left * canvasWidth
                val trackTop = trackRect.top * canvasHeight
                val trackWidth = trackRect.width() * canvasWidth
                val trackHeight = trackRect.height() * canvasHeight

                // Fill tracking area with semi-transparent color
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