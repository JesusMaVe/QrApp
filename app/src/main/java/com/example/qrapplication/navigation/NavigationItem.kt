package com.example.qrapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Scanner : NavigationItem(
        route = "scanner",
        label = "Escáner",
        icon = Icons.Default.QrCodeScanner
    )

    data object History : NavigationItem(
        route = "history",
        label = "Historial",
        icon = Icons.Default.History
    )
}
