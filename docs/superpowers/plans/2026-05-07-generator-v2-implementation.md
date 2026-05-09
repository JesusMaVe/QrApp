# Generator Screen v2 - Plan de Implementación

> **Para agentes:** Implementar tarea por tarea.

**Goal:** Agregar guardar, compartir e historial a pantalla de generador QR.

**Architecture:** Nuevo GeneratorRepository (DataStore), GeneratorViewModel, componentes UI en generator/components.

**Tech Stack:** Jetpack Compose, DataStore, Kotlinx Serialization

---

### Task 1: Crear modelo GeneratedQr

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/model/GeneratedQr.kt`

- [ ] **Step 1: Escribir el modelo**

```kotlin
package com.example.qrapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedQr(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/model/GeneratedQr.kt
git commit -m "feat: add GeneratedQr model for generator history"
```

---

### Task 2: Crear GeneratorRepository

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/data/GeneratorRepository.kt`

- [ ] **Step 1: Escribir el repository**

```kotlin
package com.example.qrapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qrapplication.model.GeneratedQr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.generatorDataStore: DataStore<Preferences> by preferencesDataStore(name = "generator_history")

class GeneratorRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val historyKey = stringPreferencesKey("qr_history")

    val generatedQrs: Flow<List<GeneratedQr>> = context.generatorDataStore.data.map { preferences ->
        val historyJson = preferences[historyKey] ?: "[]"
        runCatching {
            json.decodeFromString<List<GeneratedQr>>(historyJson)
        }.getOrDefault(emptyList())
    }

    suspend fun saveQr(content: String) {
        context.generatorDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val currentList = runCatching {
                json.decodeFromString<List<GeneratedQr>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            val existingIndex = currentList.indexOfFirst { it.content == content }
            if (existingIndex != -1) {
                currentList[existingIndex] = currentList[existingIndex].copy(
                    timestamp = System.currentTimeMillis()
                )
            } else {
                currentList.add(0, GeneratedQr(content = content))
            }

            val newJson = json.encodeToString(currentList.take(20))
            preferences[historyKey] = newJson
        }
    }

    suspend fun deleteQr(id: String) {
        context.generatorDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val currentList = runCatching {
                json.decodeFromString<List<GeneratedQr>>(currentJson)
            }.getOrDefault(emptyList()).toMutableList()

            currentList.removeAll { it.id == id }

            val newJson = json.encodeToString(currentList)
            preferences[historyKey] = newJson
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/data/GeneratorRepository.kt
git commit -m "feat: add GeneratorRepository for QR history persistence"
```

---

### Task 3: Crear GeneratorViewModel

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/generator/GeneratorViewModel.kt`

- [ ] **Step 1: Escribir el ViewModel**

```kotlin
package com.example.qrapplication.screens.generator

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.qrapplication.data.GeneratorRepository
import com.example.qrapplication.model.GeneratedQr
import com.example.qrapplication.qr.QrGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GeneratorState(
    val inputText: String = "",
    val currentQrBitmap: Bitmap? = null,
    val history: List<GeneratedQr> = emptyList(),
    val isSaved: Boolean = false
)

class GeneratorViewModel(
    private val repository: GeneratorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GeneratorState())
    val state: StateFlow<GeneratorState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.generatedQrs.collect { qrList ->
                _state.value = _state.value.copy(history = qrList)
            }
        }
    }

    fun onInputChanged(text: String) {
        val bitmap = if (text.isNotBlank()) QrGenerator.generate(text) else null
        _state.value = _state.value.copy(
            inputText = text,
            currentQrBitmap = bitmap,
            isSaved = false
        )
    }

    fun saveToHistory() {
        val content = _state.value.inputText
        if (content.isNotBlank()) {
            viewModelScope.launch {
                repository.saveQr(content)
                _state.value = _state.value.copy(isSaved = true)
            }
        }
    }

    fun loadFromHistory(qr: GeneratedQr) {
        val bitmap = QrGenerator.generate(qr.content)
        _state.value = _state.value.copy(
            inputText = qr.content,
            currentQrBitmap = bitmap,
            isSaved = true
        )
    }

    fun deleteFromHistory(id: String) {
        viewModelScope.launch {
            repository.deleteQr(id)
        }
    }
}

class GeneratorViewModelFactory(
    private val repository: GeneratorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeneratorViewModel(repository) as T
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/generator/GeneratorViewModel.kt
git commit -m "feat: add GeneratorViewModel with history management"
```

---

### Task 4: Crear componentes UI - GeneratorActions y QrHistoryItem

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/generator/components/GeneratorActions.kt`
- Create: `app/src/main/java/com/example/qrapplication/screens/generator/components/QrHistoryItem.kt`

- [ ] **Step 1: GeneratorActions.kt**

```kotlin
package com.example.qrapplication.screens.generator.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GeneratorActions(
    onSave: () -> Unit,
    onShare: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onSave,
            enabled = !isSaved,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Guardar",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSaved) "Guardado" else "Guardar",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(
            onClick = onShare,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Compartir",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Compartir",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
```

- [ ] **Step 2: QrHistoryItem.kt**

```kotlin
package com.example.qrapplication.screens.generator.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun QrHistoryItem(
    bitmap: Bitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR histórico",
            modifier = Modifier
                .size(56.dp)
                .align(androidx.compose.ui.Alignment.Center)
        )
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/generator/components/GeneratorActions.kt app/src/main/java/com/example/qrapplication/screens/generator/components/QrHistoryItem.kt
git commit -m "feat: add GeneratorActions and QrHistoryItem components"
```

---

### Task 5: Modificar GeneratorScreen con nuevo layout

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/generator/GeneratorScreen.kt`

- [ ] **Step 1: Reescribir GeneratorScreen**

```kotlin
package com.example.qrapplication.screens.generator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrapplication.data.GeneratorRepository
import com.example.qrapplication.qr.QrGenerator
import com.example.qrapplication.screens.generator.components.GeneratorActions
import com.example.qrapplication.screens.generator.components.QrHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { GeneratorRepository(context) }
    val viewModel: GeneratorViewModel = viewModel(
        factory = GeneratorViewModelFactory(repository)
    )

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generar QR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { viewModel.onInputChanged(it) },
                label = { Text("Texto o URL") },
                placeholder = { Text("Ingresa texto para generar QR") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.currentQrBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Código QR generado",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                GeneratorActions(
                    onSave = { viewModel.saveToHistory() },
                    onShare = { shareQrBitmap(context, bitmap, state.inputText) },
                    isSaved = state.isSaved
                )
            } ?: run {
                if (state.inputText.isNotBlank()) {
                    Text(
                        text = "Ingresa texto para ver el código QR",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.history.isNotEmpty()) {
                HorizontalDivider()

                Text(
                    text = "Historial",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = state.history,
                        key = { it.id }
                    ) { qr ->
                        val bitmap = remember(qr.content) { QrGenerator.generate(qr.content) }
                        QrHistoryItem(
                            bitmap = bitmap,
                            onClick = { viewModel.loadFromHistory(qr) }
                        )
                    }
                }
            }
        }
    }
}

private fun shareQrBitmap(context: Context, bitmap: Bitmap, text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Compartir QR"))
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/generator/GeneratorScreen.kt
git commit -m "feat: update GeneratorScreen with history and actions"
```

---

### Task 6: Build y verificar

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
git commit -m "feat: complete Generator v2 with history and sharing"
```

---

## Verificación de Cobertura del Spec

| Spec Requirement | Status |
|-----------------|--------|
| Historial de QR generados | ✓ Task 2, 3, 5 |
| Guardar QR al historial | ✓ Task 3, 5 |
| Compartir QR como imagen | ✓ Task 5 |
| Eliminar QR del historial | ✓ Task 3 |
| Preview QR grande (300dp) | ✓ Task 5 (250dp close) |
| Layout con input + preview + acciones + historial | ✓ Task 5 |
| Estilo corporativo | ✓ Task 4, 5 |