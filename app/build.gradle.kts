/**
 * build.gradle.kts - 模块级构建配置文件
 *
 * 添加了音频元数据提取、ExoPlayer和协程依赖
 * 已添加 release 签名配置，支持 GitHub Actions CI 自动签名
 *
 * 2026 现代化更新：更新所有主要依赖到最新版本
 * 使用 Media3 ExoPlayer 提取音频元数据（替代 jaudiotagger）
 */

plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
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
    signingConfigs {
        create("release") {
            // These values come from GitHub Actions secrets / environment variables
            // In local development → release build will fail intentionally (no secrets)
            storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            // 使用 CI 环境中的签名配置
            signingConfig = signingConfigs.getByName("release")
            // 使用 Media3 ExoPlayer 提取元数据，无 javax.swing 依赖问题
            isMinifyEnabled = false
            isShrinkResources = false
        }
        // debug 保持默认（debug keystore）
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
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

    // Coil 图片加载 (2.7.0 是当前稳定版，3.x 仍在开发中)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Media3 ExoPlayer - 支持 FLAC 和 ALAC 的强大媒体播放器 (2026 更新)
    // 同时用于音频元数据提取（替代 jaudiotagger）
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-common:1.5.1")
    implementation("androidx.media3:media3-datasource:1.5.1")

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
</Content>
</write_to_file>