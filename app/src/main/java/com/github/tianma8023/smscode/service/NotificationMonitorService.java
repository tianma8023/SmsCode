package com.github.tianma8023.smscode.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.github.tianma8023.smscode.entity.SmsMsg;
import com.github.tianma8023.smscode.utils.SettingsUtils;
import com.github.tianma8023.smscode.utils.XLog;

/**
 * Notification Listener Service
 */
public class NotificationMonitorService extends NotificationListenerService {

    public static final String ACTION_CANCEL_NOTIFICATION = "action_cancel_notification";

    public static final String EXTRA_KEY_SMS_MSG = "extra_key_sms_msg";

    private class NotificationControllerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            XLog.d("NotificationController: {}", action);
            if (ACTION_CANCEL_NOTIFICATION.equals(action)) {
                mSmsMsg = intent.getParcelableExtra(EXTRA_KEY_SMS_MSG);
                performCancelNotification();
            }
        }
    }

    private NotificationControllerReceiver mControllerReceiver;

    private SmsMsg mSmsMsg;

    @Override
    public void onListenerConnected() {
        init();
    }

    private void init() {
        if (mControllerReceiver == null) {
            mControllerReceiver = new NotificationControllerReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_CANCEL_NOTIFICATION);
            registerReceiver(mControllerReceiver, intentFilter);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        performCancelNotification();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    @Override
    public void onListenerDisconnected() {
        if (mControllerReceiver != null) {
            unregisterReceiver(mControllerReceiver);
        }
    }

    /**
     * Try to cancel code message notification
     */
    private void performCancelNotification() {
        if (mSmsMsg == null) {
            return;
        }

        String defaultSmsPkg = SettingsUtils.getDefaultSmsAppPackage(this);
        if (TextUtils.isEmpty(defaultSmsPkg)) {
            return;
        }

        String sender = mSmsMsg.getSender();
        String smsCode = mSmsMsg.getSmsCode();
        String smsBody = mSmsMsg.getBody();

        StatusBarNotification[] sbnArr = getActiveNotifications();
        for (StatusBarNotification sbn : sbnArr) {
            if (defaultSmsPkg.equals(sbn.getPackageName())) {
                Notification notification = sbn.getNotification();

                String title = notification.extras.getString(Notification.EXTRA_TITLE);
                String text = notification.extras.getString(Notification.EXTRA_TEXT);

                XLog.d("title = {}, text = {}", title, text);
                boolean hit = false;

                if (title != null && title.contains(sender)) {
                    hit = true;
                } else if (text != null && (text.equals(smsBody) || text.contains(smsCode))) {
                    hit = true;
                }

                if (hit) {
                    cancelNotification(sbn.getKey());
                    mSmsMsg = null;
                    XLog.d("cancel notification succeed");
                    break;
                }
            }
        }
    }
}
