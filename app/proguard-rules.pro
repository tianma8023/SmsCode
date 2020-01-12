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

# obfuscate method & variables
-obfuscationdictionary proguard-dictionary.txt

# obfuscate class name
-classobfuscationdictionary proguard-dictionary.txt

# obfuscate package name
-packageobfuscationdictionary proguard-dictionary.txt

# repackage
-repackageclasses android.support.v7

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

# ==========================
# event bus proguard start

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# event bus proguard end
# ==========================

# ==========================
# greenDAO 3 proguard start
### greenDAO 3
### GreenDaoUpgradeHelper
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
    public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
    public static void createTable(org.greenrobot.greendao.database.Database, boolean);
}
-keep class **$Properties {
    *;
}

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

# greenDAO 3 proguard end
# ==========================

# ==========================
# BRVAH proguard start
-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}
-keepattributes InnerClasses
# BRVAH proguard end
# ==========================