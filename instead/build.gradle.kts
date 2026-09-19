import java.util.Locale
import javax.inject.Inject

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.download.plugin)
}

interface InsteadBuildOps {
    @get:Inject
    val execOps: ExecOperations

    @get:Inject
    val fs: FileSystemOperations
}

data class ArchInfo(val triple: String, val ljarch: String)

data class SdlLib(val repo: String, val ref: String, val dest: String)

data class SdlDep(val repo: String, val sha: String)

val abis = (rootProject.findProperty("abiFilters") as? String ?: "arm64-v8a,x86_64").split(",")

android {

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // LuaJIT can only be cross-built here for 64-bit ABIs.
            abiFilters.addAll(abis)
        }
        externalNativeBuild {
            cmake {
                cppFlags += ""
                arguments += listOf("-DANDROID_PLATFORM=android-25", "-DCMAKE_BUILD_TYPE=Release",
                        "-DANDROID_ARM_MODE=arm")
                version = "3.31.0"
            }
        }
        lint {
            abortOnError = false
        }

        consumerProguardFiles("consumer-rules.pro")
    }

    ndkVersion = libs.versions.ndk.get()

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "INSTEAD_VERSION", "\"${libs.versions.insteadVersion.get()}\"")
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            buildConfigField("String", "INSTEAD_VERSION", "\"${libs.versions.insteadVersion.get()}\"")
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
            jniLibs.srcDir("src/main/c/luajit-libs")
        }
        getByName("androidTest") {
            java.srcDir("src/androidTest/kotlin")
        }
        getByName("test") {
            java.srcDir("src/test/kotlin")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/c/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "org.emunix.instead"
}

dependencies {
    implementation(project(":core-storage"))
    implementation(project(":core-preferences"))
    implementation(project(":sdl-activity"))

    implementation(libs.androidx.appcompat)

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}

val downloadExt = project.extensions.getByName("download") as de.undercouch.gradle.tasks.download.DownloadExtension

tasks.register<Delete>("deleteDependencies") {
    delete("src/main/c/Instead/Instead", "src/main/c/LuaJIT")
    delete("src/main/c/luajit-libs")
    delete("src/main/c/SDL3/SDL3", "src/main/c/SDL3_image/SDL3_image")
    delete("src/main/c/SDL3_mixer/SDL3_mixer", "src/main/c/SDL3_ttf/SDL3_ttf")
    delete("src/main/c/libiconv/libiconv")
}

tasks.register("downloadInstead") {
    val insteadRef = "b755e7f379deda34deb7dbf0e0b5b30d903a1875"
    val luaJitRef = "c6ffc141a8762b41703f9287d63d93622a13dd8f"
    val iconv = "1.15"
    val downloadDir = project.layout.buildDirectory.get().asFile.path

    doLast {
        project.delete("src/main/c/Instead/Instead", "src/main/c/LuaJIT", "src/main/c/libiconv/libiconv")

        downloadExt.run {
            src("https://github.com/instead-hub/instead/archive/${insteadRef}.tar.gz")
            dest(project.file("$downloadDir/instead-$insteadRef.tar.gz"))
        }
        project.copy {
            from(project.tarTree(project.resources.gzip("$downloadDir/instead-$insteadRef.tar.gz")))
            into("src/main/c/Instead/")
        }
        project.delete("$downloadDir/instead-$insteadRef.tar.gz")
        project.file("src/main/c/Instead/instead-$insteadRef").renameTo(project.file("src/main/c/Instead/Instead"))
        project.delete("src/main/c/Instead/instead-$insteadRef")

        downloadExt.run {
            src("https://github.com/LuaJIT/LuaJIT/archive/${luaJitRef}.tar.gz")
            dest(project.file("$downloadDir/LuaJIT-$luaJitRef.tar.gz"))
        }
        project.copy {
            from(project.tarTree(project.resources.gzip("$downloadDir/LuaJIT-$luaJitRef.tar.gz")))
            into("src/main/c/")
        }
        project.delete("$downloadDir/LuaJIT-$luaJitRef.tar.gz")
        val luaJitDir = "LuaJIT-$luaJitRef"
        project.file("src/main/c/$luaJitDir").renameTo(project.file("src/main/c/LuaJIT"))
        project.delete("src/main/c/$luaJitDir")

        downloadExt.run {
            src("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-$iconv.tar.gz")
            dest(project.file("$downloadDir/libiconv-$iconv.tar.gz"))
        }
        project.copy {
            from(project.tarTree(project.resources.gzip("$downloadDir/libiconv-$iconv.tar.gz")))
            into("src/main/c/libiconv/")
        }
        project.delete("$downloadDir/libiconv-$iconv.tar.gz")
        project.file("src/main/c/libiconv/libiconv-$iconv").renameTo(project.file("src/main/c/libiconv/libiconv"))
        project.delete("src/main/c/libiconv/libiconv-$iconv")
    }
}

tasks.register("downloadSdl") {
    val sdl3 = "release-3.4.10"
    val sdl3Image = "release-3.4.6"
    val sdl3Mixer = "release-3.2.4"
    val sdl3Ttf = "release-3.2.2"
    val downloadDir = project.layout.buildDirectory.get().asFile.path

    val sdl = listOf(
        SdlLib("SDL", sdl3, "SDL3/SDL3"),
        SdlLib("SDL_image", sdl3Image, "SDL3_image/SDL3_image"),
        SdlLib("SDL_mixer", sdl3Mixer, "SDL3_mixer/SDL3_mixer"),
        SdlLib("SDL_ttf", sdl3Ttf, "SDL3_ttf/SDL3_ttf"),
    )
    val sdlDeps = mapOf(
        "SDL3_image/SDL3_image/external" to listOf(
            SdlDep("jpeg", "9db4612eee1913e689f58ce62cd3b8708b290fd3"),
            SdlDep("libpng", "4b9e071b2fc4216369372a3ed260d335dd036f15"),
            SdlDep("libwebp", "0b4545ee8210ff72ec47ab1ad56c2e7460663d11"),
            SdlDep("zlib", "0e68590d11e618d60866aa86629fbda128bc068a"),
        ),
        "SDL3_mixer/SDL3_mixer/external" to listOf(
            SdlDep("flac", "833a260a7bc29f80899d33e8c1beb2fc56b4905b"),
            SdlDep("ogg", "936fdd822a4e4fc963197b3eda931b89b52859a0"),
            SdlDep("vorbis", "eefe62244630a2d2c37ba5e6fb1a1fd8ad93ece5"),
            SdlDep("mpg123", "6e97a7fdf112a950977dad73de10be0d17ddb6b8"),
            SdlDep("libxmp", "75e85f8a5be65f9f95fb946086bee17da158166f"),
        ),
        "SDL3_ttf/SDL3_ttf/external" to listOf(
            SdlDep("freetype", "9973564cfa63763a3e4ac67c09147899539b1e07"),
        ),
    )

    doLast {
        fun fetchTarball(owner: String, repo: String, ref: String, destRel: String) {
            val archive = project.file("$downloadDir/$repo-$ref.tar.gz")
            val destDir = project.file("src/main/c/$destRel")
            project.delete(destDir)
            downloadExt.run {
                src("https://github.com/$owner/$repo/archive/$ref.tar.gz")
                dest(archive)
            }
            project.copy {
                from(project.tarTree(project.resources.gzip(archive)))
                into(destDir.parentFile)
            }
            project.delete(archive)
            project.file("${destDir.parentFile}/$repo-$ref").renameTo(destDir)
            project.delete("${destDir.parentFile}/$repo-$ref")
        }

        sdl.forEach { lib -> fetchTarball("libsdl-org", lib.repo, lib.ref, lib.dest) }
        sdlDeps.forEach { (dest, deps) ->
            deps.forEach { dep -> fetchTarball("libsdl-org", dep.repo, dep.sha, "$dest/${dep.repo}") }
        }
    }
}

tasks.register("downloadDependencies") {
    description = "Downloads INSTEAD, LuaJIT, libiconv and the SDL3 libraries"
    dependsOn("downloadInstead", "downloadSdl")
}

tasks.register("buildLuaJit") {
    description = "Builds libluajit.so for the selected ABI(s)"
    val ljSrc = project.file("src/main/c/LuaJIT")
    val outDir = project.file("src/main/c/luajit-libs")
    val workDir = project.layout.buildDirectory.dir("luajit").get().asFile
    val api = libs.versions.minSdk.get()
    val ndk = android.ndkDirectory
    val ops = project.objects.newInstance(InsteadBuildOps::class.java)

    val archInfo = mapOf(
        "arm64-v8a" to ArchInfo("aarch64-linux-android", "arm64"),
        "x86_64" to ArchInfo("x86_64-linux-android", "x64"),
        "armeabi-v7a" to ArchInfo("armv7a-linux-androideabi", "arm"),
        "x86" to ArchInfo("i686-linux-android", "x86"),
    )

    val osName = System.getProperty("os.name").lowercase(Locale.ROOT)
    val hostDir = when {
        osName.contains("mac") -> {
            val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)
            if (arch == "aarch64" || arch == "arm64") "darwin-arm64" else "darwin-x86_64"
        }
        osName.contains("windows") -> "windows-x86_64"
        else -> "linux-x86_64"
    }
    val tc = project.file("$ndk/toolchains/llvm/prebuilt/$hostDir")
    fun resolveTool(name: String): String {
        val candidates = listOf(".exe", ".cmd", "").map { project.file("$tc/bin/$name$it") }
        return candidates.firstOrNull { it.exists() }?.path ?: project.file("$tc/bin/$name").path
    }

    inputs.dir(ljSrc)
    inputs.property("abis", abis.joinToString(","))
    outputs.dir(outDir)
    doLast {
        if (!File(ljSrc, "src/Makefile").exists()) {
            throw GradleException("LuaJIT sources are missing: run :instead:downloadDependencies first")
        }
        abis.forEach { abi ->
            val info = archInfo[abi]
                ?: throw GradleException("Unsupported ABI: $abi")
            // buildvm must match the target pointer size, so 32-bit targets need
            // a 32-bit host compiler (gcc-multilib on Debian/Ubuntu).
            val hostCc = if (abi == "arm64-v8a" || abi == "x86_64") "cc" else "gcc -m32"
            val clang = resolveTool("${info.triple}${api}-clang")
            val ar = resolveTool("llvm-ar")
            val strip = resolveTool("llvm-strip")
            val buildDir = project.file("$workDir/$abi")
            val jobs = Runtime.getRuntime().availableProcessors()

            ops.fs.delete {
                delete(buildDir)
            }
            ops.fs.copy {
                from(ljSrc)
                into(buildDir)
            }
            ops.execOps.exec {
                commandLine("make", "-C", project.file("$buildDir/src").absolutePath, "-j$jobs",
                        "HOST_CC=$hostCc",
                        "CC=$clang",
                        "TARGET_CC=$clang",
                        "TARGET_LD=$clang",
                        "TARGET_AR=$ar rcus",
                        "TARGET_STRIP=$strip",
                        "TARGET_SYS=Linux",
                        "TARGET_LJARCH=${info.ljarch}",
                        "TARGET_SONAME=libluajit.so",
                        "TARGET_FLAGS=-O2 -fPIC",
                        "TARGET_LDFLAGS=-Wl,-z,max-page-size=16384",
                        "BUILDMODE=shared")
            }
            ops.fs.copy {
                from(project.file("$buildDir/src/libluajit.so"))
                into(project.file("$outDir/$abi"))
            }
        }
    }
}

tasks.register<Copy>("copyLangs") {
    description = "Copies langs from Instead to assets"
    from("src/main/c/Instead/Instead/lang/")
    into("src/main/assets/lang/")
    include("*.ini")
}

tasks.register<Copy>("copyThemes") {
    description = "Copies themes from Instead to assets"
    from("src/main/c/Instead/Instead/themes/")
    into("src/main/assets/themes/")
    exclude("CMakeLists.txt", "Makefile")
}

tasks.register<Copy>("copyStead") {
    description = "Copies stead folder from Instead to assets"
    from("src/main/c/Instead/Instead/stead/")
    into("src/main/assets/stead/")
    exclude("**/CMakeLists.txt", "Makefile")
}

tasks.register<Delete>("cleanLuaJit") {
    delete("src/main/c/luajit-libs")
}

tasks.register<Delete>("cleanAssets") {
    delete("src/main/assets/lang/", "src/main/assets/themes/", "src/main/assets/stead/")
}

tasks.register<Delete>("fdroidRemoveBinaries") {
    delete("src/main/c/Instead/Instead/contrib/")
    delete("src/main/c/SDL3/SDL3/Xcode/")
    delete("src/main/c/SDL3_image/SDL3_image/Xcode/")
    delete("src/main/c/SDL3_mixer/SDL3_mixer/Xcode/")
    delete("src/main/c/SDL3_ttf/SDL3_ttf/Xcode/")
    delete("src/main/c/libiconv/libiconv/tests/")
}

tasks.named("preBuild") {
    dependsOn("copyLangs", "copyThemes", "copyStead", "buildLuaJit")
}

// LuaJIT must exist before the native INSTEAD library is configured/linked.
tasks.configureEach {
    if (name.startsWith("configureCMake") || name.startsWith("buildCMake")) {
        dependsOn("buildLuaJit")
    }
}

afterEvaluate {
    if (project.hasProperty("packageRelease")) {
        (project.property("packageRelease") as Task).finalizedBy("cleanAssets")
    }
    if (project.hasProperty("packageDebug")) {
        (project.property("packageDebug") as Task).finalizedBy("cleanAssets")
    }
    if (project.hasProperty("clean")) {
        (project.property("clean") as Task).finalizedBy("cleanAssets", "cleanLuaJit")
    }
}