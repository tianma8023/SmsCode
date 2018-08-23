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
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crossbowffs.remotepreferences.RemotePreferences;
import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.app.theme.ThemeItem;
import com.github.tianma8023.smscode.constant.IConstants;
import com.github.tianma8023.smscode.constant.IPrefConstants;
import com.github.tianma8023.smscode.utils.PackageUtils;
import com.github.tianma8023.smscode.utils.RemotePreferencesUtils;
import com.github.tianma8023.smscode.utils.VerificationUtils;
import com.github.tianma8023.smscode.utils.XLog;
import com.github.tianma8023.smscode.utils.rom.MiuiUtils;
import com.github.tianma8023.smscode.utils.rom.RomUtils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;

/**
 * 首选项Fragment
 */
public class SettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static final String EXTRA_KEY_CURRENT_THEME = "extra_key_current_theme";

    private HomeActivity mHomeActivity;

    private SwitchPreference mEnablePreference;

    private RemotePreferences mRemotePreferences;

    public interface OnPreferenceClickCallback {
        void onPreferenceClicked(String key, String title, boolean nestedPreference);
    }

    private OnPreferenceClickCallback mPreferenceClickCallback;

    private boolean mIsFirstRunSinceV1;
    private WebView mPermStateWebView;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(ThemeItem curThemeItem) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_KEY_CURRENT_THEME, curThemeItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        findPreference(IPrefConstants.KEY_VERBOSE_LOG_MODE).setOnPreferenceChangeListener(this);
        mEnablePreference = (SwitchPreference) findPreference(IPrefConstants.KEY_ENABLE);
        mEnablePreference.setOnPreferenceChangeListener(this);

        findPreference(IPrefConstants.KEY_SOURCE_CODE).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_DONATE_BY_ALIPAY).setOnPreferenceClickListener(this);
        // findPreference(IPrefConstants.KEY_DONATE_BY_WECHAT).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_SMSCODE_TEST).setOnPreferenceClickListener(this);
        findPreference(IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE).setOnPreferenceClickListener(this);
        Preference chooseThemePref = findPreference(IPrefConstants.KEY_CHOOSE_THEME);
        chooseThemePref.setOnPreferenceClickListener(this);
        initChooseThemePreference(chooseThemePref);

        // Hide mark as read preference item.
        Preference markAsReadPref = findPreference(IPrefConstants.KEY_MARK_AS_READ);
        PreferenceGroup experimentalGroup = (PreferenceGroup) findPreference(IPrefConstants.KEY_EXPERIMENTAL);
        experimentalGroup.removePreference(markAsReadPref);

        // Hide donate by wechat preference item
        Preference donateByWechat = findPreference(IPrefConstants.KEY_DONATE_BY_WECHAT);
        PreferenceGroup aboutGroup = (PreferenceGroup) findPreference(IPrefConstants.KEY_ABOUT);
        aboutGroup.removePreference(donateByWechat);
    }

    private void initChooseThemePreference(Preference chooseThemePref) {
        Bundle args = getArguments();
        ThemeItem themeItem = args.getParcelable(EXTRA_KEY_CURRENT_THEME);
        if (themeItem != null) {
            chooseThemePref.setSummary(themeItem.getColorNameRes());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHomeActivity = (HomeActivity) getActivity();
        mRemotePreferences = RemotePreferencesUtils.getDefaultRemotePreferences(mHomeActivity);
        mIsFirstRunSinceV1 = RemotePreferencesUtils.isFirstRunSinceV1(mRemotePreferences);
        initIfIsFirstRunV1();
    }

    private void initIfIsFirstRunV1() {
        if (mIsFirstRunSinceV1) {
            View dialogView = mHomeActivity.getLayoutInflater().inflate(R.layout.dialog_perm_state, null);
            mPermStateWebView = dialogView.findViewById(R.id.perm_state_webview);
            mPermStateWebView.loadUrl("file:///android_res/raw/perm_state.html");
        }
    }

    public void setOnPreferenceClickCallback(OnPreferenceClickCallback preferenceClickCallback) {
        mPreferenceClickCallback = preferenceClickCallback;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
            if (mPreferenceClickCallback != null) {
                mPreferenceClickCallback.onPreferenceClicked(key, preference.getTitle().toString(), true);
            }
        } else if (IPrefConstants.KEY_CHOOSE_THEME.equals(key)) {
            if (mPreferenceClickCallback != null) {
                mPreferenceClickCallback.onPreferenceClicked(key, preference.getTitle().toString(), false);
            }
        } else if (IPrefConstants.KEY_SMSCODE_TEST.equals(key)) {
            showSmsCodeTestDialog();
        } else if (IPrefConstants.KEY_SOURCE_CODE.equals(key)) {
            aboutProject();
        } else if (IPrefConstants.KEY_DONATE_BY_ALIPAY.equals(key)) {
            donateByAlipay();
        } else if (IPrefConstants.KEY_DONATE_BY_WECHAT.equals(key)) {
            donateByWechat();
        } else {
            return false;
        }
        return true;
    }

    private void aboutProject() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(IConstants.PROJECT_SOURCE_CODE_URL));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mHomeActivity, R.string.browser_install_or_enable_prompt, Toast.LENGTH_SHORT).show();
        }
    }

    private void donateByAlipay() {
        if (!PackageUtils.isWeChatInstalled(mHomeActivity)) { // uninstalled
            Toast.makeText(mHomeActivity, R.string.wechat_install_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PackageUtils.isWeChatEnabled(mHomeActivity)) { // installed but disabled
            Toast.makeText(mHomeActivity, R.string.wechat_enable_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(IConstants.WECHAT_PACKAGE_NAME, IConstants.WECHAT_LAUNCHER_UI);
        intent.putExtra(IConstants.WECHAT_KEY_EXTRA_DONATE, true);
        startActivity(intent);
    }

    private void donateByWechat() {
        if (!PackageUtils.isAlipayInstalled(mHomeActivity)) { // uninstalled
            Toast.makeText(mHomeActivity, R.string.alipay_install_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PackageUtils.isAlipayEnabled(mHomeActivity)) { // installed but disabled
            Toast.makeText(mHomeActivity, R.string.alipay_enable_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(IConstants.ALIPAY_QRCODE_URI_PREFIX + IConstants.ALIPAY_QRCODE_URL));
        startActivity(intent);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (IPrefConstants.KEY_ENABLE.equals(key)) {
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
        if (!enable) {
            return;
        }
        if (mIsFirstRunSinceV1) {
            showPermissionStatement();
            mIsFirstRunSinceV1 = false;
            RemotePreferencesUtils.setFirstRunSinceV1(mRemotePreferences, mIsFirstRunSinceV1);
        } else {
            tryToAcquireNecessaryPermissions();
        }
    }

    // 申请必要权限
    private void tryToAcquireNecessaryPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        Rationale<List<String>> rationale = new Rationale<List<String>>() {
            @Override
            public void showRationale(Context context, List<String> data, final RequestExecutor executor) {
                new MaterialDialog.Builder(mHomeActivity)
                        .title(R.string.permission_requirement)
                        .content(R.string.receive_sms_permission_requirement)
                        .positiveText(R.string.okay)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                executor.execute();
                            }
                        })
                        .negativeText(R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                executor.cancel();
                            }
                        })
                        .show();
            }
        };

        AndPermission.with(this)
                .runtime()
                .permission(Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS)
                .rationale(rationale)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        requestOtherPermissionsIfNecessary();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Toast.makeText(mHomeActivity, R.string.prompt_sms_permission_denied, Toast.LENGTH_LONG).show();
                        mEnablePreference.setChecked(false);
                    }
                })
                .start();
    }

    // 展示权限声明
    private void showPermissionStatement() {
        new MaterialDialog.Builder(mHomeActivity)
                .title(R.string.permission_statement)
                .customView(mPermStateWebView, false)
                .positiveText(R.string.okay)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        tryToAcquireNecessaryPermissions();
                    }
                })
                .show();
    }

    // 必要情况下，申请其他更多权限（MIUI的"通知类短信"权限）
    private void requestOtherPermissionsIfNecessary() {
        if (RomUtils.isMiui()) {
            if (!RemotePreferencesUtils.isServiceSmsPromptShown(mRemotePreferences)) {
                new MaterialDialog.Builder(mHomeActivity)
                        .title(R.string.permission_requirement)
                        .content(R.string.service_sms_permission_requirement)
                        .positiveText(R.string.okay)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MiuiUtils.goToPermissionEditorActivity(mHomeActivity);
                                RemotePreferencesUtils.setServiceSmsPromptShown(mRemotePreferences, true);
                            }
                        })
                        .negativeText(R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Toast.makeText(mHomeActivity, R.string.prompt_service_sms_permission_denied, Toast.LENGTH_LONG).show();
                                mEnablePreference.setChecked(false);
                            }
                        })
                        .show();
            }
        }
    }
}
