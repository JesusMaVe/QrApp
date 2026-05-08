package com.example.qrapplication.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrapplication.barcode.HapticManager
import com.example.qrapplication.data.ScanRepository
import com.example.qrapplication.model.ScanRecord
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

    val context = LocalContext.current
    val scans by viewModel.scans.collectAsState()
    val folders by viewModel.folders.collectAsState()
    var showFolderDialog by remember { mutableStateOf(false) }

    // Estado del tab seleccionado (índice, 0 = Todas)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Crear un mapa para búsqueda rápida de carpetas por ID
    val folderMap = remember(folders) {
        folders.associateBy { it.id }
    }

    // Computar tabs dinámicamente
    val tabs = remember(folders) {
        buildList {
            add("Todas")
            add("Sin carpeta")
            // Agregar carpetas (máximo 3 adicionales para no exceder 5)
            val visibleFolders = folders.take(3)
            addAll(visibleFolders.map { it.name })
            // Si hay más carpetas, agregar indicador
            if (folders.size > 3) {
                add("+${folders.size - 3}")
            }
        }
    }

    // Computar QRs filtrados según tab
    val filteredScans = remember(scans, selectedTabIndex, folders) {
        when (selectedTabIndex) {
            0 -> scans // Todas
            1 -> scans.filter { it.folderId == null } // Sin carpeta
            else -> {
                val folderIndex = selectedTabIndex - 2
                if (folderIndex < folders.size) {
                    val folder = folders[folderIndex]
                    scans.filter { it.folderId == folder.id }
                } else {
                    // "+X" - mostrar todas
                    scans
                }
            }
        }
    }

    // Estado para mover QR a carpeta (long press)
    var showMoveDialogFor by remember { mutableStateOf<ScanRecord?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Historial") },
                    actions = {
                        FolderButton(onClick = { showFolderDialog = true })
                    }
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        val count = when (index) {
                            0 -> null // Todas - sin count
                            1 -> scans.count { it.folderId == null }.takeIf { it > 0 }
                            else -> {
                                val folderIndex = index - 2
                                if (folderIndex < folders.size) {
                                    scans.count { it.folderId == folders[folderIndex].id }.takeIf { it > 0 }
                                } else null
                            }
                        }
                        val tabTitle = if (count != null) "$title ($count)" else title

                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                if (title.startsWith("+")) {
                                    // Abrir diálogo de selección
                                    showFolderDialog = true
                                } else {
                                    selectedTabIndex = index
                                }
                            },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (filteredScans.isEmpty()) {
            val emptyTitle = when (selectedTabIndex) {
                0 -> "Sin escaneos aún"
                1 -> "No hay QRs sin carpeta"
                else -> "Carpeta vacía"
            }
            val emptyDesc = when (selectedTabIndex) {
                0 -> "Los códigos que escanees aparecerán aquí para consulta rápida"
                1 -> "Los QRs sin carpeta aparecerán aquí"
                else -> "Mueve QRs a esta carpeta con long press"
            }
            EmptyState(
                icon = Icons.Default.Star,
                title = emptyTitle,
                description = emptyDesc,
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
                    items = filteredScans,
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
                                folderName = record.folderId?.let { folderMap[it]?.name },
                                onClick = { onScanTap(record.content, record.parsedContentType) },
                                onLongClick = {
                                    HapticManager.vibrate(context)
                                    showMoveDialogFor = record
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    // Diálogo para mover QR a carpeta (long press)
    if (showMoveDialogFor != null) {
        FolderDialog(
            folders = folders,
            onFolderSelected = { folder ->
                viewModel.moveScanToFolder(showMoveDialogFor!!.id, folder?.id)
                showMoveDialogFor = null
            },
            onCreateFolder = { name ->
                viewModel.createFolder(name)
            },
            onDismiss = { showMoveDialogFor = null }
        )
    }

    // Diálogo original para gestión de carpetas
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