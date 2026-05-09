# Generator Screen v2 - Funcionalidad Completa

## Propósito
Agregar funcionalidad de guardado, compartir e historial a la pantalla de generador de QR.

## Cambios Aprobados

### Funcionalidades
- Historial de QR generados (lista persistente)
- Guardar QR al historial (botón)
- Compartir QR como imagen (botón)
- Eliminar QR del historial

### Layout Principal
- Input de texto
- Preview QR grande (300dp)
- Row de acciones: Guardar | Compartir
- Sección inferior: Historial horizontal de mini-QR

### Componentes Nuevos
- `GeneratorActions` — Botones guardar y compartir
- `QrHistoryRow` — LazyRow horizontal de thumbnails
- `QrHistoryItem` — Thumbnail clickeable

### Estilo Visual (Corporativo)
- Cards con borde sutil (#E0E0E0)
- Botones con bordes y texto
- Historial: thumbnails 60x60dp en scroll horizontal
- Colores neutros, tipografía limpia

## Arquitectura
- `GeneratorRepository` — Persistencia de QR (DataStore JSON)
- `GeneratorViewModel` — Estado de generación e historial
- `GeneratedQr` — Modelo (id, content, timestamp, bitmap)

## Mantener
- Generación en tiempo real
- TopAppBar con back
- Input actual

## Pendiente
- Loading states
- Manejo de errores al guardar