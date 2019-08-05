package com.github.tianma8023.smscode.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
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
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.IntDef;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


/**
 * 处理验证码的Service
 */
public class SmsCodeHandleService extends Service {

    private static final String SERVICE_NAME = "SmsCodeHandleService";

    public static final String EXTRA_KEY_SMS_MESSAGE_DATA = "key_sms_message_data";

    private static final int MSG_ENABLE_ACCESSIBILITY_SERVICE = 0xff;

    private static final int MSG_COPY_TO_CLIPBOARD = 0;
    private static final int MSG_SHOW_TOAST = 1;
    private static final int MSG_DELETE_SMS = 2;
    private static final int MSG_MARK_AS_READ = 3;
    private static final int MSG_RECORD_SMS_MSG = 4;
    private static final int MSG_AUTO_INPUT_CODE = 5;
    private static final int MSG_CLEAR_CLIPBOARD = 6;
    private static final int MSG_SHOW_CODE_NOTIFICATION = 7;
    private static final int MSG_CANCEL_NOTIFICATION = 8;
    private static final int MSG_QUIT_QUEUE = 9;
    private static final int WAIT_FOR_QUIT = 10;

    private AtomicInteger mPreQuitQueueCount;
    private static final int DEFAULT_QUIT_COUNT = 0;

    private String mFocusMode;
    private String mAutoInputMode;

    private static final int OP_DELETE = 0;
    private static final int OP_MARK_AS_READ = 1;

    @IntDef({OP_DELETE, OP_MARK_AS_READ})
    @interface SmsOp {
    }

    private volatile Handler uiHandler;
    private volatile Handler workerHandler;

    public SmsCodeHandleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread workerThread = new HandlerThread(SERVICE_NAME);
        workerThread.start();

        uiHandler = new WorkerHandler(Looper.getMainLooper());
        workerHandler = new WorkerHandler(workerThread.getLooper());

        mPreQuitQueueCount = new AtomicInteger(DEFAULT_QUIT_COUNT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Show a notification for the foreground service.
            Notification notification = new NotificationCompat.Builder(this, NotificationConst.CHANNEL_ID_FOREGROUND_SERVICE)
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_app_icon))
                    .setWhen(System.currentTimeMillis())
                    .setContentText(getString(R.string.foreground_notification_title))
                    .setAutoCancel(true)
                    .setColor(getColor(R.color.ic_launcher_background))
                    .build();
            startForeground(NotificationConst.NOTIFICATION_ID_FOREGROUND_SVC, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_KEY_SMS_MESSAGE_DATA)) {
            SmsMsg smsMsg = intent.getParcelableExtra(EXTRA_KEY_SMS_MESSAGE_DATA);
            doWork(smsMsg);
        }
        return START_NOT_STICKY;
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

        if (TextUtils.isEmpty(msgBody)) {
            return;
        }
        String smsCode = SmsCodeUtils.parseSmsCodeIfExists(this, msgBody);

        if (TextUtils.isEmpty(smsCode)) { // Not SMS code msg.
            return;
        }

        XLog.i("Sms code: {}", smsCode);
        smsMsg.setSmsCode(smsCode);
        smsMsg.setCompany(SmsCodeUtils.parseCompany(msgBody));

        // 是否需要启动 AccessibilityService
        boolean autoInputEnabled = SPUtils.autoInputCodeEnabled(this);
        if (autoInputEnabled) {
            mFocusMode = SPUtils.getFocusMode(this);
            mAutoInputMode = SPUtils.getAutoInputMode(this);

            if (PrefConst.AUTO_INPUT_MODE_ROOT.equals(mAutoInputMode)
                    && PrefConst.FOCUS_MODE_AUTO.equals(mFocusMode)) {
                // Root mode + Auto Focus Mode
                workerHandler.sendEmptyMessage(MSG_ENABLE_ACCESSIBILITY_SERVICE);
            }
        }

        // 是否需要复制到剪切板
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                SPUtils.copyToClipboardEnabled(this)) {
            Message copyMsg = uiHandler.obtainMessage(MSG_COPY_TO_CLIPBOARD, smsCode);
            uiHandler.sendMessage(copyMsg);
        }

        // 是否显示Toast
        if (SPUtils.showToast(this)) {
            Message toastMsg = uiHandler.obtainMessage(MSG_SHOW_TOAST, smsCode);
            uiHandler.sendMessage(toastMsg);
        }

        // 是否自动输入
        if (autoInputEnabled) {
            Message autoInputMsg = workerHandler.obtainMessage(MSG_AUTO_INPUT_CODE, smsMsg);
            workerHandler.sendMessage(autoInputMsg);
        }

        // 是否显示通知
        if (SPUtils.showCodeNotification(this)) {
            Message notificationMsg = workerHandler.obtainMessage(MSG_SHOW_CODE_NOTIFICATION, smsMsg);
            workerHandler.sendMessage(notificationMsg);
        }

        // 是否记录验证码短信
        if (SPUtils.recordSmsCodeEnabled(this)) {
            Message recordMsg = workerHandler.obtainMessage(MSG_RECORD_SMS_MSG, smsMsg);
            workerHandler.sendMessage(recordMsg);
        }

        // 是否删除验证码短信NotificationController
        if (SPUtils.deleteSmsEnabled(this)) {
            Message deleteMsg = workerHandler.obtainMessage(MSG_DELETE_SMS, smsMsg);
            workerHandler.sendMessageDelayed(deleteMsg, 100);
            mPreQuitQueueCount.getAndIncrement();
        } else {
            // 是否标记验证码短信为已读
            if (SPUtils.markAsReadEnabled(this)) {
                // mark sms as read
                Message markMsg = workerHandler.obtainMessage(MSG_MARK_AS_READ, smsMsg);
                workerHandler.sendMessageDelayed(markMsg, 100);
                mPreQuitQueueCount.getAndIncrement();
            }
        }

        // 是否拦截验证码短信通知
        if (SPUtils.blockNotificationEnabled(this)) {
            // block sms notification
            uiHandler.postDelayed(() -> {
                Intent intent = new Intent(NotificationMonitorService.ACTION_BLOCK_SMS_NOTIFICATION);
                intent.putExtra(NotificationMonitorService.EXTRA_KEY_SMS_MSG, smsMsg);
                sendBroadcast(intent);
            }, 500);
            Intent intent = new Intent(NotificationMonitorService.ACTION_BLOCK_SMS_NOTIFICATION);
            intent.putExtra(NotificationMonitorService.EXTRA_KEY_SMS_MSG, smsMsg);
            sendBroadcast(intent);
        }

        mPreQuitQueueCount.getAndIncrement();
        workerHandler.sendEmptyMessageDelayed(WAIT_FOR_QUIT, 200);
    }

    private class WorkerHandler extends Handler {
        WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_ACCESSIBILITY_SERVICE: {
                    enableAccessibilityService();
                    break;
                }
                case MSG_COPY_TO_CLIPBOARD: {
                    copyToClipboard((String) msg.obj);
                    break;
                }
                case MSG_SHOW_TOAST: {
                    showToast((String) msg.obj);
                    break;
                }
                case MSG_DELETE_SMS: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    deleteSms(smsMsg.getSender(), smsMsg.getBody());
                    handlePreQuitQueue();
                    break;
                }
                case MSG_MARK_AS_READ: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    markSmsAsRead(smsMsg.getSender(), smsMsg.getBody());
                    handlePreQuitQueue();
                    break;
                }
                case MSG_RECORD_SMS_MSG: {
                    recordSmsMsg((SmsMsg) msg.obj);
                    break;
                }
                case MSG_AUTO_INPUT_CODE: {
                    SmsMsg smsMsg = (SmsMsg) msg.obj;
                    handleAutoInputCode(smsMsg.getSmsCode());
                    break;
                }
                case MSG_CLEAR_CLIPBOARD: {
                    clearClipboard();
                    break;
                }
                case MSG_SHOW_CODE_NOTIFICATION: {
                    showCodeNotification((SmsMsg) msg.obj);
                    break;
                }
                case MSG_CANCEL_NOTIFICATION: {
                    cancelNotification((Integer) msg.obj);
                    handlePreQuitQueue();
                    break;
                }
                case WAIT_FOR_QUIT: {
                    handlePreQuitQueue();
                    break;
                }
                case MSG_QUIT_QUEUE: {
                    quit();
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unsupported msg type");
            }
        }
    }

    private void enableAccessibilityService() {
        String accessSvcName = AccessibilityUtils.getServiceName(SmsCodeAutoInputService.class);
        // 用root的方式启动
        boolean enabled = ShellUtils.enableAccessibilityService(accessSvcName);
        XLog.d("Accessibility enabled by Root: {}", enabled);
        if (enabled) { // waiting for AutoInputService working on.
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyToClipboard(String smsCode) {
        ClipboardUtils.copyToClipboard(this, smsCode);
    }

    private void handleAutoInputCode(String smsCode) {
        if (PrefConst.AUTO_INPUT_MODE_ROOT.equals(mAutoInputMode)
                && PrefConst.FOCUS_MODE_MANUAL.equals(mFocusMode)) {
            // focus mode: manual focus
            // input mode: root mode
            boolean success = ShellUtils.inputText(smsCode);
            if (success) {
                XLog.i("Auto input succeed");
                if (SPUtils.copyToClipboardEnabled(this) &&
                        SPUtils.shouldClearClipboard(this)) {
                    uiHandler.sendEmptyMessage(MSG_CLEAR_CLIPBOARD);
                }
            }
        } else {
            // start auto input
            Intent intent = new Intent(SmsCodeAutoInputService.ACTION_START_AUTO_INPUT);
            intent.putExtra(SmsCodeAutoInputService.EXTRA_KEY_SMS_CODE, smsCode);
            sendBroadcast(intent);
        }
    }

    private void clearClipboard() {
        ClipboardUtils.clearClipboard(this);
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

    private void showCodeNotification(SmsMsg smsMsg) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String company = smsMsg.getCompany();
        String smsCode = smsMsg.getSmsCode();
        String title = TextUtils.isEmpty(company) ? smsMsg.getSender() : company;
        String content = getString(R.string.code_notification_content, smsCode);

        int notificationId = smsMsg.hashCode();
        Intent copyCodeIntent = CodeCopyService.buildCopyCodeIntent(this, smsCode);
        PendingIntent pi = PendingIntent.getService(this,
                0,
                copyCodeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, NotificationConst.CHANNEL_ID_SMSCODE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_app_icon))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.ic_launcher_background))
                .build();

        manager.notify(notificationId, notification);

        // 是否自动清除验证码通知
        if (SPUtils.autoCancelCodeNotification(this)) {
            Message cancelNotifyMsg = workerHandler
                    .obtainMessage(MSG_CANCEL_NOTIFICATION, notificationId);
            int retentionTime = SPUtils.getNotificationRetentionTime(this) * 1000;
            workerHandler.sendMessageDelayed(cancelNotifyMsg, retentionTime);
            mPreQuitQueueCount.getAndIncrement();
        }
    }

    private void cancelNotification(int notificationId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.cancel(notificationId);
    }

    private void handlePreQuitQueue() {
        mPreQuitQueueCount.decrementAndGet();
        if (mPreQuitQueueCount.get() <= DEFAULT_QUIT_COUNT) {
            // 结束Looper
            workerHandler.sendEmptyMessage(MSG_QUIT_QUEUE);
        }
    }

    private void quit() {
        if (workerHandler != null) {
            workerHandler.getLooper().quitSafely();
            XLog.d("worker thread quit");
        }
        stopSelf();
    }
}
