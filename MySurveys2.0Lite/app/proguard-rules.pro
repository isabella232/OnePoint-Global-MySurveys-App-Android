# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Padmavathi\AppData\Local\Android\android-sdk/tools/proguard/proguard-android.txt
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
-dontoptimize

-dontwarn com.opg.sdk.**
-keep class com.opg.sdk.** { *; }

-dontwarn com.opg.survey.**
-keep class com.opg.survey.** { *; }

-keep class android.support.v7.widget.SearchView { *; }

#-dontwarn org.apache.cordova.**
#-keep class org.apache.cordova.**{*;}

#-dontwarn android.**
#-keep class android.**{*;}

#-dontwarn java.**
#-keep class java.**{*;}

#-dontwarn CS2JNet.System.**
#-dontwarn OnePoint.Player.Html.**

#-dontwarn org.apache.log4j.**
#-keep class org.apache.log4j.* { *; }
##################
-dontwarn OnePoint.CordovaPlugin.**
-keep class OnePoint.CordovaPlugin.** { *; }

-dontwarn org.apache.log4j.chainsaw.LoadXMLAction
-keep class org.apache.log4j.** { *; }

-dontwarn com.phonegap.**
-keep class com.phonegap.** { *; }

-dontwarn com.gargoylesoftware.**
-keep class com.gargoylesoftware.**{*;}

-dontwarn org.apache.**
-keep class org.apache.** { *; }

-dontwarn junit.framework.**
-keep class junit.framework.** { *; }

-dontwarn org.junit.**
-keep class org.junit.** { *; }

-dontwarn de.mindpipe.android.logging.**
-keep class de.mindpipe.android.logging.** { *; }

-dontwarn android.test.**
-keep class android.test.** { *; }

-dontwarn com.google.zxing.**
-keep class com.google.zxing** { *; }

-dontwarn com.google.**
-keep class com.google.** { *; }

-dontwarn org.json.**
-keep class org.json.** {*;}

-dontwarn OnePoint.Runtime.**
-keep class OnePoint.Runtime.** {*;}

-dontwarn netscape.javascript.**
-keep class netscape.javascript.** { *; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class java.io.PrintStream {
     public void println(%);
     public void println(**);
     public void print(%);
     public void print(**);
 }

########################

#-dontwarn javax.xml.**


#-keep junit.framework.**
#-dontwarn OnePoint.Logging.LogManager
#-dontwarn com.opg.main.**
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {public *;}
#-keepattributes EnclosingMethod
#-keepattributes InnerClasses

#-dontwarn org.apache.log4j.**
#-keep class org.apache.log4j.** { *; }

#-dontwarn com.androidquery.auth.**
#-keep class com.androidquery.auth.** { *; }

#-dontwarn oauth.**
#-keep class oauth.** { *; }

#-dontwarn com.android.auth.TwitterHandle.**
#-keep class com.android.auth.TwitterHandle.** { *; }



#-keep public class OnePoint.Player.Html.UI.HtmlPage
#-dontwarn OnePoint.Player.Html.UI.HtmlPage

#-keep class apache.** { *; }

#-keep public class android.net.http.SslError
#-keep public class android.webkit.WebViewClient
#-keep public class android.webkit.WebView

#-dontwarn android.webkit.WebView
#-dontwarn android.net.http.SslError
#-dontwarn android.webkit.WebViewClient




#-keep class org.apache.http.auth.InvalidCredentialsException
#-keep class org.apache.http.** { *; }

#-dontwarn javax.annotation.**

#-dontwarn javax.**
#-keep class javax.**{*;}

#-dontwarn org.chromium.base.multidex.**