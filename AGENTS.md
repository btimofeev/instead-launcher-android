# AGENTS.md

## Project overview

INSTEAD Launcher is an Android app for downloading and running games for the "INSTEAD" text-quest engine. The native engine and SDL3 are built from source directly in this project (the `:instead` module).

- The JVM-layer language is Kotlin. The UI on `master` is built with Android Views + XML layouts (NOT Compose).
- Architecture: MVVM + Clean Architecture + `singleton android activity`.
- DI: Hilt (KSP). DB: Room (KSP). Background work: WorkManager + foreground services.
- SDK versions: `minSdk 25`, `compile/targetSdk 34`, `ndk 27.2.12479018`, Java 21 bytecode (source/target), Gradle runs on JDK 25.

## Repository structure

| Module | Purpose |
|---|---|
| `app` | Main launcher: UI, features, repositories, use cases, WorkManager |
| `instead` | Native INSTEAD engine + SDL3/LuaJIT/libiconv (C/Kotlin), `InsteadActivity` |
| `sdl-activity` | Java SDL wrapper for Android (package `org.libsdl.app`) |
| `scancode-generator` | Keyboard scancode generator |
| `core-storage` | Filesystem abstraction (game, data and saves directories) |
| `core-preferences` | Settings abstraction (SharedPreferences) |

App packages: `org.emunix.insteadlauncher` (app), `org.emunix.instead*` (libraries).

Code layers under `app/src/main/kotlin/org/emunix/insteadlauncher/`:
- `presentation/` — Fragment/ViewModel/Adapter, states under `presentation/models`
- `domain/` — entities, repository interfaces, use case classes, work wrappers (`domain/work`)
- `data/` — repository implementations, Room (`.data/db`), network (`.data/network`), parsers
- `di/` — Hilt modules
- `services/` — services and Workers (InstallGame, ScanGamesWorker, UpdateRepositoryWorker)

## Branches

- `master` — main branch. Current UI: Android Views/XML.
- `compose` — work-in-progress port of the UI to Jetpack Compose (Navigation Compose, Hilt Navigation Compose, Coil Compose). Not merged yet; the migration is planned for later. Conventions listed below describe `master`—expect them to shift when the Compose port lands.
- `SDL3` — active branch for native SDL3 development.

## Build

```sh
# 1) Download native dependencies (INSTEAD, LuaJIT, libiconv, SDL3) — mandatory
./gradlew :instead:downloadDependencies
# 2) Build and install the debug APK
./gradlew assembleDebug
./gradlew installDebug
```

Or `make all` (README: `deps` → `build` → `install`).

Important details:
- Native sources (SDL3, INSTEAD, etc.) are **downloaded into `instead/src/main/c/...`** and are not stored in git (see `.gitignore`). Do not edit them — they get overwritten on the next build. Custom code lives in `instead/src/main/c/Instead/instead_launcher.c`.
- The `lang/`, `themes/` and `stead/` resources under `instead/src/main/assets/` are generated from the downloaded INSTEAD by the `copyLangs`/`copyThemes`/`copyStead` tasks.
- LuaJIT is built with the `:instead:buildLuaJit` task (cross-compiled with GNU `make` and the NDK toolchain directly in Gradle, using injected `ExecOperations`) and is wired into `preBuild`.
- Default ABIs: `arm64-v8a,armeabi-v7a,x86_64`. `armeabi-v7a` (and `x86`) require a 32-bit host C compiler for LuaJIT (`gcc-multilib`/`libc6-dev-i386` on Debian/Ubuntu). To override: `./gradlew assembleDebug -PabiFilters="arm64-v8a,armeabi-v7a,x86,x86_64"`.
- `keystore.properties` (repo root, not in git) controls release signing; without it the signingConfig is set to null.
- Build config versions (SDK/NDK versions, app versionCode/versionName, INSTEAD version) are set in `gradle/libs.versions.toml` (`minSdk`, `compileSdk`, `ndk`, `appVersionCode`, `appVersionName`, `insteadVersion`).
- SDL3/INSTEAD/LuaJIT/Lua versions are set by the `downloadSdl`/`downloadInstead` tasks in `instead/build.gradle.kts`.

## Tests

JUnit 5 (plugin `de.mannodermaus.android-junit5`):

```sh
./gradlew :app:testDebugUnitTest
```

Tests live in `app/src/test/kotlin/` (see `GameParserImplTest`). Test resources (`testgame`, `testgame.zip`) sit next to them in `app/src/test/resources`. There are no instrumented tests.

## Lint / style

- `lint { abortOnError false }` — lint does not block the build.
- Kotlin style: standard (4 spaces, trailing commas).
- `kotlin`-stdlib version and `ksp` (KSP2) are used for both Hilt and Room in the JVM modules.
- NEVER add comments unless necessary; the codebase does contain comments — keep them in place.
- File header comment format: `Copyright (c) <year> Boris Timofeev <btimofeev@emunix.org>` + MIT license. Use the current year for new files.

## Code conventions

- MVVM: a Fragment subscribes to the ViewModel's `StateFlow` via `viewModels()`/`by viewModels()`, the state is an immutable data class.
- Repositories are defined as interfaces in `domain/repository`, implementations in `data/repository`.
- Use case classes live in `domain/usecase` (`...UseCase` interface + `...UseCaseImpl`).
- Dependency injection is exclusively via Hilt (`@Inject constructor`, `@AndroidEntryPoint`, `di/*Module.kt`).
- ViewBinding is enabled; use the ViewBindingPropertyDelegate (`viewbindingpropertydelegate-noreflection`).
- WorkManager jobs are run through wrapper classes in `domain/work`; the Workers themselves live in `services/`.
- Logging: Timber. Crash reports: ACRA (release builds only).
- Launching a game: `InsteadApi.startGame(gameName, playFromBeginning)` → `InsteadActivity` (SDL).

## CI

GitHub Actions: `.github/workflows/android.yml` — builds a debug APK for all ABIs, caches the downloaded SDL3, uploads the artifact. Runs on push/PR against the `SDL3`, `master` and `main` branches and on `v*` tags. The main branch is `master`; `SDL3` is the active branch for native SDL3 development. The `compose` branch is not yet covered by CI triggers.

## Misc

- `fastlane/metadata/android/{en-US,ru-RU}` — changelogs and the Google Play listing (changelog files are named by versionCode, e.g. `90200.txt`).
- `CHANGELOG.md` follows the Keep a Changelog format (a "Development" section on top).
- String translations: `app/src/main/res/values*/strings.xml`.