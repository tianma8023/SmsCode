package com.github.tianma8023.smscode.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.utils.ClipboardUtils;

/**
 * Code copy service
 */
public class CopyCodeService extends Service {

    private static final String ACTION_COPY_CODE = BuildConfig.APPLICATION_ID + ".action.COPY_CODE";
    private static final String EXTRA_KEY_CODE = "key_code";

    public CopyCodeService() {
    }

    public static Intent createCopyCodeIntent(Context context, String smsCode) {
        Intent intent = new Intent(context, CopyCodeService.class);
        intent.setAction(ACTION_COPY_CODE);
        intent.putExtra(EXTRA_KEY_CODE, smsCode);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_COPY_CODE.equals(action)) {
                final String smsCode = intent.getStringExtra(EXTRA_KEY_CODE);
                handleActionCopyCode(smsCode);
                sendStopHandleServiceBroadcast(this);
            }
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    private void handleActionCopyCode(String smsCode) {
        ClipboardUtils.copyToClipboard(this, smsCode);

        String content = getString(R.string.prompt_sms_code_copied, smsCode);
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }

    private void sendStopHandleServiceBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(SmsCodeHandleService.ACTION_STOP_HANDLE_SERVICE);
        context.sendBroadcast(intent);
    }
}
