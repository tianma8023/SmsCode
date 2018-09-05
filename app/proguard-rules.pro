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

# ===============================
# AndPermission start
-dontwarn com.yanzhenjie.permission.**
# AndPermission end
# ===============================


# ===============================
# logback-android start
-dontwarn javax.mail.**
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*
# logback-android end
# ===============================

# ==========================
# Umeng analyze proguard start

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.umeng.** {*;}

# Umeng analyze proguard end
# ==========================

# ==========================
# bugly proguard start

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.** {
    *;
}

# bugly proguard end
# ==========================