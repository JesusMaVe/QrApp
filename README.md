# QrApplication - Scanner QR y Codigos de Barras

Aplicacion Android moderna para escanear codigos QR y codigos de barras, construida con Jetpack Compose, CameraX y ML Kit.

## Proposito de la App

QrApplication proporciona una experiencia de escaneo en tiempo real con acciones contextuales basadas en el contenido escaneado. La aplicacion identifica varios formatos de codigos de barras y detecta inteligentemente tipos de contenido como URL, datos WiFi, Contactos y Productos.

## Caracteristicas

- **Escaneo en tiempo real**: Camara con soporte para multiples formatos de codigos de barras (QR, EAN, UPC, Code128, etc.)
- **Marco de escaneo adaptativo**: El overlay de escaneo se ajusta automaticamente segun el tipo de codigo detectado
- **Control de linterna**: Boton para togglear la linterna del dispositivo directamente en la UI del escaner
- **Acciones contextuales**: Manejo automatico de diferentes tipos de contenido (abre URLs en navegador, marca numeros, busca productos, etc.)
- **Historial de escaneos**: Persistencia local de resultados usando DataStore para referencia futura
- **Generador de QR**: Crear codigos QR a partir de texto personalizado
- **Feedbackaptic**: Vibracion al detectar un codigo exitosamente
- **Compartir**: Compartir contenido escaneado via Android share sheet
- **Interfaz moderna**: Diseño limpio con Material 3 y animaciones suaves

## Tecnologias

- Kotlin
- Jetpack Compose (Material 3)
- CameraX
- ML Kit Barcode Scanning
- Jetpack DataStore (Preferences con Serialization)
- ZXing (para generacion de QR)
- Navigation Compose

## Requisitos

- Android Studio Ladybug o superior
- Android SDK 36 (Compile SDK)
- Min SDK 24
- Gradle 9.x

## Como ejecutar la app

### Configuracion inicial

1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Sincronizar dependencias con Gradle

### Comandos de construccion

```bash
# Construir APK de debug
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Verificar compilacion Kotlin
./gradlew compileDebugKotlin

# Ejecutar tests unitarios
./gradlew test

# Verificacion de lint
./gradlew lint
```

### Estructura del proyecto

```
app/src/main/java/com/example/qrapplication/
├── barcode/          # Logica de escaneo: Analyzer, ViewModel, ActionHandler
├── data/             # ScanRepository (persistencia DataStore)
├── model/            # ScanRecord, ContentType
├── navigation/       # NavHost, NavigationItem
├── qr/               # QrGenerator
├── scanner/          # CameraPreview, ScanOverlay
├── screens/
│   ├── generator/    # GeneratorScreen (crear QR)
│   ├── history/      # HistoryScreen, ViewModel
│   └── scanner/      # ScannerScreen
├── ui/               # Theme, EmptyState
└── util/             # TimeFormatter
```

## Licencia

Apache License 2.0
