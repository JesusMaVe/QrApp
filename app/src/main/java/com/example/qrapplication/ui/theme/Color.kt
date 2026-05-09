package com.example.qrapplication.ui.theme

import androidx.compose.ui.graphics.Color

// === Existing Purple Palette ===
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// === Android Green Palette (Material Design 3) ===
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

// === Android Orange (Warning/Secondary) ===
val AndroidOrange500 = Color(0xFFFF9800)
val AndroidOrange700 = Color(0xFFF57C00)

// === Android Red (Error) ===
val AndroidRed500 = Color(0xFFF44336)

// === ContentType Colors - Light Mode ===
object ContentTypeColorsLight {
    val URL_BG = Color(0xFFE3F2FD)
    val URL_BORDER = Color(0xFF90CAF9)
    val URL_ACCENT = Color(0xFF1976D2)

    val EMAIL_BG = Color(0xFFE8F5E9)
    val EMAIL_BORDER = Color(0xFFA5D6A7)
    val EMAIL_ACCENT = Color(0xFF388E3C)

    val PHONE_BG = Color(0xFFFFF3E0)
    val PHONE_BORDER = Color(0xFFFFCC80)
    val PHONE_ACCENT = Color(0xFFF57C00)

    val TEXT_BG = Color(0xFFF5F5F5)
    val TEXT_BORDER = Color(0xFFE0E0E0)
    val TEXT_ACCENT = Color(0xFF616161)

    val PRODUCT_BG = Color(0xFFF3E5F5)
    val PRODUCT_BORDER = Color(0xFFCE93D8)
    val PRODUCT_ACCENT = Color(0xFF7B1FA2)

    val WIFI_BG = Color(0xFFE0F7FA)
    val WIFI_BORDER = Color(0xFF80DEEA)
    val WIFI_ACCENT = Color(0xFF0097A7)

    val CONTACT_BG = Color(0xFFFFFDE7)
    val CONTACT_BORDER = Color(0xFFFFF59D)
    val CONTACT_ACCENT = Color(0xFFFBC02D)

    val UNKNOWN_BG = Color(0xFFFAFAFA)
    val UNKNOWN_BORDER = Color(0xFFE0E0E0)
    val UNKNOWN_ACCENT = Color(0xFF757575)
}

// === ContentType Colors - Dark Mode ===
object ContentTypeColorsDark {
    val URL_BG = Color(0xFF1A237E).copy(alpha = 0.3f)
    val URL_BORDER = Color(0xFF42A5F5)
    val URL_ACCENT = Color(0xFF64B5F6)

    val EMAIL_BG = Color(0xFF1B5E20).copy(alpha = 0.3f)
    val EMAIL_BORDER = Color(0xFF66BB6A)
    val EMAIL_ACCENT = Color(0xFF81C784)

    val PHONE_BG = Color(0xFFE65100).copy(alpha = 0.3f)
    val PHONE_BORDER = Color(0xFFFFB74D)
    val PHONE_ACCENT = Color(0xFFFFCC80)

    val TEXT_BG = Color(0xFF424242).copy(alpha = 0.3f)
    val TEXT_BORDER = Color(0xFF9E9E9E)
    val TEXT_ACCENT = Color(0xFFBDBDBD)

    val PRODUCT_BG = Color(0xFF4A148C).copy(alpha = 0.3f)
    val PRODUCT_BORDER = Color(0xFFBA68C8)
    val PRODUCT_ACCENT = Color(0xFFCE93D8)

    val WIFI_BG = Color(0xFF006064).copy(alpha = 0.3f)
    val WIFI_BORDER = Color(0xFF4DD0E1)
    val WIFI_ACCENT = Color(0xFF80DEEA)

    val CONTACT_BG = Color(0xFFF57F17).copy(alpha = 0.2f)
    val CONTACT_BORDER = Color(0xFFFFD54F)
    val CONTACT_ACCENT = Color(0xFFFFF59D)

    val UNKNOWN_BG = Color(0xFF424242).copy(alpha = 0.3f)
    val UNKNOWN_BORDER = Color(0xFF757575)
    val UNKNOWN_ACCENT = Color(0xFF9E9E9E)
}

// === Scanner UI Colors ===
object ScannerColors {
    val OverlayDark = Color(0xFF000000)
    val HintBgLight = Color(0xCC000000)
    val HintBgDark = Color(0xE6000000)
    val IconButtonBgLight = Color(0x26FFFFFF)
    val IconButtonBgDark = Color(0x33FFFFFF)
}
