package com.github.tianma8023.smscode.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Telephony;

import com.github.tianma8023.smscode.entity.SmsMsg;
import com.github.tianma8023.smscode.utils.SPUtils;
import com.github.tianma8023.smscode.utils.XLog;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import ch.qos.logback.classic.Level;

/**
 * 观察监测短信的Service
 */
public class SmsObserveService extends Service {

    private SmsObserver mSmsObserver;

    private int lastId = 0;

    private boolean mCurIsVerboseLog;

    private static final String EXTRA_KEY_VERBOSE_LOG = "extra_key_verbose_log";

    @Override
    public void onCreate() {
        super.onCreate();
        mCurIsVerboseLog = SPUtils.isVerboseLogMode(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_KEY_VERBOSE_LOG)) {
            boolean argVerboseLog = intent.getBooleanExtra(EXTRA_KEY_VERBOSE_LOG, false);
            if (mCurIsVerboseLog != argVerboseLog) {
                // argument is changed, need to set new level to logger
                // 因为Logger在不同进程的实例不止一个(多进程的弊端)，所以需要在这里同步
                mCurIsVerboseLog = argVerboseLog;
                if (mCurIsVerboseLog) {
                    XLog.setLogLevel(Level.TRACE);
                } else {
                    XLog.setLogLevel(Level.INFO);
                }
            }
        }
        registerObserver();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterObserver();
    }

    private void registerObserver() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            if (mSmsObserver == null) {
                mSmsObserver = new SmsObserver(new Handler(Looper.getMainLooper()));
            }
            // register SMS content observer
            getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI,
                    true, mSmsObserver);
        } else {
            XLog.d("RECEIVE_SMS permission denied");
        }
    }

    private void unregisterObserver() {
        if (mSmsObserver == null) {
            return;
        }

        // unregister content observer
        getContentResolver().unregisterContentObserver(mSmsObserver);
    }

    private class SmsObserver extends ContentObserver {

        private SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            XLog.d("SmsObserver#onChange()");
            parseNewSms();
        }
    }

    private void parseNewSms() {
        final String[] projection = new String[]{
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
        };
        // 考虑到不止新增短信能触发Sms ContentObserver的onChange()，比如删除短信也能触发
        // 所以需要时间判断, 10s之内的是新短信
        final String selection = Telephony.Sms.DATE + " > ?";
        final String[] selectionArgs = new String[]{
                "" + (System.currentTimeMillis() - 10000),
        };
        final String sortOrder = Telephony.Sms.DATE + " desc limit 1";
        Cursor cursor = getContentResolver().query(Telephony.Sms.Inbox.CONTENT_URI, projection,
                selection, selectionArgs, sortOrder);
        if (cursor == null) {
            return;
        }
        if(cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(Telephony.Sms._ID));
            if (id != lastId) { // same with the last id
                lastId = id;

                String sender = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));

                SmsMsg smsMsg = new SmsMsg();
                smsMsg.setSender(sender);
                smsMsg.setBody(body);
                smsMsg.setDate(date);

                Intent smsCodeHandleSvc = new Intent(this, SmsCodeHandleService.class);
                smsCodeHandleSvc.putExtra(SmsCodeHandleService.EXTRA_KEY_SMS_MESSAGE_DATA, smsMsg);
                ContextCompat.startForegroundService(this, smsCodeHandleSvc);
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    public static void startMe(Context context, boolean isVerboseLog) {
        Intent service = new Intent(context, SmsObserveService.class);
        service.putExtra(EXTRA_KEY_VERBOSE_LOG, isVerboseLog);
        context.startService(service);
    }

    public static void stopMe(Context context) {
        Intent service = new Intent(context, SmsObserveService.class);
        context.stopService(service);
    }

}
