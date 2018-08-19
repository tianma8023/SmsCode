package com.github.tianma8023.smscode.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.constant.IConstants;
import com.github.tianma8023.smscode.constant.IPrefConstants;
import com.github.tianma8023.smscode.utils.PackageUtils;
import com.github.tianma8023.smscode.utils.VerificationUtils;
import com.github.tianma8023.smscode.utils.XLog;

import permissions.dispatcher.PermissionUtils;

/**
 * 首选项Fragment
 */
public class SettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private HomeActivity mHomeActivity;

    public interface OnNestedPreferenceClickListener {
        void onNestedPreferenceClicked(String key, String title);
    }

    public interface OnPreferenceSwitchedListener {
        void onPreferenceSwitched(String key, boolean newValue);
    }

    private OnNestedPreferenceClickListener mNestedPreferenceClickListener;
    private OnPreferenceSwitchedListener mPreferenceSwitchedListener;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        findPreference(IPrefConstants.KEY_VERBOSE_LOG_MODE).setOnPreferenceChangeListener(this);
        findPreference(IPrefConstants.KEY_ENABLE).setOnPreferenceChangeListener(this);

        findPreference(IPrefConstants.KEY_SOURCE_CODE).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_ALIPAY).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_WECHAT).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_SMSCODE_TEST).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE).setOnPreferenceClickListener(this);

        // Hide mark as read preference item.
        Preference markAsReadPref = findPreference(IPrefConstants.KEY_MARK_AS_READ);
        PreferenceGroup experimentalGroup = (PreferenceGroup) findPreference(IPrefConstants.KEY_EXPERIMENTAL);
        experimentalGroup.removePreference(markAsReadPref);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHomeActivity = (HomeActivity) getActivity();
    }

    public void setOnNestedPreferenceClickListener(OnNestedPreferenceClickListener nestedPreferenceClickListener) {
        mNestedPreferenceClickListener = nestedPreferenceClickListener;
    }

    public void setOnPreferenceSwitchedListener(OnPreferenceSwitchedListener listener) {
        mPreferenceSwitchedListener = listener;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_SOURCE_CODE.equals(key)) {
            aboutProject();
        } else if (IPrefConstants.KEY_DONATE_BY_ALIPAY.equals(key)) {
            donateByAlipay();
        } else if (IPrefConstants.KEY_DONATE_BY_WECHAT.equals(key)) {
            donateByWechat();
        } else if (IPrefConstants.KEY_SMSCODE_TEST.equals(key)) {
            showSmsCodeTestDialog();
        } else if (IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
            if (mNestedPreferenceClickListener != null) {
                mNestedPreferenceClickListener.onNestedPreferenceClicked(key, preference.getTitle().toString());
            }
        } else {
            return false;
        }
        return true;
    }

    private void aboutProject() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(IConstants.PROJECT_SOURCE_CODE_URL));
        startActivity(intent);
    }

    private void donateByAlipay() {
        if (PackageUtils.isAlipayInstalled(mHomeActivity)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(IConstants.ALIPAY_QRCODE_URI_PREFIX
                    + IConstants.ALIPAY_QRCODE_URL));
            startActivity(intent);
        } else {
            Toast.makeText(mHomeActivity, R.string.alipay_install_prompt, Toast.LENGTH_SHORT).show();
        }
    }

    private void donateByWechat() {
        if (PackageUtils.isWeChatInstalled(mHomeActivity)) {
            Intent intent = new Intent();
            intent.setClassName(IConstants.WECHAT_PACKAGE_NAME, IConstants.WECHAT_LAUNCHER_UI);
            intent.putExtra(IConstants.WECHAT_KEY_EXTRA_DONATE, true);
            startActivity(intent);
        } else {
            Toast.makeText(mHomeActivity, R.string.wechat_install_prompt, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_ENABLE.equals(key)) {
//            if (mPreferenceSwitchedListener != null) {
//                mPreferenceSwitchedListener.onPreferenceSwitched(key, (Boolean) newValue);
//            }
            onEnabledSwitched((Boolean) newValue);
        } else if (IPrefConstants.KEY_VERBOSE_LOG_MODE.equals(key)) {
            onVerboseLogModeSwitched((Boolean) newValue);
        } else {
            return false;
        }
        return true;
    }

    private void onVerboseLogModeSwitched(boolean on) {
        if (on) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }
    }

    private void showSmsCodeTestDialog() {
        new MaterialDialog.Builder(mHomeActivity)
                .title(R.string.pref_smscode_test)
                .input(R.string.sms_content_hint, 0, true, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        new Thread(new SmsCodeTestTask(mHomeActivity, input.toString())).start();
                    }
                })
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .negativeText(R.string.cancel)
                .show();
    }

    private class SmsCodeTestTask implements Runnable {

        private String mMsgBody;
        private Context mContext;

        SmsCodeTestTask(Context context, String msgBody) {
            mMsgBody = msgBody;
            mContext = context;
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = MSG_SMSCODE_TEST;
            if (TextUtils.isEmpty(mMsgBody)) {
                msg.obj = "";
            } else {
                msg.obj = VerificationUtils.parseVerificationCodeIfExists(mContext, mMsgBody);
            }
            mHandler.sendMessage(msg);
        }
    }

    private static final int MSG_SMSCODE_TEST = 0xff;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMSCODE_TEST:
                    handleSmsCode((String) msg.obj);
                    return true;
            }
            return false;
        }
    });

    private void handleSmsCode(String verificationCode) {
        String text;
        if (TextUtils.isEmpty(verificationCode)) {
            text = getString(R.string.cannot_parse_smscode);
        } else {
            text = getString(R.string.cur_verification_code, verificationCode);
        }
        Toast.makeText(mHomeActivity, text, Toast.LENGTH_LONG).show();
    }

    private void onEnabledSwitched(boolean enable) {
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tryToAcquireNecessaryPermissions();
            }
        }
    }

    private static final int REQUEST_CODE_RECEIVE_SMS = 0xff;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void tryToAcquireNecessaryPermissions() {
        boolean granted = PermissionUtils.hasSelfPermissions(mHomeActivity, Manifest.permission.RECEIVE_SMS);
        if (!granted) {
            new MaterialDialog.Builder(mHomeActivity)
                    .title("权限获取")
                    .content("需要获取读取短信权限")
                    .positiveText("好的")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_RECEIVE_SMS);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
