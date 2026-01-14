# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class androidx.datastore.preferences.PreferencesProto$* { *; }
-keepclassmembers class androidx.datastore.preferences.PreferencesProto$* { *; }
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Kotlinx Serialization - Keep class names for reflection
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Serializers
-keep,includedescriptorclasses class in.hridayan.ashell.**$$serializer { *; }
-keepclassmembers class in.hridayan.ashell.** {
    *** Companion;
}
-keepclasseswithmembers class in.hridayan.ashell.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all enums used in serializable navigation routes
-keep enum in.hridayan.ashell.shell.file_browser.domain.model.ConnectionMode { *; }
-keep enum in.hridayan.ashell.shell.file_browser.domain.model.OperationType { *; }
-keep enum in.hridayan.ashell.shell.file_browser.domain.model.OperationStatus { *; }
-keep enum in.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution { *; }

# Keep all @Keep annotated classes (redundant but explicit)
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
