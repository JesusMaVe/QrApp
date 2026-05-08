package com.example.qrapplication.screens.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.qrapplication.model.QrFolder

data class ScannedItem(
    val id: String,
    val content: String,
    val contentType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurstReviewScreen(
    scannedItems: List<ScannedItem>,
    folders: List<QrFolder>,
    onAssignToFolder: (List<String>, QrFolder?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIds = remember { mutableStateListOf<String>() }
    var showFolderSelector by remember { mutableStateOf(false) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    selectedIds.addAll(scannedItems.map { it.id })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisar escaneos (${scannedItems.size})") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Selecciona los QR para asignar a una carpeta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scannedItems) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedIds.contains(item.id),
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(item.id)
                                    else selectedIds.remove(item.id)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.contentType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showFolderSelector) {
                Column {
                    Text(
                        text = "Seleccionar carpeta:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            onAssignToFolder(selectedIds.toList(), null)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sin carpeta")
                    }

                    folders.forEach { folder ->
                        TextButton(
                            onClick = {
                                onAssignToFolder(selectedIds.toList(), folder)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(folder.name)
                        }
                    }

                    if (showCreateFolder) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newFolderName,
                                onValueChange = { newFolderName = it },
                                label = { Text("Nueva carpeta") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            TextButton(
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        // Create folder and assign
                                        onAssignToFolder(selectedIds.toList(), QrFolder(name = newFolderName))
                                        onDismiss()
                                    }
                                }
                            ) {
                                Text("Crear")
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { showCreateFolder = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Crear nueva carpeta")
                        }
                    }

                    TextButton(
                        onClick = { showFolderSelector = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onAssignToFolder(selectedIds.toList(), null)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sin carpeta")
                    }

                    Button(
                        onClick = { showFolderSelector = true },
                        modifier = Modifier.weight(1f),
                        enabled = selectedIds.isNotEmpty()
                    ) {
                        Text("Asignar carpeta")
                    }
                }
            }
        }
    }
}