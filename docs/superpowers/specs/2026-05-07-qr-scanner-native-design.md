# QR Scanner - Diseño Nativo Android (Opción A)

## Overview
Rediseñar el scanner QR de la app para que tenga un estilo nativo de Android, inspirado en la app de cámara nativa de Android 14+.

## Visual Design

### Frame de Escaneo (ScanOverlay)
- **Forma**: Marco rectangular con bordes visibles (3px)
- **Esquinas**: Esquinas redondeadas (corner radius 16dp) con curvas suaves
- **Color**: Verde sistema Android (`#00C853` / Material Green 500)
- **Estados**:
  - Inactivo: Borde verde con 60% opacidad
  - Detectando: Borde animado con pulse (150ms)
  - Dentro del marco: Verde brillante (`#00E676`)
  - Fuera del marco: Rojo/naranja (`#FF9800`)

### UI Superior
- **Fondo**: Gradiente vertical (negro 70% → transparente)
- **Botones**: Circulares con fondo translúcido (rgba blanco 15%)
- **Iconos**: Blancos, tamaño 24dp
- **Sombra**: Box shadow sutil para profundidad

### Hint Text
- **Posición**: Centro inferior del frame
- **Fondo**: Fondo semi-translúcido (negro 50%) con blur
- **Border radius**: 20px (pill shape)
- **Padding**: 8dp vertical, 16dp horizontal

### Bottom Sheet de Resultados
- **Handle**: Barra de arrastre del sistema (40px × 4px)
- **Tipo de contenido**: Label pequeño (12sp), uppercase, color-acento
- **Contenido**: Texto del QR (16sp)
- **Botones**: 
  - Primary: Filled con color acento del sistema
  - Secondary: Outlined o text button
- **Animación**: Slide up con 300ms ease-out
- **Border radius**: 24dp top

### Paleta de Colores
- **Primary**: `#00C853` (Android Green 500)
- **Primary Variant**: `#00E676` (Green A400)
- **Secondary**: `#FF9800` (Orange 500 para warning)
- **Error**: `#F44336` (Red 500)
- **Surface**: `#1C1B1F` (dark) / `#FFFBFE` (light)
- **On Surface**: `#E6E1E5` (dark) / `#1C1B1F` (light)

## Animaciones

### Frame de Detección
- **Entrada**: Fade in 150ms cuando se detecta QR
- **Estado**: Pulse animation 1s loop cuando está追踪ando
- **Transición de estado**: Color transition 200ms

### Bottom Sheet
- **Entrada**: Slide up + fade in 300ms
- **Salida**: Slide down 200ms

## Comportamiento

### Feedback
- **Vibración**: 50ms al detectar código
- **Sonido**: Opcional, sonido de cámara del sistema
- **Toast**: Mostrar tipo detectado brevemente

### Modo Ráfaga (Burst)
- Feedback visual distintivo (color diferente o indicador)
- Sin mostrar bottom sheet en modo ráfaga

## Archivos a Modificar

1. **ScanOverlay.kt**: Frame con esquinas redondeadas, animaciones
2. **ScannerScreen.kt**: UI con gradiente, botones flotantes
3. **ResultBottomSheet.kt**: Estilo Material 3 con colores del sistema
4. **Theme.kt**: Colores Android Green para el tema
5. **Color.kt**: Agregar Android Green palette

## Éxito

- [ ] Frame de escaneo con estilo Android nativo
- [ ] Colores consistentes con Material Design 3
- [ ] Animaciones fluidas (60fps)
- [ ] Feedback haptic al detectar
- [ ] Bottom sheet con estilo Material 3
- [ ] Compatible con light/dark theme