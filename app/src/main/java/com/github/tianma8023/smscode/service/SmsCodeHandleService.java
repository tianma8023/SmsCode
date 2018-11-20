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
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.constant.NotificationConst;
import com.github.tianma8023.smscode.constant.PrefConst;
import com.github.tianma8023.smscode.db.DBManager;
import com.github.tianma8023.smscode.entity.SmsMsg;
import com.github.tianma8023.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.smscode.utils.ClipboardUtils;
import com.github.tianma8023.smscode.utils.SPUtils;
import com.github.tianma8023.smscode.utils.ShellUtils;
import com.github.tianma8023.smscode.utils.SmsCodeUtils;
import com.github.tianma8023.smscode.utils.StringUtils;
import com.github.tianma8023.smscode.utils.XLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 处理验证码的Service
 */
public class SmsCodeHandleService extends IntentService {

    private static final String SERVICE_NAME = "SmsCodeHandleService";

    private static final int NOTIFY_ID_FOREGROUND_SVC = 0xff;

    private static final int JOB_ID = 0x100;
    public static final String EXTRA_KEY_SMS_MESSAGE_DATA = "key_sms_message_data";

    private static final int MSG_SMSCODE_EXTRACTED = 0xff;
    private static final int MSG_MARK_AS_READ = 0xfe;
    private static final int MSG_DELETE_SMS = 0xfd;

    private boolean mAutoInputEnabled;
    private boolean mIsAutoInputModeRoot;
    private String mFocusMode;

    private static final int OP_DELETE = 0;
    private static final int OP_MARK_AS_READ = 1;

    @IntDef({OP_DELETE, OP_MARK_AS_READ})
    @interface SmsOp {
    }

    public SmsCodeHandleService() {
        this(SERVICE_NAME);
    }

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
            SmsMsg smsMsg = intent.getParcelableExtra(EXTRA_KEY_SMS_MESSAGE_DATA);
            doWork(smsMsg);
        }
    }

    private void doWork(SmsMsg smsMsg) {
        if (!SPUtils.isEnable(this)) {
            XLog.i("SmsCode disabled, exiting");
            return;
        }

        String sender = smsMsg.getSender();
        String msgBody = smsMsg.getBody();
        long date = smsMsg.getDate();

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
        final String smsCode =
                SmsCodeUtils.parseSmsCodeIfExists(this, msgBody);

        if (TextUtils.isEmpty(smsCode)) { // Not verification code msg.
            return;
        }

        smsMsg.setSmsCode(smsCode);

        mAutoInputEnabled = SPUtils.autoInputCodeEnabled(this);
        XLog.d("AutoInputEnabled: {}", mAutoInputEnabled);
        if (mAutoInputEnabled) {
            mFocusMode = SPUtils.getFocusMode(this);
            mIsAutoInputModeRoot = PrefConst.AUTO_INPUT_MODE_ROOT.equals(SPUtils.getAutoInputMode(this));

            XLog.d("FocusMode: {}", mFocusMode);
            XLog.d("AutoInputRootMode: {}", mIsAutoInputModeRoot);
            if (mIsAutoInputModeRoot && PrefConst.FOCUS_MODE_AUTO.equals(mFocusMode)) {
                // Root mode + Auto Focus Mode
                String accessSvcName = AccessibilityUtils.getServiceName(SmsCodeAutoInputService.class);
                // 用root的方式启动
                boolean enabled = ShellUtils.enableAccessibilityService(accessSvcName);
                XLog.d("Accessibility enabled by Root: {}", enabled);
                if (enabled) { // waiting for AutoInputService working on.
                    sleep(1);
                }
            }
        }

        XLog.i("Verification code: {}", smsCode);

        Message copyMsg = new Message();
        copyMsg.obj = smsCode;
        copyMsg.what = MSG_SMSCODE_EXTRACTED;
        mMainHandler.sendMessage(copyMsg);

        if (SPUtils.deleteSmsEnabled(this)) {
            // delete sms
            Message deleteMsg = new Message();
            deleteMsg.obj = smsMsg;
            deleteMsg.what = MSG_DELETE_SMS;
            mMainHandler.sendMessageDelayed(deleteMsg, 100);
        } else {
            if (SPUtils.markAsReadEnabled(this)) {
                // mark sms as read
                Message markMsg = new Message();
                markMsg.obj = smsMsg;
                markMsg.what = MSG_MARK_AS_READ;
                mMainHandler.sendMessageDelayed(markMsg, 100);
            }
        }

        if (SPUtils.recordSmsCodeEnabled(this)) {
            smsMsg.setCompany(SmsCodeUtils.parseCompany(msgBody));
            recordSmsMsg(smsMsg);
        }

        if (SPUtils.blockNotificationEnabled(this)) {
            // cancel notification
            Intent intent = new Intent(NotificationMonitorService.ACTION_CANCEL_NOTIFICATION);
            intent.putExtra(NotificationMonitorService.EXTRA_KEY_SMS_MSG, smsMsg);
            sendBroadcast(intent);
//            final String defaultSmsPkg = SettingsUtils.getDefaultSmsAppPackage(this);
//            mMainHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    boolean success = ShellUtils.cancelAllNotifications(defaultSmsPkg);
//                    XLog.d("cancel notification by root: {}", success);
//                }
//            }, 1000);
        }
    }

    private void recordSmsMsg(SmsMsg smsMsg) {
        try {
            DBManager dm = DBManager.get(this);
            dm.addSmsMsg(smsMsg);
            XLog.d("add SMS message record succeed");

            List<SmsMsg> smsMsgList = dm.queryAllSmsMsg();
            if (smsMsgList.size() > PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT) {
                List<SmsMsg> outdatedMsgList = new ArrayList<>();
                for (int i = PrefConst.MAX_SMS_RECORDS_COUNT_DEFAULT; i < smsMsgList.size(); i++) {
                    outdatedMsgList.add(smsMsgList.get(i));
                }
                dm.removeSmsMsgList(outdatedMsgList);
                XLog.d("Remove outdated SMS message records succeed");
            }
        } catch (Exception e) {
            XLog.e("add SMS message record failed", e);
        }
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMSCODE_EXTRACTED:
                    onSmsCodeExtracted((String) msg.obj);
                    break;
                case MSG_MARK_AS_READ: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    String sender = smsMsg.getSender();
                    String body = smsMsg.getBody();
                    markSmsAsRead(sender, body);
                    break;
                }
                case MSG_DELETE_SMS: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    String sender = smsMsg.getSender();
                    String body = smsMsg.getBody();
                    deleteSms(sender, body);
                    break;
                }
            }
        }
    };


    private void onSmsCodeExtracted(final String smsCode) {
        boolean copyToClipboardEnabled = SPUtils.copyToClipboardEnabled(this);
        if (copyToClipboardEnabled) {
            ClipboardUtils.copyToClipboard(this, smsCode);
        }

        if (SPUtils.showToast(this)) {
            showToast(smsCode);
        }

        if (mAutoInputEnabled) {
            if (mIsAutoInputModeRoot && PrefConst.FOCUS_MODE_MANUAL.equals(mFocusMode)) {
                // focus mode: manual focus
                // input mode: root mode
                boolean success = ShellUtils.inputText(smsCode);
                if (success) {
                    XLog.i("Auto input succeed");
                    if (copyToClipboardEnabled &&
                            SPUtils.shouldClearClipboard(this)) {
                        ClipboardUtils.clearClipboard(this);
                    }
                }
            } else {
                // start auto input
                Intent intent = new Intent(SmsCodeAutoInputService.ACTION_START_AUTO_INPUT);
                intent.putExtra(SmsCodeAutoInputService.EXTRA_KEY_SMS_CODE, smsCode);
                sendBroadcast(intent);
            }
        }
    }

    private void showToast(String smsCode) {
        String text = this.getString(R.string.cur_verification_code, smsCode);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void markSmsAsRead(String sender, String body) {
        operateSms(sender, body, OP_MARK_AS_READ);
    }

    private void deleteSms(String sender, String body) {
        operateSms(sender, body, OP_DELETE);
    }

    /**
     * Handle sms according to its operation
     */
    private void operateSms(String sender, String body, @SmsOp int smsOp) {
        Cursor cursor = null;
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                XLog.e("Don't have permission to read/write sms");
                return;
            }
            String[] projection = new String[]{
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.READ,
                    Telephony.Sms.DATE
            };
            // 查看最近5条短信
            String sortOrder = Telephony.Sms.DATE + " desc limit 5";
            Uri uri = Telephony.Sms.CONTENT_URI;
            cursor = this.getContentResolver().query(uri, projection, null, null, sortOrder);
            if (cursor == null)
                return;
            while (cursor.moveToNext()) {
                String curAddress = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                int curRead = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ));
                String curBody = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                if (curAddress.equals(sender) && curRead == 0 && curBody.startsWith(body)) {
                    String smsMessageId = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
                    String where = Telephony.Sms._ID + " = ?";
                    String[] selectionArgs = new String[]{smsMessageId};
                    if (smsOp == OP_DELETE) {
                        int rows = getContentResolver().delete(uri, where, selectionArgs);
                        if (rows > 0) {
                            XLog.i("Delete sms succeed");
                            break;
                        }
                    } else if (smsOp == OP_MARK_AS_READ) {
                        ContentValues values = new ContentValues();
                        values.put(Telephony.Sms.READ, true);
                        int rows = this.getContentResolver().update(uri, values, where, selectionArgs);
                        if (rows > 0) {
                            XLog.i("Mark as read succeed");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (smsOp == OP_MARK_AS_READ) {
                XLog.e("Mark as read failed: ", e);
            } else if (smsOp == OP_DELETE) {
                XLog.e("Delete sms failed: ", e);
            }
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
