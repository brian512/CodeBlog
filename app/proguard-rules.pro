# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Java\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#不压缩输入的类文件
-dontshrink

#指定代码的压缩级别
-optimizationpasses 5

#包名不混合大小写
-dontusemixedcaseclassnames

#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses

#优化  不优化输入的类文件
-dontoptimize

#混淆时是否记录日志
-verbose
-dontskipnonpubliclibraryclassmembers

#预校验
-dontpreverify

#忽略警告
-ignorewarnings
-dontnote

#如果有警告也不终止
-dontwarn

# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep public abstract interface android.content.pm.*

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class android.support.** {*;}
-keep class org.apache.http.** {*;}
-keep class org.apache.http.impl.nio.** {*;}
-keep class org.apache.http.nio.** {*;}
-keep class org.apache.http.entity.mime.** {*;}

-keep class org.apache.commons.codec.**{*;}
-keep class org.apache.commons.codec.binary.**{*;}
-keep class org.apache.commons.codec.language.**{*;}
-keep class org.apache.commons.codec.net.**{*;}

# 不混淆注解(这里主要是不混淆EventBus的注解)
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keepclassmembers class ** {
      @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode {
        *;
}
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
      <init>(java.lang.Throwable);
}

#EventBus相关
-keepclassmembers class * {
    public void onEvent*(***);
}

-keep public class com.tencent.** {*;}
-keep public class android.net.** {*;}
-keep public class org.jsoup.** {*;}
-keep public class com.qhad.ads.sdk.** {*;}

#删除日志（乱七八糟的都删除）
-assumenosideeffects class org.apache.log4j.** {*;}
-assumenosideeffects class de.mindpipe.android.logging.log4j.LogConfigurator {*;}
-assumenosideeffects class android.util.Log { public * ; }
-assumenosideeffects class com.brian.csdnblog.util.LogUtil {
    public static void d(...);
    public static void log(...);
}

#友盟
-keepclassmembers class * {
    public <init> (org.json.JSONObject);
}
-keep public class com.brian.csdnblog.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class org.apache.http.impl.client.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-keepattributes Exceptions, Signature, InnerClasses
-keepattributes SourceFile, LineNumberTable

#小米推送
-keep class com.brian.csdnblog.receiver.MiPushMessageReceiver {*;}

# 腾讯相关
-keep class com.tencent.** {*;}

-keep class android.support.**{*;}

# 解决第三方包，爆出的warning
-keepattributes EnclosingMethod