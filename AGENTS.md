# AGENTS.md - QrApplication

Quick context for future agents working on this Android QR Scanner app.

## Build Commands

```bash
./gradlew assembleDebug        # Build APK
./gradlew installDebug         # Install on device
./gradlew compileDebugKotlin  # Type-check only
```

## Key Dependencies (check versions in `gradle/libs.versions.toml`)

- CameraX 1.4.2
- ML Kit Barcode Scanning 17.3.0
- ZXing Android Embedded 4.3.0 + Core 3.5.3
- DataStore Preferences 1.1.7
- Kotlinx Serialization 1.7.3
- Accompanist Permissions 0.37.2

## Package Structure

```
app/src/main/java/com/example/qrapplication/
├── barcode/          # Analyzer, ViewModel, ActionHandler, HapticManager
├── data/             # ScanRepository (DataStore persistence)
├── model/            # ScanRecord, ContentType
├── navigation/       # NavHost, NavigationItem (3 tabs)
├── qr/               # QrGenerator
├── scanner/          # CameraPreview, ScanOverlay (UI tracker only)
├── screens/
│   ├── generator/    # GeneratorScreen (create QR)
│   ├── history/      # HistoryScreen, ViewModel
│   └── scanner/       # ScannerScreen
├── ui/               # Theme, EmptyState
└── util/             # TimeFormatter
```

## Architecture

- **Pattern:** MVVM with `StateFlow`
- **State in Screens:** `viewModel.state.collectAsState()`
- **Repository:** Passed from `AppNavHost` to screens via constructor
- **No DI framework:** Manual DI via factory classes

## Key Features

- **QR Tracker:** Minimal tracking rectangle (green in frame, red outside)
- **Burst Mode:** Toggle for rapid scanning (auto-save, 500ms debounce)
- **Gallery Scan:** PickVisualMedia for scanning images from gallery
- **QR Only:** Scanner detects QR, AZTEC, DataMatrix (not barcode formats)
- **Bounding Box:** Only processes when QR is inside scan frame

## Common Patterns

- **Haptic Feedback:** `vibrate(context)` in ScannerScreen
- **Avoid Duplicates:** `ScanRepository.saveScan()` updates timestamp
- **Share:** Use `ShareManager.shareText(context, content)`
- **Gallery Picker:** Use `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia())`
- **Lifecycle:** Use `androidx.lifecycle.compose.LocalLifecycleOwner`

## Navigation

3 bottom tabs: Scanner → Generator → History. Routes: `scanner`, `generator`, `history`.

## Quirks

- Camera permission via Accompanist `rememberPermissionState`
- Debounce: 3s normal, 500ms burst mode
- Detection filter: only QR/AZTEC/DataMatrix formats
- Scanner uses coroutine with IO dispatcher for DataStore
- `trackingState: TrackingState` passed to ScanOverlay

## Key Files

- `barcode/BarcodeAnalyzer.kt` - ML Kit with corner points for tracking
- `barcode/BarcodeScannerViewModel.kt` - State, burst mode, gallery result
- `barcode/ImageAnalyzer.kt` - Static image processing for gallery
- `scanner/components/ScanOverlay.kt` - Minimal: only tracker rectangle
- `scanner/components/CameraPreview.kt` - CameraX FILL_CENTER scale