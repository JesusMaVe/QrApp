package com.example.qrapplication.screens.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.qrapplication.ui.components.EmptyState

@Composable
fun HistoryScreen() {
    EmptyState(
        icon = Icons.Default.Star,
        title = "Sin escaneos aún",
        description = "Los códigos que escanees aparecerán aquí para consulta rápida",
        modifier = Modifier.fillMaxSize()
    )
}
