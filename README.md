## INSTEAD Launcher for Android
This application allows you to download and run games for the popular text quest engine "INSTEAD" on Android.

### Install

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/org.emunix.insteadlauncher/)
[<img src="/images/badges/google-play.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=org.emunix.insteadlauncher)
[<img src="/images/badges/github.png"
     alt="Get it on Github"
     height="80">](https://github.com/btimofeev/instead-launcher-android/releases)

### Screenshots

<img src="/images/screenshots/screenshot_1.png" width="270"> <img src="/images/screenshots/screenshot_2.png" width="270"> <img src="/images/screenshots/screenshot_3.png" width="270">

### How to compile
To compile the application, you need to run these commands in the root directory of the project:
```
./gradlew :instead:downloadDependencies
./gradlew assembleDebug
./gradlew installDebug
```

or just run ```make``` which will run the three commands above in sequence.

On Windows, `make` and a C host compiler are not available by default. Install
[MSYS2](https://www.msys2.org/) (`pacman -S make gcc`) and add the MSYS2 binary
directories (e.g. `C:\msys64\usr\bin` and `C:\msys64\mingw64\bin`) to `PATH`.
Only 64-bit ABIs (`arm64-v8a`, `x86_64`) can be built on Windows; the 32-bit
`armeabi-v7a` ABI requires a Linux host.

On Linux, 32-bit ABIs are built by default (together with `arm64-v8a` and
`x86_64`). They need a 32-bit C host compiler to build LuaJIT, so install it
first. On Debian/Ubuntu and derivatives:

```
sudo apt-get install gcc-multilib libc6-dev-i386
```

Alternatively, build in a Docker container to keep the host clean
([Dockerfile](docker/Dockerfile), no host NDK/SDK needed):

```
./docker/build.sh
```

The first run builds the `instead-builder` image automatically. The debug APK
is placed at `app/build/outputs/apk/debug/`. For details run
`./docker/build.sh --help`.

### License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
