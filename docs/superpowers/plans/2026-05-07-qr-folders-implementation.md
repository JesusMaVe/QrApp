# QR Folders - Plan de Implementación

> **Para agentes:** Implementar tarea por tarea.

**Goal:** Sistema de carpetas para organizar códigos QR escaneados.

**Architecture:** Nuevo modelo QrFolder, actualización de ScanRecord con folderId, nuevos componentes UI, actualización de repositorio y flujos de scanner.

**Tech Stack:** Jetpack Compose, DataStore, Kotlinx Serialization

---

### Task 1: Actualizar modelo ScanRecord con folderId

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/model/ScanRecord.kt`

- [ ] **Step 1: Agregar folderId al modelo**

```kotlin
package com.example.qrapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val contentType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val folderId: String? = null
) {
    val parsedContentType: ContentType
        get() = runCatching { ContentType.valueOf(contentType) }.getOrDefault(ContentType.UNKNOWN)
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/model/ScanRecord.kt
git commit -m "feat: add folderId to ScanRecord for folder organization"
```

---

### Task 2: Crear modelo QrFolder

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/model/QrFolder.kt`

- [ ] **Step 1: Escribir el modelo**

```kotlin
package com.example.qrapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class QrFolder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/model/QrFolder.kt
git commit -m "feat: add QrFolder model"
```

---

### Task 3: Actualizar ScanRepository para manejar carpetas

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/data/ScanRepository.kt`

- [ ] **Step 1: Agregar manejo de carpetas al repository**

Agregar al inicio de la clase:
```kotlin
    private val foldersKey = stringPreferencesKey("qr_folders")
    val folders: StateFlow<List<QrFolder>> = _foldersFlow.asStateFlow()
    private val _foldersFlow = MutableStateFlow<List<QrFolder>>(emptyList())
```

En init, agregar después del collect de scans:
```kotlin
        scope.launch {
            dataStore.data.collect { preferences ->
                val foldersJson = preferences[foldersKey] ?: "[]"
                val loadedFolders = runCatching {
                    Json.decodeFromString<List<QrFolder>>(foldersJson)
                }.getOrDefault(emptyList())
                _foldersFlow.value = loadedFolders
            }
        }
```

Agregar métodos:
```kotlin
    suspend fun createFolder(name: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[foldersKey] ?: "[]"
            val currentList = runCatching {
                Json.decodeFromString<List<QrFolder>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            currentList.add(QrFolder(name = name))

            preferences[foldersKey] = Json.encodeToString(currentList)
            _foldersFlow.value = currentList
        }
    }

    suspend fun deleteFolder(id: String) {
        dataStore.edit { preferences ->
            val foldersJson = preferences[foldersKey] ?: "[]"
            val foldersList = runCatching {
                Json.decodeFromString<List<QrFolder>>(foldersJson)
            }.getOrDefault(emptyList()).toMutableList()

            foldersList.removeAll { it.id == id }
            preferences[foldersKey] = Json.encodeToString(foldersList)
            _foldersFlow.value = foldersList
        }

        // Mover QR de esta carpeta a "sin carpeta"
        val currentList = cacheReference.get()
        val updatedList = currentList.map { scan ->
            if (scan.folderId == id) scan.copy(folderId = null) else scan
        }
        cacheReference.set(updatedList)
        _scansFlow.value = updatedList

        dataStore.edit { preferences ->
            preferences[historyKey] = Json.encodeToString(updatedList)
        }
    }

    suspend fun moveScanToFolder(scanId: String, folderId: String?) {
        dataStore.edit { preferences ->
            val currentList = cacheReference.get().ifEmpty {
                preferences[historyKey]?.let { json ->
                    runCatching {
                        Json.decodeFromString<List<ScanRecord>>(json)
                    }.getOrDefault(emptyList())
                } ?: emptyList()
            }

            val updatedList = currentList.map { scan ->
                if (scan.id == scanId) scan.copy(folderId = folderId) else scan
            }

            cacheReference.set(updatedList)
            rebuildContentIndex(updatedList)
            _scansFlow.value = updatedList

            preferences[historyKey] = Json.encodeToString(updatedList)
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/data/ScanRepository.kt
git commit -m "feat: add folder management to ScanRepository"
```

---

### Task 4: Crear FolderRepository para mejor separación

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/data/FolderRepository.kt`

- [ ] **Step 1: Separar lógica de carpetas**

```kotlin
package com.example.qrapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qrapplication.model.QrFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.folderDataStore: DataStore<Preferences> by preferencesDataStore(name = "qr_folders")

class FolderRepository(context: Context) {

    private val dataStore = context.folderDataStore
    private val json = Json { ignoreUnknownKeys = true }
    private val foldersKey = stringPreferencesKey("folders_list")

    val folders: Flow<List<QrFolder>> = dataStore.data.map { preferences ->
        val jsonStr = preferences[foldersKey] ?: "[]"
        runCatching {
            json.decodeFromString<List<QrFolder>>(jsonStr)
        }.getOrDefault(emptyList())
    }

    suspend fun createFolder(name: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[foldersKey] ?: "[]"
            val currentList = runCatching {
                json.decodeFromString<List<QrFolder>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            currentList.add(QrFolder(name = name))
            preferences[foldersKey] = json.encodeToString(currentList)
        }
    }

    suspend fun deleteFolder(id: String) {
        dataStore.edit { preferences ->
            val currentJson = preferences[foldersKey] ?: "[]"
            val currentList = runCatching {
                json.decodeFromString<List<QrFolder>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            currentList.removeAll { it.id == id }
            preferences[foldersKey] = json.encodeToString(currentList)
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/data/FolderRepository.kt
git commit -m "feat: add FolderRepository for folder management"
```

---

### Task 5: Crear componentes UI - FolderDialog

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/history/components/FolderDialog.kt`

- [ ] **Step 1: Escribir FolderDialog**

```kotlin
package com.example.qrapplication.screens.history.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qrapplication.model.QrFolder

@Composable
fun FolderDialog(
    folders: List<QrFolder>,
    onFolderSelected: (QrFolder?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateField by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar carpeta") },
        text = {
            Column {
                // Opción "Sin carpeta"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFolderSelected(null) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sin carpeta",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Lista de carpetas
                folders.forEach { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFolderSelected(folder) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Crear nueva carpeta
                if (showCreateField) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newFolderName.isNotBlank()) {
                                    onCreateFolder(newFolderName)
                                    newFolderName = ""
                                    showCreateField = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Crear")
                        }
                    }
                } else {
                    TextButton(
                        onClick = { showCreateField = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crear carpeta")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/components/FolderDialog.kt
git commit -m "feat: add FolderDialog component"
```

---

### Task 6: Crear FolderButton para HistoryScreen

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/history/components/FolderButton.kt`

- [ ] **Step 1: Escribir FolderButton**

```kotlin
package com.example.qrapplication.screens.history.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FolderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "Carpetas",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/components/FolderButton.kt
git commit -m "feat: add FolderButton component"
```

---

### Task 7: Crear BurstReviewScreen

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/scanner/BurstReviewScreen.kt`

- [ ] **Step 1: Escribir BurstReviewScreen**

```kotlin
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { showFolderSelector = false }
                        ) {
                            Text("Sin carpeta")
                        }
                    }

                    folders.forEach { folder ->
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onAssignToFolder(selectedIds.toList(), folder)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(folder.name)
                        }
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/scanner/BurstReviewScreen.kt
git commit -m "feat: add BurstReviewScreen for reviewing burst mode scans"
```

---

### Task 8: Actualizar HistoryViewModel para manejar carpetas

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/history/HistoryViewModel.kt`

- [ ] **Step 1: Agregar métodos de carpeta**

```kotlin
    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name)
        }
    }

    fun moveScanToFolder(scanId: String, folderId: String?) {
        viewModelScope.launch {
            repository.moveScanToFolder(scanId, folderId)
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/HistoryViewModel.kt
git commit -m "feat: add folder methods to HistoryViewModel"
```

---

### Task 9: Actualizar HistoryScreen con botón de carpetas

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt`

- [ ] **Step 1: Agregar TopAppBar con FolderButton y mostrar FolderDialog**

Agregar imports:
```kotlin
import androidx.compose.material.icons.filled.Folder
import com.example.qrapplication.screens.history.components.FolderButton
import com.example.qrapplication.screens.history.components.FolderDialog
import com.example.qrapplication.model.QrFolder
```

En HistoryScreen, agregar estado y modificar:
```kotlin
    val folders by viewModel.folders.collectAsState()
    var showFolderDialog by remember { mutableStateOf(false) }

    // En el Scaffold, agregar FolderButton al TopAppBar
    // Y mostrar FolderDialog cuando showFolderDialog sea true
```

Modificar el TopAppBar para incluir el botón de carpetas y agregar el diálogo después del contenido.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt
git commit -m "feat: add folder button and dialog to HistoryScreen"
```

---

### Task 10: Actualizar BarcodeScannerViewModel para modo ráfaga con carpetas

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/barcode/BarcodeScannerViewModel.kt`

- [ ] **Step 1: Agregar estado para burst review**

```kotlin
    private val _burstScans = MutableStateFlow<List<ScannedBurstItem>>(emptyList())
    val burstScans: StateFlow<List<ScannedBurstItem>> = _burstScans.asStateFlow()

    private val _showBurstReview = MutableStateFlow(false)
    val showBurstReview: StateFlow<Boolean> = _showBurstReview.asStateFlow()
```

Agregar data class:
```kotlin
data class ScannedBurstItem(
    val content: String,
    val contentType: ContentType,
    val timestamp: Long = System.currentTimeMillis()
)
```

En onBarcodeDetected, cuando está en burst mode y detecta un QR válido:
```kotlin
        if (_isBurstMode.value && detectedBarcode.isInFrame) {
            _burstScans.update { current ->
                val newItem = ScannedBurstItem(
                    content = rawValue,
                    contentType = contentType
                )
                listOf(newItem) + current
            }
        }
```

Agregar método para salir del modo ráfaga:
```kotlin
    fun finishBurstMode() {
        _showBurstReview.value = true
    }

    fun clearBurstScans() {
        _burstScans.value = emptyList()
        _showBurstReview.value = false
    }
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/barcode/BarcodeScannerViewModel.kt
git commit -m "feat: add burst scan tracking to BarcodeScannerViewModel"
```

---

### Task 11: Actualizar ScannerScreen para folder selection y burst review

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/scanner/ScannerScreen.kt`

- [ ] **Step 1: Actualizar flujo de scanner para carpetas**

1. Agregar estado para folder selection
2. Modificar ResultBottomSheet para incluir opción "Guardar en carpeta"
3. Mostrar FolderDialog cuando se seleccione "Guardar en carpeta"
4. Después de guardar, ejecutar acción original

Para burst mode:
1. Cuando se desactiva burst mode, verificar si hay scans pendientes
2. Si hay scans, navegar a BurstReviewScreen
3. En BurstReviewScreen, asignar carpeta y guardar todos los QR

Agregar imports necesarios y modificar el flujo.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/scanner/ScannerScreen.kt
git commit -m "feat: integrate folder selection into scanner flow"
```

---

### Task 12: Build y verificar

**Files:**
- Build: `./gradlew assembleDebug`

- [ ] **Step 1: Compilar el proyecto**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Commit final**

```bash
git add -A
git commit -m "feat: complete QR folders system"
```

---

## Verificación de Cobertura del Spec

| Spec Requirement | Status |
|-----------------|--------|
| Botón en HistoryScreen | ✓ Task 6, 9 |
| Flujo normal con folder selection | ✓ Task 11 |
| Flujo modo ráfaga con BurstReviewScreen | ✓ Task 7, 10, 11 |
| QrFolder modelo | ✓ Task 2 |
| ScanRecord con folderId | ✓ Task 1 |
| FolderRepository | ✓ Task 4 |
| FolderDialog | ✓ Task 5 |
| Estilo corporativo | ✓ Tasks 5, 6, 7 |