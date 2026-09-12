# ProGuard rules for SwasthyaSathi AI
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.swasthyasathi.app.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
