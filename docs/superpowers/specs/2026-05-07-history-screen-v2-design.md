# History Screen v2 - Diseño Visual

## Propósito
Agregar personalidad a la pantalla de historial con colores dinámicos según tipo de contenido y previews visuales.

## Cambios Aprobados

### Layout
- Card con color de fondo según tipo de contenido
- Preview visual: mini-thumbnail si es URL (favicon), icono más grande para contactos
- Contenido truncado a 1 línea

### Componentes Nuevos
- `ColorCard` — Surface con color dinámico según ContentType
- `TypeBadge` — Chip pequeño indicando tipo (ej: "🔗 URL", "📧 Email")
- `ContentPreview` — Miniatura o preview del contenido

### Colores por Tipo
| ContentType | Color |
|-------------|-------|
| URL | Azul claro (#E3F2FD) |
| EMAIL | Verde claro (#E8F5E9) |
| PHONE | Naranja claro (#FFF3E0) |
| TEXT | Gris claro (#F5F5F5) |
| PRODUCT | Morado claro (#F3E5F5) |
| WIFI | Cyan claro (#E0F7FA) |
| CONTACT | Amarillo claro (#FFFDE7) |
| UNKNOWN | Gris neutro (#FAFAFA) |

## Mantener
- Swipe-to-delete existente
- Click para expandir/tocar
- LazyColumn con keys estables

## Pendiente
- Implementar preview de URLs con favicon
- Loading states para thumbnails