# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep jaudiotagger classes that are used
-dontwarn javax.swing.**
-dontwarn java.awt.**
-dontwarn org.jaudiotagger.**

# Keep all jaudiotagger audio format handlers
-keep class org.jaudiotagger.** { *; }

# Keep mp3agic classes
-keep class com.mpatric.mp3agic.** { *; }

# General Android recommendations
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.view.View

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}