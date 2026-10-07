# Mimore — By DeBroglie

Mimore is a local-first, native Android calculator focused on minimal UI, tactile input, fast math, persistent history, and subtle optical depth inspired by modern dynamic materials without generic glassmorphism.

## Tech stack
- Kotlin + Jetpack Compose
- Material 3
- AndroidX Core / Activity / Lifecycle
- SharedPreferences for lightweight local persistence
- Native Android haptic feedback
- No backend, login, analytics, ads, or network calls

## Minimum Android version
Android 8.0 (API 26) or higher.

## Open
Open the `Mimore-By-DeBroglie` folder in Android Studio. Use the included Gradle wrapper. The project expects a JDK compatible with the configured Android Gradle Plugin (JDK 17 is the safe baseline).

## Build
- Debug APK: `./gradlew assembleDebug`
- Debug install: `./gradlew installDebug`
- Release APK: `./gradlew assembleRelease`

APK output is under `app/build/outputs/apk/`.

## Architecture
- `calculator/` — tokenizer, parser/evaluator, calculator state helpers
- `data/` — persistent calculation history and settings store
- `haptics/` — native Android tactile feedback abstraction
- `ui/` — theme, calculator surface, history sheet, settings sheet, help content
- `MainActivity.kt` — edge-to-edge entry point and top-level composition

## Main features
- Precedence-aware expression evaluation using `BigDecimal`
- Decimal, negative numbers, percentage, delete, clear, repeated equals
- Persistent history with reuse/delete/clear-all
- System/light/dark theme plus six accent palettes
- Native haptic feedback with intensity settings
- Subtle press/deformation/highlight interactions
- Accessibility labels and touch targets
- Minimal offline-first architecture

## Validation status for this delivery
The standalone calculator engine was compiled with Kotlin/JVM and passed 22 assertions covering precedence, decimals, negative values, contextual percentages, recurring-decimal formatting, very large values, divide-by-zero, invalid expressions, and grouping.

Android XML resources parse successfully in the available runtime. A full `assembleDebug` attempt was made through the included wrapper, but this execution environment has no Android SDK/Gradle dependency cache and outbound DNS/network access is unavailable, so the Gradle distribution could not be downloaded and an APK could not be produced here. The project is therefore delivered as a complete source project rather than falsely claiming a locally compiled APK.
