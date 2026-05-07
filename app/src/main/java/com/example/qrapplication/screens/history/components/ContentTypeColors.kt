package com.example.qrapplication.screens.history.components

import androidx.compose.ui.graphics.Color
import com.example.qrapplication.model.ContentType

object ContentTypeColors {
    fun getBackgroundColor(type: ContentType): Color {
        return when (type) {
            ContentType.URL -> Color(0xFFE3F2FD)
            ContentType.EMAIL -> Color(0xFFE8F5E9)
            ContentType.PHONE -> Color(0xFFFFF3E0)
            ContentType.TEXT -> Color(0xFFF5F5F5)
            ContentType.PRODUCT -> Color(0xFFF3E5F5)
            ContentType.WIFI -> Color(0xFFE0F7FA)
            ContentType.CONTACT -> Color(0xFFFFFDE7)
            ContentType.UNKNOWN -> Color(0xFFFAFAFA)
        }
    }

    fun getBorderColor(type: ContentType): Color {
        return when (type) {
            ContentType.URL -> Color(0xFF90CAF9)
            ContentType.EMAIL -> Color(0xFFA5D6A7)
            ContentType.PHONE -> Color(0xFFFFCC80)
            ContentType.TEXT -> Color(0xFFE0E0E0)
            ContentType.PRODUCT -> Color(0xFFCE93D8)
            ContentType.WIFI -> Color(0xFF80DEEA)
            ContentType.CONTACT -> Color(0xFFFFF59D)
            ContentType.UNKNOWN -> Color(0xFFE0E0E0)
        }
    }

    fun getAccentColor(type: ContentType): Color {
        return when (type) {
            ContentType.URL -> Color(0xFF1976D2)
            ContentType.EMAIL -> Color(0xFF388E3C)
            ContentType.PHONE -> Color(0xFFF57C00)
            ContentType.TEXT -> Color(0xFF616161)
            ContentType.PRODUCT -> Color(0xFF7B1FA2)
            ContentType.WIFI -> Color(0xFF0097A7)
            ContentType.CONTACT -> Color(0xFFFBC02D)
            ContentType.UNKNOWN -> Color(0xFF757575)
        }
    }
}