package com.example.qrapplication.screens.history.components

import androidx.compose.ui.graphics.Color
import com.example.qrapplication.model.ContentType
import com.example.qrapplication.ui.theme.ContentTypeColorsDark
import com.example.qrapplication.ui.theme.ContentTypeColorsLight

object ContentTypeColors {

    fun getBackgroundColor(type: ContentType, isDark: Boolean): Color {
        return if (isDark) {
            when (type) {
                ContentType.URL -> ContentTypeColorsDark.URL_BG
                ContentType.EMAIL -> ContentTypeColorsDark.EMAIL_BG
                ContentType.PHONE -> ContentTypeColorsDark.PHONE_BG
                ContentType.TEXT -> ContentTypeColorsDark.TEXT_BG
                ContentType.PRODUCT -> ContentTypeColorsDark.PRODUCT_BG
                ContentType.WIFI -> ContentTypeColorsDark.WIFI_BG
                ContentType.CONTACT -> ContentTypeColorsDark.CONTACT_BG
                ContentType.UNKNOWN -> ContentTypeColorsDark.UNKNOWN_BG
            }
        } else {
            when (type) {
                ContentType.URL -> ContentTypeColorsLight.URL_BG
                ContentType.EMAIL -> ContentTypeColorsLight.EMAIL_BG
                ContentType.PHONE -> ContentTypeColorsLight.PHONE_BG
                ContentType.TEXT -> ContentTypeColorsLight.TEXT_BG
                ContentType.PRODUCT -> ContentTypeColorsLight.PRODUCT_BG
                ContentType.WIFI -> ContentTypeColorsLight.WIFI_BG
                ContentType.CONTACT -> ContentTypeColorsLight.CONTACT_BG
                ContentType.UNKNOWN -> ContentTypeColorsLight.UNKNOWN_BG
            }
        }
    }

    fun getBorderColor(type: ContentType, isDark: Boolean): Color {
        return if (isDark) {
            when (type) {
                ContentType.URL -> ContentTypeColorsDark.URL_BORDER
                ContentType.EMAIL -> ContentTypeColorsDark.EMAIL_BORDER
                ContentType.PHONE -> ContentTypeColorsDark.PHONE_BORDER
                ContentType.TEXT -> ContentTypeColorsDark.TEXT_BORDER
                ContentType.PRODUCT -> ContentTypeColorsDark.PRODUCT_BORDER
                ContentType.WIFI -> ContentTypeColorsDark.WIFI_BORDER
                ContentType.CONTACT -> ContentTypeColorsDark.CONTACT_BORDER
                ContentType.UNKNOWN -> ContentTypeColorsDark.UNKNOWN_BORDER
            }
        } else {
            when (type) {
                ContentType.URL -> ContentTypeColorsLight.URL_BORDER
                ContentType.EMAIL -> ContentTypeColorsLight.EMAIL_BORDER
                ContentType.PHONE -> ContentTypeColorsLight.PHONE_BORDER
                ContentType.TEXT -> ContentTypeColorsLight.TEXT_BORDER
                ContentType.PRODUCT -> ContentTypeColorsLight.PRODUCT_BORDER
                ContentType.WIFI -> ContentTypeColorsLight.WIFI_BORDER
                ContentType.CONTACT -> ContentTypeColorsLight.CONTACT_BORDER
                ContentType.UNKNOWN -> ContentTypeColorsLight.UNKNOWN_BORDER
            }
        }
    }

    fun getAccentColor(type: ContentType, isDark: Boolean): Color {
        return if (isDark) {
            when (type) {
                ContentType.URL -> ContentTypeColorsDark.URL_ACCENT
                ContentType.EMAIL -> ContentTypeColorsDark.EMAIL_ACCENT
                ContentType.PHONE -> ContentTypeColorsDark.PHONE_ACCENT
                ContentType.TEXT -> ContentTypeColorsDark.TEXT_ACCENT
                ContentType.PRODUCT -> ContentTypeColorsDark.PRODUCT_ACCENT
                ContentType.WIFI -> ContentTypeColorsDark.WIFI_ACCENT
                ContentType.CONTACT -> ContentTypeColorsDark.CONTACT_ACCENT
                ContentType.UNKNOWN -> ContentTypeColorsDark.UNKNOWN_ACCENT
            }
        } else {
            when (type) {
                ContentType.URL -> ContentTypeColorsLight.URL_ACCENT
                ContentType.EMAIL -> ContentTypeColorsLight.EMAIL_ACCENT
                ContentType.PHONE -> ContentTypeColorsLight.PHONE_ACCENT
                ContentType.TEXT -> ContentTypeColorsLight.TEXT_ACCENT
                ContentType.PRODUCT -> ContentTypeColorsLight.PRODUCT_ACCENT
                ContentType.WIFI -> ContentTypeColorsLight.WIFI_ACCENT
                ContentType.CONTACT -> ContentTypeColorsLight.CONTACT_ACCENT
                ContentType.UNKNOWN -> ContentTypeColorsLight.UNKNOWN_ACCENT
            }
        }
    }
}
