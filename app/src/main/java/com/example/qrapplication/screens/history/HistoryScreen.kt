package com.example.qrapplication.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrapplication.data.ScanRepository
import com.example.qrapplication.model.QrFolder
import com.example.qrapplication.screens.history.components.FolderButton
import com.example.qrapplication.screens.history.components.FolderDialog
import com.example.qrapplication.screens.history.components.ScanListItem
import com.example.qrapplication.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: ScanRepository,
    onScanTap: (String, com.example.qrapplication.model.ContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(repository)
    )

    val scans by viewModel.scans.collectAsState()
    val folders by viewModel.folders.collectAsState()
    var showFolderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                actions = {
                    FolderButton(onClick = { showFolderDialog = true })
                }
            )
        }
    ) { paddingValues ->
        if (scans.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Star,
                title = "Sin escaneos aún",
                description = "Los códigos que escanees aparecerán aquí para consulta rápida",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(
                    items = scans,
                    key = { record -> record.id }
                ) { record ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteScan(record.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = { DismissBackground(dismissState) },
                        content = {
                            ScanListItem(
                                record = record,
                                onClick = { onScanTap(record.content, record.parsedContentType) }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showFolderDialog) {
        FolderDialog(
            folders = folders,
            onFolderSelected = { folder ->
                showFolderDialog = false
            },
            onCreateFolder = { name ->
                viewModel.createFolder(name)
            },
            onDismiss = { showFolderDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(dismissState: androidx.compose.material3.SwipeToDismissBoxState) {
    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
        Color.Red.copy(alpha = 0.7f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "Eliminar",
            color = Color.White
        )
    }
}

class HistoryViewModelFactory(
    private val repository: ScanRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(repository) as T
    }
}