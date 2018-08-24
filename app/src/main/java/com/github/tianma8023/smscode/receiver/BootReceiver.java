package com.github.tianma8023.smscode.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.smscode.constant.IPrefConstants;
import com.github.tianma8023.smscode.service.SmsObserveService;
import com.github.tianma8023.smscode.utils.RemotePreferencesUtils;

/**
 * Start SMS observe service if necessary after booting completed.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        RemotePreferences mPreferences = RemotePreferencesUtils.getDefaultRemotePreferences(context);
        boolean enable = mPreferences.getBoolean(IPrefConstants.KEY_ENABLE,
                IPrefConstants.KEY_ENABLE_DEFAULT);
        String listenMode = mPreferences.getString(IPrefConstants.KEY_LISTEN_MODE,
                IPrefConstants.KEY_LISTEN_MODE_STANDARD);
        if (enable && IPrefConstants.KEY_LISTEN_MODE_COMPATIBLE.equals(listenMode)) {
            SmsObserveService.startMe(context);
        }
    }
}
