package com.github.tianma8023.smscode.utils;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.service.NotificationMonitorService;

/**
 * Utility of android.provider.Settings
 */
public class SettingsUtils {

    private SettingsUtils() {
    }

    private static String getSecureString(Context context, String key) {
        return Settings.Secure.getString(context.getContentResolver(), key);
    }

    /**
     * Get system default SMS app package
     * @return
     */
    public static String getDefaultSmsAppPackage(Context context) {
        String key  = "sms_default_application";
        return Settings.Secure.getString(context.getContentResolver(), key);
    }

    /**
     * Check notification listener enabled or not.
     * @param context
     * @return
     */
    public static boolean checkNotificationListenerEnabled(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            return checkNotificationListenerAbove27(context);
        } else {
            return checkNotificationListenerBelow27(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private static boolean checkNotificationListenerAbove27(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean enabled = false;
        if (manager != null) {
            ComponentName cn = new ComponentName(context, NotificationMonitorService.class);
            enabled = manager.isNotificationListenerAccessGranted(cn);
        }
        return enabled;
    }

    private static boolean checkNotificationListenerBelow27(Context context) {
        String notifyStr = getSecureString(context, "enabled_notification_listeners");
        boolean enabled = false;
        ComponentName monitorService = new ComponentName(context, NotificationMonitorService.class);
        if (!TextUtils.isEmpty(notifyStr)) {
            String[] enabledServices = notifyStr.split(":");
            for(String enabledService : enabledServices) {
                ComponentName curCN = ComponentName.unflattenFromString(enabledService);
                if (monitorService.equals(curCN)) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

    public static void gotoNotificationListenerSettings(Context context) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        context.startActivity(intent);
    }

    /**
     * Request ignore battery optimization
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("BatteryLife")
    public static void requestIgnoreBatteryOptimization(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        context.startActivity(intent);
    }

    /**
     * Go to ignore battery optimization settings.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void gotoIgnoreBatteryOptimizationSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
    }

}
