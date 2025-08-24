# Repackages optimized classes into %packageName%.repacked package
# in resulting AIX. Repackaging is necessary to avoid clashes with
# the other extensions that might be using same libraries as you.
-repackageclasses %packageName%.repacked

-android
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-useuniqueclassmembernames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmember


# Keep OneSignal classes
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

## Keep Gson (used by OneSignal for JSON serialization)
#-keep class com.google.gson.** { *; }
#-dontwarn com.google.gson.**

# Firebase Messaging (if using with OneSignal)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

## Ensure annotations are kept (important for App Inventor / Kodular extensions)
#-keepattributes *Annotation*
#
## Keep Extension runtime classes (App Inventor)
#-keep class com.google.appinventor.** { *; }
#-dontwarn com.google.appinventor.**

