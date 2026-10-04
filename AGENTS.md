# AGENTS.md

## Project overview

INSTEAD Launcher is an Android app for downloading and running games for the "INSTEAD" text-quest engine. The native engine and SDL3 are built from source directly in this project (the `:instead` module).

- The JVM-layer language is Kotlin. The UI is built with Jetpack Compose.
- Architecture: MVVM + Clean Architecture + `singleton android activity`.
- DI: Hilt (KSP). DB: Room (KSP). Background work: WorkManager + foreground services.
- SDK versions: `minSdk 25`, `compile/targetSdk 36`, `ndk 29.0.14206865`, Java 21 bytecode (source/target), Gradle runs on JDK 25.

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
- `presentation/` — Compose screens, ViewModels, navigation (`presentation/navigation`), theme (`presentation/theme`), states under `presentation/models`
- `domain/` — entities, repository interfaces, use case classes, work wrappers (`domain/work`)
- `data/` — repository implementations, Room (`.data/db`), network (`.data/network`), parsers
- `di/` — Hilt modules
- `services/` — services and Workers (InstallGame, ScanGamesWorker, UpdateRepositoryWorker)

## Branches

- `master` — the only branch. UI: Jetpack Compose; native SDL3 development happens here too.

## Commits

- Commit messages are a single short lowercase imperative line, like the existing history ("hide download cancel button while a game is being deleted"). Do not add a body explaining what changed and why unless the user asks for one.

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
- SDL3/INSTEAD/LuaJIT/Lua versions are set by the `downloadSdl`/`downloadInstead` tasks in `instead/build.gradle.kts`. INSTEAD is currently pinned to a commit (`insteadRef`, a `codeload.github.com` archive) rather than a release tarball, because `-kbd` only exists after 3.6.0; switch it back to the release tarball once 3.6.1 is out.

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

- MVVM: a Compose screen subscribes to the ViewModel's `StateFlow` via `collectAsStateWithLifecycle()`, the ViewModel is obtained with `hiltViewModel()`, the state is an immutable data class.
- Repositories are defined as interfaces in `domain/repository`, implementations in `data/repository`.
- Use case classes live in `domain/usecase` (`...UseCase` interface + `...UseCaseImpl`).
- Dependency injection is exclusively via Hilt (`@Inject constructor`, `@AndroidEntryPoint`, `di/*Module.kt`).
- Compose Compiler: the Compose Compiler Gradle plugin is applied (`org.jetbrains.kotlin.plugin.compose`), version tied to the built-in Kotlin in AGP 9. Navigation: Navigation Compose + Hilt Navigation Compose. Images: Coil 3 (`coil-compose`).
- WorkManager jobs are run through wrapper classes in `domain/work`; the Workers themselves live in `services/`.
- Logging: Timber. Crash reports: ACRA (release builds only).
- Launching a game: `InsteadApi.startGame(gameName, playFromBeginning)` → `InsteadActivity` (SDL).

## Settings-driven behaviour (prefer settings over branches)

- **Prefer expressing behaviour through settings over `if (isTv())` / device checks in code.** Every extra device-type branch is duplicated logic; a preference is read in exactly one place and stays overridable by the user.
- Device-type-dependent defaults are resolved in **`GameDefaultsImpl`** (`instead/src/main/kotlin/org/emunix/instead/GameDefaultsImpl.kt`, interface `instead_api/GameDefaultsApi.kt`, bound in `app/di/AppModule.kt`). It picks a sane default for the current device class (phone / tablet / TV / appliance — see `isLargeScreen()`, `isTelevision()`), writes it into `PreferencesProvider` **only if the user has not chosen a value yet**, and returns it:
  - `resolveTheme()` → wide theme on large screens, mobile otherwise;
  - `resolveTextScale()` → enlarged text on large screens;
  - `resolveKeyboardButtonPosition()` → `KEYBOARD_DO_NOT_SHOW_BUTTON` on TV, corner position elsewhere;
  - `resolveKeyboardMode()` → INSTEAD `kbd` mode (`0` smart) on TV, empty string elsewhere — there is no launcher preference for it, so unlike the resolvers above it writes nothing into `PreferencesProvider` and simply leaves the mode alone when it returns an empty string.
- Pattern for every "default depends on the device" setting:
  1. add `val isXxxSet: Boolean` to `PreferencesProvider` (`preferences.contains(PREF_X_KEY)`) next to the setting;
  2. add `resolveXxx(): String` to `GameDefaultsApi` and implement it in `GameDefaultsImpl` guarded by `isXxxSet`;
  3. consume `gameDefaultsApi.resolveXxx()` where the value is needed (e.g. `InsteadActivity`), not `preferencesProvider` directly.
- Consumers must go through `resolveXxx()`; the static default in `PreferencesProviderImpl` is only a last-resort fallback.
- Cover new resolvers in `app/src/test/kotlin/org/emunix/instead/GameDefaultsImplTest.kt` (phone / tablet / TV / user-picked value / idempotency).
- TV-only *UI* differences (hiding settings rows, extra settings rows) still belong to `presentation/tv/` (`TvSettingsScreen.filterForTv()`), not to the engine modules.
- Never write a user's preference from a `LaunchedEffect`/composable just to simulate a default — that is what the resolver is for.

## CI

GitHub Actions: `.github/workflows/android.yml` — builds a debug APK for all ABIs, runs JVM unit tests, caches the downloaded native dependencies (SDL3, INSTEAD, LuaJIT, libiconv), uploads the artifact. Runs on push/PR against the `master` and `main` branches and on `v*` tags. The main branch is `master`.

## Misc

- `fastlane/metadata/android/{en-US,ru-RU}` — changelogs and the Google Play listing (changelog files are named by versionCode, e.g. `90200.txt`).
- `CHANGELOG.md` follows the Keep a Changelog format (a "Development" section on top).
- String translations: `app/src/main/res/values*/strings.xml`.