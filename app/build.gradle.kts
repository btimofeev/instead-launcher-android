import java.util.Properties

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("de.mannodermaus.android-junit5")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        applicationId = "org.emunix.insteadlauncher"
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    ndkVersion = libs.versions.ndk.get()
    signingConfigs {
        create("release")
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "INSTEAD_VERSION", "\"${libs.versions.insteadVersion.get()}\"")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "INSTEAD_VERSION", "\"${libs.versions.insteadVersion.get()}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = false
    }
    namespace = "org.emunix.insteadlauncher"
}

val props = Properties()
val propFile = rootProject.file("keystore.properties")
if (propFile.canRead()) {
    props.load(propFile.inputStream())

    if (props.containsKey("storeFile") && props.containsKey("storePassword") &&
        props.containsKey("keyAlias") && props.containsKey("keyPassword")) {

        println("Release build signing")

        val releaseConfig = android.signingConfigs.getByName("release")
        releaseConfig.storeFile = file(props["storeFile"] as String)
        releaseConfig.storePassword = props["storePassword"] as String
        releaseConfig.keyAlias = props["keyAlias"] as String
        releaseConfig.keyPassword = props["keyPassword"] as String
    } else {
        println("Release build not found signing properties")

        android.buildTypes.getByName("release").signingConfig = null
    }
} else {
    println("Release build not found signing file")
    android.buildTypes.getByName("release").signingConfig = null
}

dependencies {
    implementation(project(":core-storage"))
    implementation(project(":core-preferences"))
    implementation(project(":instead"))

    // Support libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // Architecture components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.extensions)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Network
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Files
    implementation(libs.commons.io)

    // Crash reports
    implementation(libs.acra.mail)
    implementation(libs.acra.notification)

    // Logging
    implementation(libs.timber)

    // Test
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

repositories {
    mavenCentral()
}