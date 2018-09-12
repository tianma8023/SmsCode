package com.github.tianma8023.smscode.migrate;

import android.content.Context;

import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.utils.SPUtils;
import com.github.tianma8023.smscode.utils.XLog;

/**
 * SharedPreferences 相关数据迁移
 */
public class PreferencesTransition implements ITransition {

    private Context mContext;
    private int mLocalVersionCode;

    private static final int VERSION_CODE_2 = 2;

    PreferencesTransition(Context context) {
        mContext = context;
        mLocalVersionCode = SPUtils.getLocalVersionCode(mContext);
    }

    @Override
    public boolean shouldTransit() {
        if (mLocalVersionCode <= VERSION_CODE_2) {
            // v1.0.1 及以前版本，需要进行数据兼容
            return true;
        }
        return false;
    }

    @Override
    public boolean doTransition() {
        try {
            if (mLocalVersionCode <= VERSION_CODE_2) {
                if (SPUtils.isAutoInputRootMode(mContext)) {
                    // auto-input 模式是 root模式
                    SPUtils.setAutoInputMode(mContext,
                            mContext.getString(R.string.auto_input_mode_root));
                } else if (SPUtils.isAutoInputAccessibilityMode(mContext)) {
                    // auto-input 模式是 accessibility模式
                    SPUtils.setAutoInputMode(mContext,
                            mContext.getString(R.string.auto_input_mode_accessibility));
                }
            }
            SPUtils.setLocalVersionCode(mContext, BuildConfig.VERSION_CODE);
            return true;
        } catch (Exception e) {
            XLog.d("Error occurs when do preferences transition.", e);
        }
        return false;
    }
}
