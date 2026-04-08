package com.example.qrapplication.scanner.components

import android.graphics.RectF
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.qrapplication.barcode.TrackingState

@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    trackingState: TrackingState = TrackingState()
) {
    val trackingAlpha by animateFloatAsState(
        targetValue = if (trackingState.isTracking) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "trackingAlpha"
    )

    val trackingColor = if (trackingState.isInFrame) Color.Green else Color.Red

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (trackingState.isTracking && trackingState.normalizedRect != null) {
                val trackRect = trackingState.normalizedRect
                
                val trackLeft = trackRect.left * size.width
                val trackTop = trackRect.top * size.height
                val trackWidth = trackRect.width() * size.width
                val trackHeight = trackRect.height() * size.height

                // Draw tracking rectangle
                drawRoundRect(
                    color = trackingColor.copy(alpha = trackingAlpha),
                    topLeft = Offset(trackLeft, trackTop),
                    size = Size(trackWidth, trackHeight),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Fill with semi-transparent
                drawRoundRect(
                    color = trackingColor.copy(alpha = trackingAlpha * 0.15f),
                    topLeft = Offset(trackLeft, trackTop),
                    size = Size(trackWidth, trackHeight),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
        }
    }
}