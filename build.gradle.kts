// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9.0+ has built-in Kotlin support, so we don't need to declare kotlin-android plugin here
// Kotlin 2.3.0+ is required for JVM target 25 support
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
