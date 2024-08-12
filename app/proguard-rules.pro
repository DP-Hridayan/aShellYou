# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Glide-specific rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}

# General ProGuard rules to ensure meaningful stack traces
-keepattributes *Annotation*
-dontwarn javax.annotation.Nullable
-keep class javax.annotation.** { *; }

# Preserve class and method names for debugging
-keepnames class * {
    public *;
}

# Keep the names of your activities, fragments, and services
-keep class in.hridayan.ashell.** { *; }

# For libraries you are using (e.g., Retrofit, OkHttp)
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Add any other library rules as necessary