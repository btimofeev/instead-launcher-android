# Core Rules
- Language: code and responses in English.
- Output: extremely concise. Code only, no explanations unless requested.
- Comments: never add unless necessary; never delete existing ones.
- New file header: `Copyright (c) <year> Boris Timofeev <btimofeev@emunix.org>` + MIT, current year.
- Commit: one short lowercase imperative line, no body (e.g. "hide download cancel button while a game is being deleted"). `master` is the only branch.
- Style: 4 spaces, trailing commas. DI: Hilt only. Logging: Timber.

# Project
INSTEAD Launcher — Android app for downloading/running games for the "INSTEAD" engine. Native engine + SDL3 built from source in `:instead`.
Kotlin, Jetpack Compose, MVVM + Clean Architecture, singleton activity, Hilt (KSP), Room (KSP), WorkManager, JUnit 5. `minSdk 25`, `compile/targetSdk 36`.

Layers in `app/src/main/kotlin/org/emunix/insteadlauncher/`: `presentation/` (screens, ViewModels, navigation), `domain/` (entities, repository interfaces, usecases, `domain/work`), `data/` (repo impls, Room, network), `di/`, `services/` (Workers).
Modules: `app`, `instead` (native + `InsteadActivity`), `sdl-activity`, `scancode-generator`, `core-storage`, `core-preferences`.

# Build & Testing
```sh
./gradlew :instead:downloadDependencies   # mandatory, once
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest          # tests in app/src/test/kotlin/
```
- Native sources (SDL3, INSTEAD, LuaJIT, libiconv) are downloaded into `instead/src/main/c/...`, not in git. NEVER edit them — overwritten on build. Own engine code: `instead/src/main/c/Instead/instead_launcher.c`.
- 32-bit ABIs need a 32-bit host compiler (`gcc-multilib`); default ABIs: `arm64-v8a,armeabi-v7a,x86_64`.
- Versions live in `gradle/libs.versions.toml`; do not touch build files or add dependencies autonomously — ask first.
- Lint does not block the build (`abortOnError false`), but run `lint` before handing in.

# Architecture
- Screen collects ViewModel `StateFlow` via `collectAsStateWithLifecycle()`; ViewModel via `hiltViewModel()`; state = immutable data class.
- Repositories: interface in `domain/repository`, impl in `data/repository`. Usecases: `...UseCase` + `...UseCaseImpl` in `domain/usecase`.
- WorkManager: wrappers in `domain/work`, Workers in `services/`.
- Game launch: `InsteadApi.startGame(...)` → `InsteadActivity`.
- Composable stateless, state hoisted; `modifier: Modifier = Modifier` first optional param.

# Settings over device checks
**Behaviour via settings, never `if (isTv())`.** Device-dependent defaults: resolver in `GameDefaultsImpl` (interface `GameDefaultsApi`) picks a default and writes it to `PreferencesProvider` only if the user hasn't chosen. New default = `isXxxSet` in `PreferencesProvider` + `resolveXxx()` in `GameDefaultsApi` + consume `resolveXxx()`, never `preferencesProvider` directly. Cover in `GameDefaultsImplTest`. TV-only UI diffs → `presentation/tv/`.

# Misc
- Release signing: `keystore.properties` (not in git); without it signingConfig = null.
- Changelog: `CHANGELOG.md` (Keep a Changelog, "Development" on top) + `fastlane/metadata/android/{en-US,ru-RU}` files named by versionCode (e.g. `90200.txt`).
- Translations: `app/src/main/res/values*/strings.xml`.
