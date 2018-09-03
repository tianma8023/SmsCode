package com.github.tianma8023.smscode.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.constant.INotificationConstants;
import com.github.tianma8023.smscode.utils.CrashHandler;
import com.github.tianma8023.smscode.utils.XLog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class SmsCodeApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        initXLog();

        initCrashHandler();

        initWithUmengAnalyze();

        initNotificationChannel();
    }

    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = INotificationConstants.CHANNEL_ID_FOREGROUND_SERVICE;
            String channelName = getString(R.string.channel_name_foreground_service);
            createNotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    // umeng analyze initialization
    private void initWithUmengAnalyze() {
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_DUM_NORMAL);
        MobclickAgent.setCatchUncaughtExceptions(false);
    }

    private void initXLog() {
        XLog.init(this);
    }

    private void initCrashHandler() {
        CrashHandler.init(this, new CrashHandler.CrashUploader() {
            @Override
            public void upload(String crashInfo) {
                XLog.d("Upload crash info.");
            }
        });
    }

}
