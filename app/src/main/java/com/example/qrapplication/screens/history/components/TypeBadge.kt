package com.example.qrapplication.screens.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.qrapplication.model.ContentType

@Composable
fun TypeBadge(
    contentType: ContentType,
    modifier: Modifier = Modifier
) {
    val accentColor = ContentTypeColors.getAccentColor(contentType)
    val backgroundColor = accentColor.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getIconForType(contentType),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = contentType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

private fun getIconForType(type: ContentType): ImageVector {
    return when (type) {
        ContentType.URL -> Icons.Default.Link
        ContentType.EMAIL -> Icons.Default.Email
        ContentType.PHONE -> Icons.Default.Phone
        ContentType.TEXT -> Icons.Default.Search
        ContentType.PRODUCT -> Icons.Default.ShoppingCart
        ContentType.WIFI -> Icons.Default.Wifi
        ContentType.CONTACT -> Icons.Default.Person
        ContentType.UNKNOWN -> Icons.Default.Star
    }
}