package com.example.qrapplication.screens.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.qrapplication.model.ContentType
import com.example.qrapplication.model.ScanRecord
import com.example.qrapplication.util.TimeFormatter

@Composable
fun ScanListItem(
    record: ScanRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ContentTypeColors.getBackgroundColor(record.parsedContentType)
    val borderColor = ContentTypeColors.getBorderColor(record.parsedContentType)
    val accentColor = ContentTypeColors.getAccentColor(record.parsedContentType)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = getIconForType(record.parsedContentType),
                contentDescription = record.parsedContentType.displayName,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TypeBadge(contentType = record.parsedContentType)
                }

                Text(
                    text = TimeFormatter.formatRelative(record.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun getIconForType(type: ContentType): ImageVector {
    return when (type) {
        ContentType.URL -> Icons.Default.Star
        ContentType.EMAIL -> Icons.Default.Email
        ContentType.PHONE -> Icons.Default.Phone
        ContentType.TEXT -> Icons.Default.Search
        ContentType.PRODUCT -> Icons.Default.ShoppingCart
        ContentType.WIFI -> Icons.Default.Star
        ContentType.CONTACT -> Icons.Default.Search
        ContentType.UNKNOWN -> Icons.Default.Search
    }
}