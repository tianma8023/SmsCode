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
            XLog.d("BootReceiver received: {}", intent.getAction());
            boolean isVerboseLog = SPUtils.isVerboseLogMode(context);
            try {
                SmsObserveService.startMe(context, isVerboseLog);
            } catch (Exception e) {
                // 未置为电池优化白名单
                // Not allowed to start service Intent { cmp=com.github.tianma8023.smscode/.service.SmsObserveService (has extras) }: app is in background uid UidRecord
                // 有两种解决方案：
                // 1. 利用 JobService 处理，但是实时性不好，不适用于本方案
                // 2. try catch
            }
        }
    }
}
