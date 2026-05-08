# Filtrado por Carpetas - Plan de Implementación

> **Para agentes:** Usar ejecución paso a paso. Tasks usan checkbox (`- [ ]`) para seguimiento.

**Goal:** Implementar tabs horizontales para filtrar QRs por carpeta y long press para mover QRs.

**Architecture:** ScrollableTabRow de Material 3 para tabs, filtro en memoria basado en folderId, FolderDialog existente reutilizado.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, StateFlow

---

## Archivos a Modificar

| Archivo | Responsabilidad |
|---------|-----------------|
| `screens/history/HistoryScreen.kt` | Tabs, estado de filtro, lógica de filtrado |
| `screens/history/components/ScanListItem.kt` | Long press handler |
| `barcode/HapticManager.kt` | Reutilizar para feedback táctil |

---

## Task 1: Modificar ScanListItem para Long Press

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/history/components/ScanListItem.kt`

- [ ] **Step 1: Agregar imports necesarios**

Agregar al inicio del archivo:
```kotlin
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberTransformableState
import androidx.compose.foundation.combinedClickable
```

- [ ] **Step 2: Agregar parámetro onLongClick**

Modificar la firma de la función:
```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScanListItem(
    record: ScanRecord,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    folderName: String? = null,
    modifier: Modifier = Modifier
) {
```

- [ ] **Step 3: Aplicar combinedClickable al Surface**

Cambiar el `clickable` del Surface a `combinedClickable`:
```kotlin
Surface(
    modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
    // ... resto igual
)
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/components/ScanListItem.kt
git commit -m "feat: add long press support to ScanListItem"
```

---

## Task 2: Modificar HistoryScreen con Tabs y Filtro

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt`

- [ ] **Step 1: Agregar imports**

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
```

- [ ] **Step 2: Agregar estado del tab seleccionado**

Después de `val folderMap = remember(folders) { ... }` agregar:
```kotlin
// Estado del tab seleccionado (índice, 0 = Todas)
var selectedTabIndex by remember { mutableIntStateOf(0) }

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
```

- [ ] **Step 3: Computar QRs filtrados**

```kotlin
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
```

- [ ] **Step 4: Agregar ScrollableTabRow después del TopAppBar**

En el Scaffold, después del TopAppBar:
```kotlin
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
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
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
    // Continuar con el resto del código...
```

- [ ] **Step 5: Actualizar LazyColumn para usar filteredScans**

Cambiar `items = scans` a `items = filteredScans`:
```kotlin
} else {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        items(
            items = filteredScans,  // Cambiar de scans a filteredScans
            key = { record -> record.id }
        ) { record ->
```

- [ ] **Step 6: Agregar onLongClick a ScanListItem con haptic**

En el lugar donde se llama a ScanListItem, agregar:
```kotlin
ScanListItem(
    record = record,
    folderName = record.folderId?.let { folderMap[it]?.name },
    onClick = { onScanTap(record.content, record.parsedContentType) },
    onLongClick = {
        HapticManager.vibrate(context)
        showMoveDialogFor = record
    }
)
```

También agregar import:
```kotlin
import com.example.qrapplication.barcode.HapticManager
```

Y estado para manejar el diálogo:
```kotlin
var showMoveDialogFor by remember { mutableStateOf<ScanRecord?>(null) }
```

Y agregar el diálogo:
```kotlin
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
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt
git commit -m "feat: add folder tabs and filter to HistoryScreen"
```

---

## Task 3: Compilar y Verificar

**Files:**
- Test: `app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt`

- [ ] **Step 1: Compilar proyecto**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Commit**

```bash
git commit -m "chore: verify folder filter compiles correctly"
```

---

## Resumen de Archivos

| Task | Archivo | Cambios |
|------|---------|---------|
| 1 | ScanListItem.kt | +combinedClickable, +onLongClick |
| 2 | HistoryScreen.kt | +ScrollableTabRow, +filtro, +dialogo mover |
| 3 | Compilación | Verificar BUILD SUCCESSFUL |

---

## Notas de Implementación

1. **Tab count:** Solo se muestra count si > 0 para mantener tabs limpios
2. **"+X" tab:** Abre FolderDialog existente para seleccionar carpeta
3. **Haptic:** Usa HapticManager.vibrate() existente antes de abrir diálogo
4. **Filtro reactivo:** filteredScans usa `remember` para recalcular solo cuando cambia scans o tab
5. **Optimistic update:** moveScanToFolder actualiza inmediatamente la UI