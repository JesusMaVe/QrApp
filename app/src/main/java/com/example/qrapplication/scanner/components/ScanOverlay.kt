package com.example.qrapplication.scanner.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier,
    frameSize: Dp = 250.dp,
    cornerLength: Dp = 30.dp,
    borderWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineOffset"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val scrimColor = Color.Black.copy(alpha = 0.5f)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameSizePx = frameSize.toPx()
            val cornerLengthPx = cornerLength.toPx()
            val borderWidthPx = borderWidth.toPx()

            val frameLeft = (size.width - frameSizePx) / 2
            val frameTop = (size.height - frameSizePx) / 2
            val frameRight = frameLeft + frameSizePx
            val frameBottom = frameTop + frameSizePx

            drawRect(
                color = scrimColor,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, frameTop)
            )
            drawRect(
                color = scrimColor,
                topLeft = Offset(0f, frameBottom),
                size = Size(size.width, size.height - frameBottom)
            )
            drawRect(
                color = scrimColor,
                topLeft = Offset(0f, frameTop),
                size = Size(frameLeft, frameSizePx)
            )
            drawRect(
                color = scrimColor,
                topLeft = Offset(frameRight, frameTop),
                size = Size(size.width - frameRight, frameSizePx)
            )

            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(frameLeft, frameTop),
                size = Size(frameSizePx, frameSizePx),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = borderWidthPx)
            )

            val cornerColor = primaryColor
            val cornerStroke = Stroke(width = 6.dp.toPx())

            drawLine(
                color = cornerColor,
                start = Offset(frameLeft, frameTop + cornerLengthPx),
                end = Offset(frameLeft, frameTop),
                strokeWidth = cornerStroke.width
            )
            drawLine(
                color = cornerColor,
                start = Offset(frameLeft, frameTop),
                end = Offset(frameLeft + cornerLengthPx, frameTop),
                strokeWidth = cornerStroke.width
            )

            drawLine(
                color = cornerColor,
                start = Offset(frameRight - cornerLengthPx, frameTop),
                end = Offset(frameRight, frameTop),
                strokeWidth = cornerStroke.width
            )
            drawLine(
                color = cornerColor,
                start = Offset(frameRight, frameTop),
                end = Offset(frameRight, frameTop + cornerLengthPx),
                strokeWidth = cornerStroke.width
            )

            drawLine(
                color = cornerColor,
                start = Offset(frameLeft, frameBottom - cornerLengthPx),
                end = Offset(frameLeft, frameBottom),
                strokeWidth = cornerStroke.width
            )
            drawLine(
                color = cornerColor,
                start = Offset(frameLeft, frameBottom),
                end = Offset(frameLeft + cornerLengthPx, frameBottom),
                strokeWidth = cornerStroke.width
            )

            drawLine(
                color = cornerColor,
                start = Offset(frameRight - cornerLengthPx, frameBottom),
                end = Offset(frameRight, frameBottom),
                strokeWidth = cornerStroke.width
            )
            drawLine(
                color = cornerColor,
                start = Offset(frameRight, frameBottom - cornerLengthPx),
                end = Offset(frameRight, frameBottom),
                strokeWidth = cornerStroke.width
            )

            val scanLineY = frameTop + (frameSizePx * scanLineOffset)
            drawLine(
                color = primaryColor.copy(alpha = 0.8f),
                start = Offset(frameLeft + 8.dp.toPx(), scanLineY),
                end = Offset(frameRight - 8.dp.toPx(), scanLineY),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}
