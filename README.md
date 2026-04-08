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

## Guia para contribuidores

### Convenciones de desarrollo

- **Compose First**: Todos los componentes de UI deben construirse usando Jetpack Compose
- **Patron MVVM**: Separacion estricta de responsabilidades entre UI (Screens) y logica (ViewModels)
- **Estado reactivo**: Usar `StateFlow` en ViewModels para exponer estado a la UI
- **Manejo de permisos**: Permisos de camara gestionados reactivamente usando Accompanist Permissions
- **Lifecycle de CameraX**: Siempre asegurar que los casos de uso de CameraX esten correctamente bound al LifecycleOwner

### Como contribuir

1. Hacer fork del repositorio
2. Crear una rama para la feature (`git checkout -b feature/nombre`)
3. Realizar cambios con tests correspondientes
4. Verificar que el codigo compile: `./gradlew compileDebugKotlin`
5. Hacer commit y push
6. Crear un Pull Request

### Estilo de codigo

- Usar `remember` y `animate*AsState` para transiciones de UI performantes
- Preferir funciones de extension cuando sea apropiado
- Documentar funciones publicas complejas
- Seguir las convenciones de Kotlin existentes en el proyecto

## Licencia

Apache License 2.0
