plugins {
    id("com.android.application")
    // AGP 9.0+ has built-in Kotlin support
    // Kotlin 2.3.0+ is required for JVM target 25 support
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.bibichan.union.player"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.bibichan.union.player"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Signing config properties are set via gradle.properties or environment variables
            // GitHub Actions sets these in the workflow
            // Fallback to debug keystore for local builds without signing config
            storeFile = if (System.getenv("KEYSTORE_PATH") != null) {
                file(System.getenv("KEYSTORE_PATH"))
            } else if (System.getProperty("release.storeFile") != null) {
                file(System.getProperty("release.storeFile"))
            } else {
                // Use debug keystore as fallback for local testing
                file("debug.keystore")
            }
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: System.getProperty("release.storePassword") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: System.getProperty("release.keyAlias") ?: "androiddebugkey"
            keyPassword = System.getenv("KEY_PASSWORD") ?: System.getProperty("release.keyPassword") ?: "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            // AGP automatically creates 'debug' signing config
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
    }
}

dependencies {
    // AndroidX core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")

    // Material 3 - Modern design system
    implementation("androidx.compose.material3:material3:1.3.1")

    // Jetpack Compose UI
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")

    // Navigation - For switching between screens
    implementation("androidx.navigation:navigation-compose:2.8.8")

    // Icons - For navigation icons
    // Note: material-icons-extended provides a comprehensive set of icons
    // For future optimization, consider using only specific icon imports
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Image loading - For album covers (Coil)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
}
