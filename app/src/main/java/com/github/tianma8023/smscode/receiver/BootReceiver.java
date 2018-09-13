package com.github.tianma8023.smscode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.tianma8023.smscode.constant.PrefConst;
import com.github.tianma8023.smscode.service.SmsObserveService;
import com.github.tianma8023.smscode.utils.SPUtils;
import com.github.tianma8023.smscode.utils.XLog;

/**
 * Start SMS observe service if necessary after booting completed.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enable = SPUtils.isEnable(context);
        String listenMode = SPUtils.getListenMode(context);
        if (enable && PrefConst.LISTEN_MODE_COMPATIBLE.equals(listenMode)) {
            XLog.d("BootReceiver#onReceived() - {}", intent.getAction());
            boolean isVerboseLog = SPUtils.isVerboseLogMode(context);
            SmsObserveService.startMe(context, isVerboseLog);
        }
    }
}
