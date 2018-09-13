package com.github.tianma8023.smscode.service;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.constant.NotificationConst;
import com.github.tianma8023.smscode.constant.PrefConst;
import com.github.tianma8023.smscode.entity.SmsMessageData;
import com.github.tianma8023.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.smscode.utils.ClipboardUtils;
import com.github.tianma8023.smscode.utils.SPUtils;
import com.github.tianma8023.smscode.utils.ShellUtils;
import com.github.tianma8023.smscode.utils.StringUtils;
import com.github.tianma8023.smscode.utils.VerificationUtils;
import com.github.tianma8023.smscode.utils.XLog;

import java.util.concurrent.TimeUnit;


/**
 * 处理验证码的Service
 */
public class SmsCodeHandleService extends IntentService {

    private static final String SERVICE_NAME = "SmsCodeHandleService";

    private static final int NOTIFY_ID_FOREGROUND_SVC = 0xff;

    private static final int JOB_ID = 0x100;
    public static final String EXTRA_KEY_SMS_MESSAGE_DATA = "key_sms_message_data";

    private static final int MSG_COPY_TO_CLIPBOARD = 0xff;
    private static final int MSG_MARK_AS_READ = 0xfe;

    private boolean mIsAutoInputModeRoot;
    private String mFocusMode;

    public SmsCodeHandleService() {
        this(SERVICE_NAME);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SmsCodeHandleService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Show a notification for the foreground service.
            Notification notification = new NotificationCompat.Builder(this, NotificationConst.CHANNEL_ID_FOREGROUND_SERVICE)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification))
                    .setWhen(System.currentTimeMillis())
                    .setContentText(getString(R.string.sms_code_notification_title))
                    .setAutoCancel(true)
                    .setColor(getColor(R.color.ic_launcher_background))
                    .build();
            startForeground(NOTIFY_ID_FOREGROUND_SVC, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;
        if (intent.hasExtra(EXTRA_KEY_SMS_MESSAGE_DATA)) {
            SmsMessageData smsMessageData = intent.getParcelableExtra(EXTRA_KEY_SMS_MESSAGE_DATA);
            doWork(smsMessageData);
        }
    }

    private void doWork(SmsMessageData smsMessageData) {
        if (!SPUtils.isEnable(this)) {
            XLog.i("SmsCode disabled, exiting");
            return;
        }

        String sender = smsMessageData.getSender();
        String msgBody = smsMessageData.getBody();
        long date = smsMessageData.getDate();

        String lastSender = SPUtils.getLastSmsSender(this);
        long lastDate = SPUtils.getLastSmsDate(this);

        // save last sender & date
        SPUtils.setLastSmsDate(this, date);
        SPUtils.setLastSmsSender(this, sender);

        if (Math.abs(date - lastDate) <= 5000 && lastSender.equals(sender)) {
            // duplicate SMS message
            XLog.d("Duplicate SMS, exiting");
            return;
        }

        if (BuildConfig.DEBUG) {
            XLog.i("Sender: {}", sender);
            XLog.i("Body: {}", msgBody);
        } else {
            XLog.i("Sender: {}", StringUtils.escape(sender));
            XLog.i("Body: {}", StringUtils.escape(msgBody));
        }

        if (TextUtils.isEmpty(msgBody))
            return;
        String verificationCode = VerificationUtils.parseVerificationCodeIfExists(this, msgBody);

        if (TextUtils.isEmpty(verificationCode)) { // Not verification code msg.
            return;
        }

        mFocusMode = SPUtils.getFocusMode(this);
        mIsAutoInputModeRoot = PrefConst.AUTO_INPUT_MODE_ROOT.equals(SPUtils.getAutoInputMode(this));
        XLog.d("FocusMode: {}", mFocusMode);
        XLog.d("AutoInputRootMode: {}", mIsAutoInputModeRoot);

        if (PrefConst.FOCUS_MODE_AUTO.equals(mFocusMode) && mIsAutoInputModeRoot) {
            // Root mode + Auto Focus Mode
            String accessSvcName = AccessibilityUtils.getServiceName(SmsCodeAutoInputService.class);
            // 用root的方式启动
            boolean enabled = ShellUtils.enableAccessibilityService(accessSvcName);
            XLog.d("Accessibility enabled by Root: {}", enabled);
            if (enabled) { // waiting for AutoInputService working on.
                sleep(1);
            }
        }

        XLog.i("Verification code: {}", verificationCode);
        Message copyMsg = new Message();
        copyMsg.obj = verificationCode;
        copyMsg.what = MSG_COPY_TO_CLIPBOARD;
        innerHandler.sendMessage(copyMsg);

        // mark sms as read or not.
//        if (getBoolean(mPreferences, PrefConst.KEY_MARK_AS_READ, PrefConst.MARK_AS_READ_DEFAULT)) {
////            sleep(8);
//            Message markMsg = new Message();
//            markMsg.obj = smsMessageData;
//            markMsg.what = MSG_MARK_AS_READ;
//            innerHandler.sendMessageDelayed(markMsg, 8000);
//        }
    }

    private Handler innerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COPY_TO_CLIPBOARD:
                    copyToClipboardOnMainThread((String) msg.obj);
                    break;
                case MSG_MARK_AS_READ:
                    SmsMessageData smsMessageData = (SmsMessageData) msg.obj;
                    String sender = smsMessageData.getSender();
                    String body = smsMessageData.getBody();
                    markSmsAsRead(sender, body);
                    break;
            }
        }
    };

    /**
     * 在主线程上执行copy操作
     */
    private void copyToClipboardOnMainThread(String verificationCode) {
        ClipboardUtils.copyToClipboard(this, verificationCode);
        if (SPUtils.showToast(this)) {
            String text = this.getString(R.string.cur_verification_code, verificationCode);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (mIsAutoInputModeRoot && PrefConst.FOCUS_MODE_MANUAL.equals(mFocusMode)) {
            // focus mode: manual focus
            // input mode: root mode
            boolean success = ShellUtils.inputText(verificationCode);
            if (success) {
                XLog.i("Auto input succeed");
            }
        } else {
            // start auto input
            Intent intent = new Intent(SmsCodeAutoInputService.ACTION_START_AUTO_INPUT);
            intent.putExtra(SmsCodeAutoInputService.EXTRA_KEY_SMS_CODE, verificationCode);
            sendBroadcast(intent);
        }
    }

    private void markSmsAsRead(String sender, String body) {
        Cursor cursor = null;
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                XLog.e("Don't have permission to read sms");
                return;
            }
            Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
            cursor = this.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null)
                return;
            while (cursor.moveToNext()) {
                String curAddress = cursor.getString(cursor.getColumnIndex("address"));
                int curRead = cursor.getInt(cursor.getColumnIndex("read"));
                String curBody = cursor.getString(cursor.getColumnIndex("body"));
                XLog.d("curBody = {}", curBody);
                if (curAddress.equals(sender) && curRead == 0 && curBody.startsWith(body)) {
                    String smsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    int rows = this.getContentResolver().update(uri, values, "_id = ?", new String[]{smsMessageId});
                    XLog.d("Updates rows {}", rows);
                }
            }
            XLog.i("Mark as read succeed");
        } catch (Exception e) {
            XLog.e("Mark as read failed: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
