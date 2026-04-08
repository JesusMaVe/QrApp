# QrApplication - Android QR & Barcode Scanner

A modern, functional Android application for scanning QR codes and barcodes, built with **Jetpack Compose**, **CameraX**, and **ML Kit**.

## Project Overview

This application provides a real-time scanning experience with contextual actions based on the scanned content. It identifies various barcode formats and intelligently detects content types such as URLs, Wi-Fi details, Contacts, and Products.

### Main Technologies
- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Camera Engine:** [CameraX](https://developer.android.com/training/camerax)
- **Scanning Library:** [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- **Data Persistence:** [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences with Serialization)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Navigation:** [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

### Key Features
- **Adaptive Scanner Frame:** The scanning overlay automatically adjusts its shape (Square vs. Rectangular) based on the type of barcode detected.
- **Flashlight Control:** Integrated toggle for the device's torch directly in the scanner UI.
- **Contextual Actions:** Automatically handles different content types (opens URLs in browser, dials numbers, searches products, etc.) via `ActionHandler`.
- **Scan History:** Persists scan results locally using DataStore for future reference.
- **Modern UI:** Clean Material 3 design with smooth animations and intuitive feedback.

## Project Structure

- `app/src/main/java/com/example/qrapplication/`
    - `barcode/`: Core scanning logic (`BarcodeAnalyzer`), content detection (`ContentTypeDetector`), and action execution (`ActionHandler`).
    - `data/`: Data management (`ScanRepository`) using Jetpack DataStore.
    - `model/`: Domain models (`ScanRecord`, `ContentType`).
    - `navigation/`: App navigation graph and bottom bar configuration.
    - `scanner/`: Reusable UI components for the camera and overlay.
    - `screens/`: Main feature screens (`ScannerScreen`, `HistoryScreen`).
    - `ui/theme/`: Material 3 theme, colors, and typography.

## Building and Running

### Prerequisites
- Android Studio Ladybug or newer.
- Android SDK 36 (Compile SDK) / Min SDK 24.

### Commands
- **Build APK:** `./gradlew assembleDebug`
- **Install on Device:** `./gradlew installDebug`
- **Run Unit Tests:** `./gradlew test`
- **Run Instrumented Tests:** `./gradlew connectedAndroidTest`
- **Lint Check:** `./gradlew lint`

## Development Conventions

- **Compose First:** All UI components must be built using Jetpack Compose.
- **MVVM Pattern:** Strict separation of concerns between UI (Screens) and logic (ViewModels).
- **Reactive State Management:** Use `StateFlow` in ViewModels to expose state to the UI.
- **Surgical UI Updates:** Use `remember` and `animate*AsState` for performant and smooth UI transitions.
- **Permission Handling:** Camera permissions are managed reactively using `Accompanist Permissions`.
- **CameraX Lifecycle:** Always ensure CameraX use cases are correctly bound to the `LifecycleOwner`.

## Key Files for Reference
- `ScannerScreen.kt`: The primary entry point for the scanning feature.
- `BarcodeScannerViewModel.kt`: Manages the scanning state, flashlight, and detected formats.
- `ScanOverlay.kt`: Implementation of the adaptive scanning frame and animations.
- `ActionHandler.kt`: Centralized logic for executing actions based on scan results.
- `ScanRepository.kt`: Handles the persistence of scan history.
