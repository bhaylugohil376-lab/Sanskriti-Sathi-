# Basic Android ProGuard Rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Preserve WebInterface & WebView calls
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Application Classes
-keep class com.sanskritisathi.app.** { *; }
