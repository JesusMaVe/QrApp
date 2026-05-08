# Diseño: Filtrado por Carpetas y Movimiento de QRs

**Fecha:** 2026-05-08
**Proyecto:** QrApplication (Android)

---

## Resumen

Implementar tabs horizontales para filtrar QRs por carpeta, y funcionalidad de long press para mover QRs entre carpetas.

---

## 1. Estructura de Tabs

Los tabs aparecerán debajo de la barra superior ("Historial") como un scroll horizontal:

```
┌─────────────────────────────────────┐
│ Historial                    [📁]  │  <- TopAppBar
├─────────────────────────────────────┤
│ [Todas] [Trabajo] [Personal] [🔴+2] │  <- Tabs horizontales
├─────────────────────────────────────┤
│ ... lista de QRs ...                │
```

### Reglas de tabs:
- **"Todas"** siempre es el primer tab y muestra todos los QRs (no tiene count)
- **"Sin carpeta"** muestra solo QRs sin folder asignado
- **Carpetas** muestran su nombre con count: "Trabajo (5)"
- Máximo 5 tabs visibles (contando "Todas" y "Sin carpeta" si existen)
- Si hay más carpetas, el último tab muestra **"+X"** que abre diálogo con todas las carpetas

### Implementación de tabs:
- Usar `ScrollableTabRow` de Material 3
- Tab activo: texto `FontWeight.Bold` + indicador de línea (2dp, color primario)
- Tab inactivo: texto `FontWeight.Normal`, color `onSurfaceVariant`
- Edge fading con `Modifier.horizontalScroll` y gradient overlay

---

## 2. Comportamiento de Filtrado

- Al abrir la pantalla: siempre muestra el tab "Todas" (no persiste selección)
- Si no hay carpetas: solo se muestra "Todas" y "Sin carpeta"
- Al crear nueva carpeta: aparece automáticamente al final de los tabs
- Al eliminar carpeta: QRs pasan a "Sin carpeta", tab desaparece
- Cambio de tab: filtra instantáneamente sin animación

### Lógica de filtrado:
```kotlin
when (selectedTab) {
    "Todas" -> todos los QRs
    "Sin carpeta" -> QRs con folderId == null
    else -> QRs donde folderId coincide con carpeta seleccionada
}
```

---

## 3. Long Press para Mover QRs

### Interacción:
- Long press (500ms) en cualquier QR abre `FolderDialog`
- `FolderDialog` ya existe, solo se reutiliza
- Diálogo muestra: "Sin carpeta" + lista de carpetas existentes

### Flujo:
1. Usuario hace long press en QR
2. Se abre `FolderDialog` con todas las opciones
3. Usuario selecciona carpeta destino
4. QR se mueve instantáneamente (optimistic update)
5. Si el QR ya no corresponde al tab actual, se sale del tab
6. Si sigue en el tab, permanece en la lista

### Haptic feedback:
- Vibración corta al detectar long press (antes de abrir diálogo)

---

## 4. Cambios en Componentes

### `HistoryScreen.kt`:
- Agregar estado `selectedTab: String` (inicial: "Todas")
- Agregar `ScrollableTabRow` debajo del TopAppBar
- Computar lista de tabs dinámicamente desde `folders`
- Aplicar filtro a `scans` según tab seleccionado
- Agregar `onLongPress` al `ScanListItem`

### `ScanListItem.kt`:
- Agregar parámetro `onLongClick: () -> Unit`
- Aplicar `combinedClickable` o `pointerInput` para detectar long press
- Agregar ripple effect en long press (visual feedback)

### `HistoryViewModel.kt`:
- No necesita cambios (usan repository existente)

### `ScanRepository.kt`:
- `moveScanToFolder` ya existe y funciona correctamente

---

## 5. Estados y Edge Cases

| Situación | Comportamiento |
|-----------|----------------|
| Sin carpetas creadas | Mostrar tabs: [Todas] [Sin carpeta] |
| Carpeta sin QRs | Tab sigue visible, lista vacía con EmptyState |
| Mover último QR de carpeta | Carpeta desaparece de tabs |
| Carpeta renombrada | Tab actualiza nombre instantáneamente |
| >5 carpetas | Último tab es "+X", abre diálogo de selección |

---

## 6. Archivos a Modificar

1. `screens/history/HistoryScreen.kt` - Agregar tabs y filtro
2. `screens/history/components/ScanListItem.kt` - Agregar long press
3. `screens/history/components/FolderButton.kt` - Cambiar a acceso de gestión (opcional)
4. `screens/history/components/FolderDialog.kt` - Reutilizar (sin cambios)

---

## 7. Dependencias

No se requieren nuevas dependencias. Se usa:
- Material 3 `ScrollableTabRow` (ya incluido)
- Compose `combinedClickable` o `pointerInput`
- Haptic feedback existente `HapticManager`