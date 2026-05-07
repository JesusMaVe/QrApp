# History Screen v2 - Plan de Implementación

> **Para agentes:** Implementar tarea por tarea usando el enfoque de subagent o ejecución inline.

**Goal:** Agregar diseño corporativo a pantalla de historial: cards con colores sutiles según tipo, badges de tipo, y preview de contenido.

**Architecture:** Modificar componentes existentes de Compose, agregar nuevos componentes en subpaquete `components`, mantener swipe-to-delete.

**Tech Stack:** Jetpack Compose, Material3

---

### Task 1: Agregar extensión de color por ContentType

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/history/components/ContentTypeColors.kt`
- Modify: `app/src/main/java/com/example/qrapplication/model/ContentType.kt`

- [ ] **Step 1: Crear archivo ContentTypeColors.kt**

```kotlin
package com.example.qrapplication.screens.history.components

import androidx.compose.ui.graphics.Color
import com.example.qrapplication.model.ContentType

object ContentTypeColors {
    fun getBackgroundColor(type: ContentType): Color {
        return when (type) {
            ContentType.URL -> Color(0xFFE3F2FD)
            ContentType.EMAIL -> Color(0xFFE8F5E9)
            ContentType.PHONE -> Color(0xFFFFF3E0)
            ContentType.TEXT -> Color(0xFFF5F5F5)
            ContentType.PRODUCT -> Color(0xFFF3E5F5)
            ContentType.WIFI -> Color(0xFFE0F7FA)
            ContentType.CONTACT -> Color(0xFFFFFDE7)
            ContentType.UNKNOWN -> Color(0xFFFAFAFA)
        }
    }

    fun getBorderColor(type: ContentType): Color {
        return when (type) {
            ContentType.URL -> Color(0xFF90CAF9)
            ContentType.EMAIL -> Color(0xFFA5D6A7)
            ContentType.PHONE -> Color(0xFFFFCC80)
            ContentType.TEXT -> Color(0xFFE0E0E0)
            ContentType.PRODUCT -> Color(0xFFCE93D8)
            ContentType.WIFI -> Color(0xFF80DEEA)
            ContentType.CONTACT -> Color(0xFFFFF59D)
            ContentType.UNKNOWN -> Color(0xFFE0E0E0)
        }
    }

    fun getAccentColor(type: ContentType): Color {
        return when (type) {
            ContentType.URL -> Color(0xFF1976D2)
            ContentType.EMAIL -> Color(0xFF388E3C)
            ContentType.PHONE -> Color(0xFFF57C00)
            ContentType.TEXT -> Color(0xFF616161)
            ContentType.PRODUCT -> Color(0xFF7B1FA2)
            ContentType.WIFI -> Color(0xFF0097A7)
            ContentType.CONTACT -> Color(0xFFFBC02D)
            ContentType.UNKNOWN -> Color(0xFF757575)
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/components/ContentTypeColors.kt
git commit -m "feat: add ContentTypeColors helper for corporate design"
```

---

### Task 2: Crear componente TypeBadge

**Files:**
- Create: `app/src/main/java/com/example/qrapplication/screens/history/components/TypeBadge.kt`

- [ ] **Step 1: Escribir el componente TypeBadge**

```kotlin
package com.example.qrapplication.screens.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.qrapplication.model.ContentType

@Composable
fun TypeBadge(
    contentType: ContentType,
    modifier: Modifier = Modifier
) {
    val accentColor = ContentTypeColors.getAccentColor(contentType)
    val backgroundColor = accentColor.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getIconForType(contentType),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = contentType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

private fun getIconForType(type: ContentType): ImageVector {
    return when (type) {
        ContentType.URL -> Icons.Default.Link
        ContentType.EMAIL -> Icons.Default.Email
        ContentType.PHONE -> Icons.Default.Phone
        ContentType.TEXT -> Icons.Default.Search
        ContentType.PRODUCT -> Icons.Default.ShoppingCart
        ContentType.WIFI -> Icons.Default.Wifi
        ContentType.CONTACT -> Icons.Default.Person
        ContentType.UNKNOWN -> Icons.Default.Star
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/components/TypeBadge.kt
git commit -m "feat: add TypeBadge component with corporate styling"
```

---

### Task 3: Modificar ScanListItem con diseño corporativo

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/history/components/ScanListItem.kt`

- [ ] **Step 1: Reescribir ScanListItem con nuevo diseño**

```kotlin
package com.example.qrapplication.screens.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.qrapplication.model.ContentType
import com.example.qrapplication.model.ScanRecord
import com.example.qrapplication.util.TimeFormatter

@Composable
fun ScanListItem(
    record: ScanRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ContentTypeColors.getBackgroundColor(record.parsedContentType)
    val borderColor = ContentTypeColors.getBorderColor(record.parsedContentType)
    val accentColor = ContentTypeColors.getAccentColor(record.parsedContentType)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de tipo con línea lateral
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = accentColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            // Icono del tipo
            Icon(
                imageVector = getIconForType(record.parsedContentType),
                contentDescription = record.parsedContentType.displayName,
                tint = accentColor,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp)
            )

            // Contenido y metadata
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TypeBadge(contentType = record.parsedContentType)
                }

                Text(
                    text = formatCorporateTime(record.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun getIconForType(type: ContentType): ImageVector {
    return when (type) {
        ContentType.URL -> Icons.Default.Star
        ContentType.EMAIL -> Icons.Default.Email
        ContentType.PHONE -> Icons.Default.Phone
        ContentType.TEXT -> Icons.Default.Search
        ContentType.PRODUCT -> Icons.Default.ShoppingCart
        ContentType.WIFI -> Icons.Default.Star
        ContentType.CONTACT -> Icons.Default.Search
        ContentType.UNKNOWN -> Icons.Default.Search
    }
}

private fun formatCorporateTime(timestamp: Long): String {
    return TimeFormatter.formatRelative(timestamp)
}

@Composable
private fun BoxScope.fillMaxWidth(): Modifier {
    return Modifier.fillMaxWidth()
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/components/ScanListItem.kt
git commit -m "feat: apply corporate design to ScanListItem"
```

---

### Task 4: Ajustar HistoryScreen para nuevo layout

**Files:**
- Modify: `app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt`

- [ ] **Step 1: Revisar que el padding sea consistente**

El diseño existente debería funcionar con el nuevo diseño de ScanListItem. Solo verificar que el padding horizontal sea consistente.

- [ ] **Step 2: Commit si hay cambios**

```bash
git add app/src/main/java/com/example/qrapplication/screens/history/HistoryScreen.kt
git commit -m "chore: verify HistoryScreen compatible with new ScanListItem design"
```

---

### Task 5: Build y verificar

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
git commit -m "feat: complete corporate design for History Screen v2"
```

---

## Verificación de Cobertura del Spec

| Spec Requirement | Status |
|-----------------|--------|
| Card con color de fondo según tipo | ✓ Task 1, 3 |
| TypeBadge con ícono y texto | ✓ Task 2 |
| Preview visual (iconos) | ✓ Task 3 |
| Contenido truncado a 1 línea | ✓ Task 3 |
| Swipe-to-delete mantenido | ✓ Revisar en Task 3 |
| LazyColumn con keys estables | ✓ Ya existente |
| Colores corporativos (no brillantes) | ✓ Task 1 |

**Pending del spec (no incluido en plan por complejidad):**
- Preview de URLs con favicon (Task compleja, fuera de scope MVP)
- Loading states para thumbnails (depende de Task anterior)