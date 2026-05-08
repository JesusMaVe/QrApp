# QR Folders - Sistema de Carpetas

## Propósito
Agregar carpetas para organizar códigos QR escaneados.

## Cambios Aprobados

### Acceso
- Botón en HistoryScreen que abre diálogo de carpetas

### Flujo Normal (Scan individual)
1. QR detectado → BottomSheet actual
2. Nueva opción: "Guardar en carpeta"
3. Al pulsar: muestra selector de carpeta o crear nueva
4. Después de guardar: acción original (abrir navegador, etc.)

### Flujo Modo Ráfaga
1. Escanea todos los QR automáticamente (sin dialogos)
2. Al apagar modo ráfaga → pantalla "Revisar escaneos"
3. Lista de QR escaneados con checkboxes
4. Opción "Asignar a carpeta" → selector
5. No abre navegador automáticamente (diferente al normal)

### Modelo de Datos

**QrFolder:**
- id: String
- name: String
- createdAt: Long

**ScanRecord (actualizado):**
- folderId: String? = null (null = "Sin carpeta")

### Carpetas por defecto
- "Sin carpeta" — carpeta por defecto para QR sin asignar

### Componentes UI
- `FolderButton` — Botón en HistoryScreen
- `FolderDialog` — Crear/seleccionar carpeta
- `BurstReviewScreen` — Revisar QR escaneados en ráfaga
- `FolderListItem` — Item de carpeta en selector

### Estilo
- Corporativo (igual que History y Generator)

## Pendiente
- Mover QR entre carpetas (futuro)
- Editar nombre de carpeta (futuro)
- Eliminar carpeta (futuro)