Audit or update Android/Compose SDK usage in TraiScore2 for the following:

$ARGUMENTS

## Current versions in this project

File: `gradle/libs.versions.toml`

| Library | Current | Notes |
|---------|---------|-------|
| AGP | 8.13.0 | Android Gradle Plugin |
| Kotlin | 2.0.21 | Compose Compiler Plugin included since 2.0 |
| Compose BOM | 2024.12.01 | Controls all `androidx.compose.*` versions |
| Material3 | 1.3.1 | Pinned outside BOM — check for drift |
| Navigation Compose | 2.8.4 | Type-safe routes available since 2.8 |
| Lifecycle (all) | 2.8.7 | runtime-ktx, viewmodel-compose, runtime-compose |
| Activity Compose | 1.9.3 | `enableEdgeToEdge()` lives here |
| Room (all) | 2.6.1 | runtime, ktx, compiler, common |
| Hilt | 2.51.1 | hilt-android + hilt-compiler |
| Core KTX | 1.15.0 | |
| Core Splashscreen | 1.0.1 | Stable — no major changes expected |

---

## How to check for updates

### Option A — Android Studio
`File → Project Structure → Suggestions tab` shows available upgrades with one-click apply.

### Option B — Gradle command
```bash
./gradlew dependencyUpdates
```
Requires the [ben-manes/gradle-versions-plugin](https://github.com/ben-manes/versions-plugin) — add it to `build.gradle.kts` if not present.

### Option C — Check Maven manually
- Compose BOM: https://developer.android.com/jetpack/compose/bom/bom-mapping
- All Jetpack: https://developer.android.com/jetpack/androidx/versions

---

## Safe upgrade order

Always upgrade in this order to avoid incompatibilities:

1. **AGP** (requires matching Android Studio version)
2. **Kotlin** (Compose Compiler Plugin tracks Kotlin — same version ref `kotlin`)
3. **Compose BOM** (bumps all `androidx.compose.*` at once — no per-library pinning needed)
4. **Navigation Compose** (independent of BOM)
5. **Lifecycle** (independent of BOM)
6. **Room** (independent — compiler version must match runtime)
7. **Hilt** (independent — `hilt-android` and `hilt-compiler` must be the same version)

> Never mix BOM-managed versions with manual pins. If Material3 is pinned in `libs.versions.toml`, remove the `version.ref` from the library entry and let BOM control it.

---

## Key API changes to know (as of knowledge cutoff Aug 2025)

### Compose Compiler Plugin (Kotlin 2.0+)
- The old `kotlinCompilerExtensionVersion` in `composeOptions {}` is **gone**.
- Plugin is now: `id("org.jetbrains.kotlin.plugin.compose")` — already correct in this project.
- No extra config needed. The plugin picks up automatically from the Kotlin version.

### Navigation Compose 2.8+ — Type-safe routes
Old pattern (avoid in new code):
```kotlin
composable("routineScreen/{documentId}") { backStackEntry ->
    val id = backStackEntry.arguments?.getString("documentId")
}
```
New pattern (use this):
```kotlin
@Serializable
data class RoutineRoute(val documentId: String)

composable<RoutineRoute> { backStackEntry ->
    val route: RoutineRoute = backStackEntry.toRoute()
}
```
Navigation args are now compile-time safe. No string parsing, no crashes on typos.

### Material3 1.4+ changes to watch
- `TopAppBar` now has `expandedHeight` / `collapsedHeight` parameters — use instead of manual size hacks.
- `ExposedDropdownMenuBox` API stabilized — replaces custom dropdown patterns.
- `OutlinedTextField` container color defaults changed in BOM 2025.x — always set all four container states explicitly (this project already does this in `RoutineTable.kt`).

### Lifecycle 2.9+ (if upgrading)
- `collectAsStateWithLifecycle()` is now the default way to collect flows in Compose — already used in this project.
- `ViewModel.viewModelScope` now uses `Dispatchers.Main.immediate` — no behavior change, just faster.

### Room 2.7+ (if upgrading)
- KSP is now the recommended annotation processor over KAPT. If upgrading Room past 2.6, switch from:
  ```kotlin
  kapt("androidx.room:room-compiler:...")
  ```
  to:
  ```kotlin
  ksp("androidx.room:room-compiler:...")
  ```
  And add `id("com.google.devtools.ksp")` to the plugins block.

### Hilt 2.52+ (if upgrading)
- No breaking changes. Upgrade is safe as long as `hilt-android` and `hilt-compiler` stay on the same version.
- With KSP migration (see Room above), also switch `kapt(hilt-compiler)` → `ksp(hilt-compiler)`.

---

## What NOT to do

| ❌ Avoid | ✅ Instead |
|----------|-----------|
| Pin individual `androidx.compose.*` versions | Use BOM only — remove `version.ref` from compose libs |
| Mix `kapt` and `ksp` for the same library | Pick one processor per library |
| Upgrade AGP without matching Android Studio | Check the AGP/Studio compatibility table first |
| Upgrade Kotlin without checking Compose plugin compatibility | They are the same version — update both refs together |
| Use `androidx.compose.ui:ui` with an explicit version AND BOM | Either BOM or pin, never both |
| Use deprecated `Navigation` string routes for new screens | Use `@Serializable` type-safe routes |

---

## When asked to audit versions

1. Read `gradle/libs.versions.toml` to get current values
2. Compare against the table above (or check Maven for the latest)
3. Identify what's outdated
4. Report: current version → latest version, and whether the upgrade is safe or has breaking changes
5. Apply changes only to `libs.versions.toml` — never edit `build.gradle.kts` version strings directly

## When asked to upgrade a specific library

1. Update the version in `libs.versions.toml`
2. Check if it pulls any other library that needs updating (e.g., Room compiler = Room runtime)
3. Check for API changes that affect existing code in the project
4. Run a Gradle sync mentally — flag any known incompatibilities before writing code
