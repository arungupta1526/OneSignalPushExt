package com.prem.onesignalpush;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.onesignal.OneSignal;
import com.onesignal.OSDeviceState;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenedResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@DesignerComponent(
        version = 1,
        versionName = "1.0",
        description = "OneSignal Push Notification Extension for MIT App Inventor/Kodular.<br>" +
                "Made by: Arun Gupta<br>" +
                "<span><a href=\"https://github.com/arungupta1526/OneSignalPushExt\" target=\"_blank\"><small><mark>Github</mark></small></a></span> | " +
                "<span><a href=\"https://community.appinventor.mit.edu/t/154323\" target=\"_blank\"><small><mark>Mit AI2 Community</mark></small></a></span> | " +
                "<span><a href=\"https://community.kodular.io/t/300697\" target=\"_blank\"><small><mark>Kodular Community</mark></small></a></span>",
        nonVisible = true,
        iconName = "icon.png",
        helpUrl = "https://www.telegram.me/Arungupta1526"
)

//<span style="background-color: yellow; font-size: smaller;">Github</span>
// @SimpleObject(external = true)
public class OneSignalPush extends AndroidNonvisibleComponent {

    private final Context context;
    private final Activity activity;
    private String appId = "";
    private boolean initialized = false;

    public OneSignalPush(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.activity = container.$context();
    }

    // ===================== PROPERTIES =====================

    @SimpleProperty(description = "Get the current OneSignal App ID")
    public String AppId() {
        return appId;
    }

    // ===================== INITIALIZATION =====================

    @SimpleFunction(description = "Initialize OneSignal with the provided App ID")
    public void Initialize(String appId) {
        this.appId = appId;

        try {
            // Set log level
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

            // Initialize OneSignal
            OneSignal.initWithContext(context.getApplicationContext());
            OneSignal.setAppId(appId);

            // Set up notification handlers
            OneSignal.setNotificationOpenedHandler(result -> {
                String body = result.getNotification().getBody();
                String title = result.getNotification().getTitle();
                String data = result.getNotification().getAdditionalData() != null
                        ? result.getNotification().getAdditionalData().toString()
                        : "{}";
                NotificationOpened(body, title, data);
            });

            OneSignal.setNotificationWillShowInForegroundHandler(notificationReceivedEvent -> {
                OSNotification notification = notificationReceivedEvent.getNotification();
                String body = notification.getBody();
                String title = notification.getTitle();
                String data = notification.getAdditionalData() != null ? notification.getAdditionalData().toString()
                        : "{}";
                NotificationReceived(body, title, data);
                notificationReceivedEvent.complete(notification);
            });

            initialized = true;
            InitializationSuccess();

        } catch (Exception e) {
            InitializationError(e.getMessage());
        }
    }

    // ===================== USER MANAGEMENT =====================

    @SimpleFunction(description = "Set external user ID for the current user")
    public void SetExternalUserId(String externalId) {
        if (!checkInitialized())
            return;
        OneSignal.setExternalUserId(externalId);
    }

    @SimpleFunction(description = "Remove external user ID")
    public void RemoveExternalUserId() {
        if (!checkInitialized())
            return;
        OneSignal.removeExternalUserId();
    }

    @SimpleFunction(description = "Get the OneSignal User ID (Player ID)")
    public String GetUserId() {
        if (!checkInitialized())
            return "";
        OSDeviceState state = OneSignal.getDeviceState();
        return state != null ? state.getUserId() : "";
    }

    @SimpleFunction(description = "Get the device push token")
    public String GetPushToken() {
        if (!checkInitialized())
            return "";
        OSDeviceState state = OneSignal.getDeviceState();
        return state != null ? state.getPushToken() : "";
    }

    // ===================== TAGS MANAGEMENT =====================

    @SimpleFunction(description = "Send a tag to OneSignal for user segmentation")
    public void SendTag(String key, String value) {
        if (!checkInitialized())
            return;
        OneSignal.sendTag(key, value);
    }

    @SimpleFunction(description = "Delete a specific tag")
    public void DeleteTag(String key) {
        if (!checkInitialized())
            return;
        OneSignal.deleteTag(key);
    }

    // ===================== SUBSCRIPTION MANAGEMENT =====================

    @SimpleFunction(description = "Check if push notifications are subscribed")
    public boolean IsSubscribed() {
        if (!checkInitialized())
            return false;
        OSDeviceState state = OneSignal.getDeviceState();
        return state != null && state.isSubscribed();
    }

    @SimpleFunction(description = "Enable or disable push notifications")
    public void SetSubscription(boolean enable) {
        if (!checkInitialized())
            return;
        OneSignal.disablePush(!enable);
        // Dispatch subscription changed event
        SubscriptionChanged(enable);
    }

    @SimpleFunction(description = "Prompt user for push notification permission")
    public void PromptForPush() {
        if (!checkInitialized())
            return;
        OneSignal.promptForPushNotifications();
    }

    // ===================== NOTIFICATION PERMISSIONS =====================

    @SimpleFunction(description = "Ask for notification permission (Android 13+)")
    public void AskForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @SimpleFunction(description = "Check if notification permission is granted")
    public boolean IsPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Always true for Android < 13
    }

    // ===================== NOTIFICATION SENDING =====================

    // @SimpleFunction(description = "Send notification to all subscribed users (API
    // simulation)")
    // public void SendToAllSubscribers(String apiKey, String title, String body,
    // String largeIcon, String data) {
    // try {
    // // This is just a simulation - actual sending would require HTTP client
    // // For now, we'll just trigger the success event
    // NotificationSent();
    // } catch (Exception e) {
    // NotificationSendFailed(e.getMessage());
    // }
    // }

    // ===================== EVENTS =====================

    @SimpleEvent(description = "Triggered when OneSignal is initialized successfully")
    public void InitializationSuccess() {
        EventDispatcher.dispatchEvent(this, "InitializationSuccess");
    }

    @SimpleEvent(description = "Triggered when OneSignal initialization fails")
    public void InitializationError(String error) {
        EventDispatcher.dispatchEvent(this, "InitializationError", error);
    }

    @SimpleEvent(description = "Triggered when a notification is received")
    public void NotificationReceived(String body, String title, String data) {
        EventDispatcher.dispatchEvent(this, "NotificationReceived", body, title, data);
    }

    @SimpleEvent(description = "Triggered when a notification is opened")
    public void NotificationOpened(String body, String title, String data) {
        EventDispatcher.dispatchEvent(this, "NotificationOpened", body, title, data);
    }

    // @SimpleEvent(description = "Triggered when notification is sent
    // successfully")
    // public void NotificationSent() {
    // EventDispatcher.dispatchEvent(this, "NotificationSent");
    // }

    // @SimpleEvent(description = "Triggered when notification sending fails")
    // public void NotificationSendFailed(String error) {
    // EventDispatcher.dispatchEvent(this, "NotificationSendFailed", error);
    // }

    @SimpleEvent(description = "Triggered when subscription status changes")
    public void SubscriptionChanged(boolean isSubscribed) {
        EventDispatcher.dispatchEvent(this, "SubscriptionChanged", isSubscribed);
    }

    // ===================== UTILITY FUNCTIONS =====================

    @SimpleFunction(description = "Returns the Android API version of the device.")
    public int GetApiVersion() {
        return Build.VERSION.SDK_INT;
    }

    @SimpleFunction(description = "Returns the Android version release (e.g., 13, 14).")
    public String GetAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    @SimpleFunction(description = "Check if OneSignal is initialized")
    public boolean IsInitialized() {
        return initialized;
    }

    // ===================== PRIVATE METHODS =====================

    private boolean checkInitialized() {
        if (!initialized) {
            // Use a simpler error message without event dispatch to avoid recursion
            return false;
        }
        return true;
    }
}