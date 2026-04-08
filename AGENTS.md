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

**Note:** ZXing Core version (3.5.3) differs from embedded version (4.3.0) - this is intentional.

## Package Structure

```
app/src/main/java/com/example/qrapplication/
├── barcode/          # Scanning logic: Analyzer, ViewModel, ActionHandler
├── data/             # ScanRepository (DataStore persistence)
├── model/            # ScanRecord, ContentType
├── navigation/       # NavHost, NavigationItem (3 tabs)
├── qr/               # QrGenerator
├── scanner/          # CameraPreview, ScanOverlay
├── screens/
│   ├── generator/    # GeneratorScreen (create QR)
│   ├── history/      # HistoryScreen, ViewModel
│   └── scanner/      # ScannerScreen
└── ui/               # Theme, EmptyState
```

## Architecture

- **Pattern:** MVVM with `StateFlow`
- **State in Screens:** `viewModel.state.collectAsState()`
- **Repository:** Passed from `AppNavHost` to screens via constructor
- **No DI framework:** Manual DI via factory classes (e.g., `HistoryViewModelFactory`)

## Common Patterns

- **Haptic Feedback:** Use `HapticManager.vibrate(context)` after successful scan
- **Avoid Duplicates:** `ScanRepository.saveScan()` checks content and updates timestamp
- **Share:** Use `ShareManager.shareText(context, content)`
- **Lifecycle:** Use `androidx.lifecycle.compose.LocalLifecycleOwner` (not deprecated version)

## Navigation

3 bottom tabs: Scanner → Generator → History. Routes: `scanner`, `generator`, `history`.

## Quirks

- Camera permission via Accompanist `rememberPermissionState`
- Debounce in BarcodeScannerViewModel: 3 seconds between same scans
- ScanOverlay animates frame size based on barcode format
- Scanner uses coroutine scope with IO dispatcher for DataStore writes