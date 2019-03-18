package com.github.tianma8023.smscode.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.utils.ClipboardUtils;

/**
 * Code copy service
 */
public class CodeCopyService extends Service {

    private static final String ACTION_COPY_CODE = "com.github.tianma8023.smscode.service.ACTION_COPY_CODE";
    private static final String EXTRA_KEY_CODE = "key_code";

    public CodeCopyService() {
    }

    public static Intent buildCopyCodeIntent(Context context, String smsCode) {
        Intent intent = new Intent(context, CodeCopyService.class);
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
}
