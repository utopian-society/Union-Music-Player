# Union Music Player - ProGuard Rules
# Fixed for R8 compatibility with Jetpack Compose

# ═══════════════════════════════════════════════════════════════
# ANDROIDX & COMPOSE - Keep essential framework classes
# ═══════════════════════════════════════════════════════════════

# Keep Compose runtime and core classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Navigation components
-keep class androidx.navigation.** { *; }
-keepclassmembers class androidx.navigation.** { *; }

# Keep Material Icons - Required for Icons.*.* usage
# This prevents R8 from removing icon classes used at runtime
-keep class androidx.compose.material.icons.** { *; }
-dontwarn androidx.compose.material.icons.**

# Keep Material 3 components
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class androidx.compose.material3.** { *; }

# Keep AndroidX core components
-keepattributes *Annotation*
-keepclassmembers class * extends android.app.Activity { *; }
-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.view.View { *; }

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ═══════════════════════════════════════════════════════════════
# AUDIO LIBRARIES - Only keep if actually used
# ═══════════════════════════════════════════════════════════════

# JAudioTagger - Using -dontwarn to allow missing classes
-dontwarn javax.swing.**
-dontwarn java.awt.**
-dontwarn org.jaudiotagger.**

# Mp3agic - Using -dontwarn to allow missing classes
-dontwarn com.mpatric.mp3agic.**

# ═══════════════════════════════════════════════════════════════
# COIL IMAGE LOADING
# ═══════════════════════════════════════════════════════════════

# Keep Coil classes for image loading
-keep class coil.** { *; }
-keep class coil3.** { *; }
-dontwarn coil.**
-dontwarn coil3.**

# ═══════════════════════════════════════════════════════════════
# SIZE OPTIMIZATIONS
# ═══════════════════════════════════════════════════════════════

# Remove debug attributes in release builds
-optimizations !code/simplification/various,!code/allocation/*
-allowaccessmodification

# ═══════════════════════════════════════════════════════════════
# R8 SPECIFIC OPTIMIZATIONS
# ═══════════════════════════════════════════════════════════════

# Keep generic signature information
-keepattributes Signature

# Keep line number table for crash reporting
-keepattributes SourceFile,LineNumberTable
