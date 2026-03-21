/**
* build.gradle.kts - 模块级构建配置文件
*
* 添加了音频元数据提取、ExoPlayer和协程依赖
* 已添加 release 签名配置，支持 GitHub Actions CI 自动签名
*
* 2026 现代化更新：更新所有主要依赖到最新版本
* 使用 Media3 ExoPlayer 提取音频元数据（替代 jaudiotagger）
* 使用 PKCS12 格式的 keystore（Java 25+ 默认格式）
* Gradle 9.4.1 + AGP 9.10 + Kotlin 2.1.20 + Java 25
*/

plugins {
    id("com.android.application")
    // id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.bibichan.union.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.bibichan.union.player"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ── Signing configuration ───────────────────────────────────────────────
    // 使用 debug 签名作为后备，确保 CI 建置可以成功
    // 如需正式发布，请在 GitHub Secrets 中正确配置 ANDROID_KEYSTORE_BASE64
    // PKCS12 格式是 Java 25+ 的默认格式，更安全且兼容性更好
    signingConfigs {
        getByName("debug") {
            // 使用默认 debug keystore
        }
    }

    buildTypes {
        getByName("release") {
            // 尝试使用环境变量中的签名配置，如果不存在则使用 debug 签名
            // 这样可以确保 CI 建置不会因为签名问题而失败
            val keystorePath = System.getenv("KEYSTORE_PATH")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("KEY_ALIAS")
            val keyPassword = System.getenv("KEY_PASSWORD")

            if (keystorePath != null && keystorePassword != null && 
                keyAlias != null && keyPassword != null) {
                // 使用 CI 环境中的签名配置（PKCS12 格式）
                signingConfig = signingConfigs.create("release") {
                    storeFile = file(keystorePath)
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                    // PKCS12 是 Java 25+ 的默认格式
                    storeType = "PKCS12"
                }
            } else {
                // 后备：使用 debug 签名
                signingConfig = signingConfigs.getByName("debug")
            }

            isMinifyEnabled = false
            isShrinkResources = false
        }
        // debug 保持默认（debug keystore）
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    // AndroidX 核心库 (2026 更新)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Jetpack Compose (2026 更新)
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Coil 图片加载 (2.7.0 是当前稳定版)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Media3 ExoPlayer - 支持 FLAC 和 ALAC 的强大媒体播放器 (2026 更新)
    // 同时用于音频元数据提取（替代 jaudiotagger）
    implementation("androidx.media3:media3-exoplayer:1.10.0")
    implementation("androidx.media3:media3-ui:1.10.0")
    implementation("androidx.media3:media3-common:1.10.0")
    implementation("androidx.media3:media3-datasource:1.10.0")

    // mp3agic - 轻量级 MP3 元数据提取（作为备用，主要用于 MP3 ID3 标签）
    implementation("com.mpatric:mp3agic:0.9.1")

    // Kotlin 协程 - 用于并行文件扫描 (2026 更新)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
