package com.github.tianma8023.smscode.app;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.service.accessibility.SmsCodeAutoInputService;
import com.github.tianma8023.smscode.utils.AccessibilityUtils;
import com.github.tianma8023.smscode.utils.ShellUtils;

import static com.github.tianma8023.smscode.constant.IPrefConstants.AUTO_INPUT_MODE_ACCESSIBILITY;
import static com.github.tianma8023.smscode.constant.IPrefConstants.AUTO_INPUT_MODE_ROOT;
import static com.github.tianma8023.smscode.constant.IPrefConstants.KEY_AUTO_INPUT_MODE;
import static com.github.tianma8023.smscode.constant.IPrefConstants.KEY_ENABLE_AUTO_INPUT_CODE;
import static com.github.tianma8023.smscode.constant.IPrefConstants.KEY_FOCUS_MODE;

public class AutoInputSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private Context mContext;

    private SwitchPreference mAutoInputEnablePref;
    private ListPreference mAutoInputModePref;

    private String mCurAutoMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_auto_input_code);

        mAutoInputEnablePref = (SwitchPreference) findPreference(KEY_ENABLE_AUTO_INPUT_CODE);
        mAutoInputEnablePref.setOnPreferenceChangeListener(this);

        mAutoInputModePref = (ListPreference) findPreference(KEY_AUTO_INPUT_MODE);
        mAutoInputModePref.setOnPreferenceChangeListener(this);
        mCurAutoMode = mAutoInputModePref.getValue();

        ListPreference focusModePref = (ListPreference) findPreference(KEY_FOCUS_MODE);
        focusModePref.setOnPreferenceChangeListener(this);

        refreshEnableAutoInputPreference(mAutoInputEnablePref.isChecked());
        refreshAutoInputModePreference(mCurAutoMode);
        refreshFocusModePreference(focusModePref, focusModePref.getValue());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case KEY_ENABLE_AUTO_INPUT_CODE:
                refreshEnableAutoInputPreference((Boolean) newValue);
                break;
            case KEY_AUTO_INPUT_MODE:
                if (!newValue.equals(mCurAutoMode)) {
                    mCurAutoMode = (String) newValue;
                    refreshAutoInputModePreference(mCurAutoMode);
                    if (AUTO_INPUT_MODE_ROOT.equals(newValue)) {
                        showRootModePrompt();
                    } else if (AUTO_INPUT_MODE_ACCESSIBILITY.equals(mCurAutoMode)) {
                        showAccessibilityModePrompt();
                    }
                }
                break;
            case KEY_FOCUS_MODE:
                refreshFocusModePreference((ListPreference) preference, (String) newValue);
                break;
            default:
                return false;
        }
        return true;
    }

    private void showAccessibilityModePrompt() {
        String serviceId = AccessibilityUtils.getServiceId(SmsCodeAutoInputService.class);
        boolean accessibilityEnabled = AccessibilityUtils.checkAccessibilityEnabled(getActivity(), serviceId);

        if (!accessibilityEnabled) {
            new MaterialDialog.Builder(mContext)
                    .title(R.string.open_auto_input_accessibility)
                    .content(R.string.open_auto_input_accessibility_prompt)
                    .positiveText(R.string.go_to_open)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            AccessibilityUtils.gotoAccessibility(mContext);
                        }
                    })
                    .show();
        }
    }

    private void showRootModePrompt() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.acquire_root_permission)
                .content(R.string.acquire_root_permission_prompt)
                .positiveText(R.string.okay)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ShellUtils.checkRootPermission();
                    }
                })
                .show();
    }

    private void refreshEnableAutoInputPreference(boolean autoInputEnabled) {
        if (!autoInputEnabled) {
            mAutoInputEnablePref.setSummary(R.string.pref_entry_auto_input_code_summary);
        } else {
            if (TextUtils.isEmpty(mAutoInputModePref.getValue())) {
                Toast.makeText(getActivity(), R.string.pref_auto_input_mode_summary_default, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshAutoInputModePreference(String newValue) {
        if (TextUtils.isEmpty(newValue)) {
            mAutoInputModePref.setSummary(R.string.pref_auto_input_mode_summary_default);
            return;
        }
        CharSequence[] entries = mAutoInputModePref.getEntries();
        int index = mAutoInputModePref.findIndexOfValue(newValue);
        try {
            mAutoInputModePref.setSummary(entries[index]);
        } catch (Exception e) {
            // ignore
        }
    }

    private void refreshFocusModePreference(ListPreference focusModePref, String newValue) {
        if (TextUtils.isEmpty(newValue))
            return;
        CharSequence[] entries = focusModePref.getEntries();
        int index = focusModePref.findIndexOfValue(newValue);
        try {
            focusModePref.setSummary(entries[index]);
        } catch (Exception e) {
            //ignore
        }
    }

}
