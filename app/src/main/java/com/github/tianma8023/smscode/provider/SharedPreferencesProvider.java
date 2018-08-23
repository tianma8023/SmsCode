package com.github.tianma8023.smscode.provider;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
import com.github.tianma8023.smscode.constant.IPrefConstants;

/**
 * SharedPreferencesProvider for IPC
 */
public class SharedPreferencesProvider extends RemotePreferenceProvider {

    public SharedPreferencesProvider() {
        super(IPrefConstants.REMOTE_PREF_AUTHORITY, new String[]{IPrefConstants.REMOTE_PREF_NAME});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
//        if (write)
//            return false;
        return super.checkAccess(prefName, prefKey, write);
    }
}
