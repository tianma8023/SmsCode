package com.github.tianma8023.smscode.app.base;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    @Override
    @NonNull
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        return super.findPreference(key);
    }

}
