package com.github.tianma8023.smscode.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.github.tianma8023.smscode.entity.SmsMsg;
import com.github.tianma8023.smscode.utils.XLog;

/**
 * Notification Listener Service
 */
public class NotificationMonitorService extends NotificationListenerService {

    public static final String ACTION_BLOCK_SMS_NOTIFICATION = "action_block_sms_notification";

    public static final String EXTRA_KEY_SMS_MSG = "extra_key_sms_msg";

    private class NotificationControllerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            XLog.d("NotificationController receive: {}", action);
            if (ACTION_BLOCK_SMS_NOTIFICATION.equals(action)) {
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
            intentFilter.addAction(ACTION_BLOCK_SMS_NOTIFICATION);
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
    private synchronized void performCancelNotification() {
        if (mSmsMsg == null) {
            return;
        }

        String defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(this);
        if (TextUtils.isEmpty(defaultSmsPkg)) {
            return;
        }

        String sender = mSmsMsg.getSender();
        String smsCode = mSmsMsg.getSmsCode();
        String smsBody = mSmsMsg.getBody();

        StatusBarNotification[] sbnArr;
        try {
            sbnArr = getActiveNotifications();
        } catch (Exception e) {
            // fix bug:
            // RuntimeException Failed to unparcel Bitmap.
            XLog.e("Failed to get active notifications", e);
            return;
        }
        for (StatusBarNotification sbn : sbnArr) {
            if (defaultSmsPkg.equals(sbn.getPackageName())) {
                Notification notification = sbn.getNotification();

                CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
                CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);


                XLog.d("title = {}, text = {}", title, text);
                boolean hit = false;

                if (title != null && title.toString().contains(sender)) {
                    hit = true;
                } else if (text != null && (text.equals(smsBody) || text.toString().contains(smsCode))) {
                    hit = true;
                }

                if (hit) {
                    cancelNotification(sbn.getKey());
                    XLog.i("block sms notification succeed");
                    break;
                }
            }
        }
        mSmsMsg = null;
    }
}
