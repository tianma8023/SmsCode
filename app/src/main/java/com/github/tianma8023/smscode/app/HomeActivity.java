package com.github.tianma8023.smscode.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tianma8023.smscode.BuildConfig;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.app.base.BaseActivity;
import com.github.tianma8023.smscode.app.faq.FaqFragment;
import com.github.tianma8023.smscode.app.permissions.PermItemAdapter;
import com.github.tianma8023.smscode.app.permissions.PermItemContainer;
import com.github.tianma8023.smscode.app.theme.ThemeItem;
import com.github.tianma8023.smscode.app.theme.ThemeItemAdapter;
import com.github.tianma8023.smscode.app.theme.ThemeItemContainer;
import com.github.tianma8023.smscode.constant.PrefConst;
import com.github.tianma8023.smscode.service.SmsObserveService;
import com.github.tianma8023.smscode.utils.SPUtils;
import com.github.tianma8023.smscode.utils.SettingsUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class HomeActivity extends BaseActivity implements
        SettingsFragment.OnPreferenceClickCallback {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private static final String TAG_NESTED = "tag_nested";
    private static final String TAG_FAQ = "tag_faq";

    private Fragment mCurrentFragment;
    private FragmentManager mFragmentManager;

    private MaterialDialog mThemeChooseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        handleIntent(getIntent());

        // setup toolbar
        setupToolbar();
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean enable = SPUtils.isEnable(this);
        String listenMode = SPUtils.getListenMode(this);
        if (enable && PrefConst.LISTEN_MODE_COMPATIBLE.equals(listenMode)) {
            boolean isVerboseLog = SPUtils.isVerboseLogMode(this);
            SmsObserveService.startMe(this, isVerboseLog);
        } else {
            SmsObserveService.stopMe(this);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        refreshActionBar(getString(R.string.app_name));
    }

    private void handleIntent(Intent intent) {
        int themeIdx = SPUtils.getCurrentThemeIndex(this);
        ThemeItem themeItem = ThemeItemContainer.get().getItemAt(themeIdx);

        String action = intent.getAction();
        SettingsFragment settingsFragment = null;
        if (Intent.ACTION_VIEW.equals(action)) {
            String extraAction = intent.getStringExtra(SettingsFragment.EXTRA_ACTION);
            if (SettingsFragment.ACTION_GET_RED_PACKET.equals(extraAction)) {
                settingsFragment = SettingsFragment.newInstance(themeItem, extraAction);
            }
        }

        if (settingsFragment == null) {
            settingsFragment = SettingsFragment.newInstance(themeItem);
        }

        settingsFragment.setOnPreferenceClickCallback(this);
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.home_content, settingsFragment)
                .commit();
        mCurrentFragment = settingsFragment;
    }

    @Override
    public void onPreferenceClicked(String key, String title, boolean nestedPreference) {
        if (nestedPreference) {
            onNestedPreferenceClicked(key, title);
            return;
        }
        if (PrefConst.CHOOSE_THEME.equals(key)) {
            onChooseThemePreferenceClicked();
        }
    }

    private void onNestedPreferenceClicked(String key, String title) {
        Fragment newFragment = null;
        if (PrefConst.ENTRY_AUTO_INPUT_CODE.equals(key)) {
            newFragment = new AutoInputSettingsFragment();
        }
        if (newFragment == null)
            return;

        mFragmentManager
                .beginTransaction()
                .replace(R.id.home_content, newFragment, TAG_NESTED)
                .addToBackStack(TAG_NESTED)
                .commit();
        mCurrentFragment = newFragment;
        refreshActionBar(title);
    }

    private void refreshActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setHomeButtonEnabled(true);
            if (mCurrentFragment instanceof SettingsFragment) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            mFragmentManager.popBackStackImmediate();
            mCurrentFragment = mFragmentManager.findFragmentById(R.id.home_content);
            refreshActionBar(getString(R.string.app_name));
        }
        invalidateOptionsMenu();
    }

    private BaseQuickAdapter.OnItemClickListener mItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            if (mThemeChooseDialog != null && mThemeChooseDialog.isShowing()) {
                mThemeChooseDialog.dismiss();
            }

            if (SPUtils.getCurrentThemeIndex(HomeActivity.this) == position) {
                return;
            }
            SPUtils.setCurrentThemeIndex(HomeActivity.this, position);
            recreate();
        }
    };

    private void onChooseThemePreferenceClicked() {
        if (mThemeChooseDialog == null) {
            ThemeItemAdapter adapter = new ThemeItemAdapter(this,
                    ThemeItemContainer.get().getThemeItemList());
            adapter.setOnItemClickListener(mItemClickListener);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

            mThemeChooseDialog = new MaterialDialog.Builder(this)
                    .title(R.string.pref_choose_theme_title)
                    .adapter(adapter, layoutManager)
                    .negativeText(R.string.cancel)
                    .build();

            RecyclerView recyclerView = mThemeChooseDialog.getRecyclerView();
            recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        }
        mThemeChooseDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_home_faq:
                onFAQSelected();
                return true;
            case R.id.action_perm_state:
                onPermStateSelected();
                return true;
            case R.id.action_ignore_battery_optimization:
                onIgnoreBatteryOptimizationSelected();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem faqItem = menu.findItem(R.id.action_home_faq);
        if (mCurrentFragment instanceof FaqFragment) {
            faqItem.setVisible(false);
        } else {
            faqItem.setVisible(true);
        }

        MenuItem ignoreOptimizeItem = menu.findItem(R.id.action_ignore_battery_optimization);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ignoreOptimizeItem.setVisible(false);
        } else {
            ignoreOptimizeItem.setVisible(true);
        }
        return true;
    }

    private void onFAQSelected() {
        FaqFragment faqFragment = FaqFragment.newInstance();
        mFragmentManager
                .beginTransaction()
                .replace(R.id.home_content, faqFragment, TAG_FAQ)
                .addToBackStack(TAG_FAQ)
                .commit();
        mCurrentFragment = faqFragment;
        refreshActionBar(getString(R.string.action_home_faq_title));
        invalidateOptionsMenu();
    }

    private void onPermStateSelected() {
        PermItemAdapter adapter = new PermItemAdapter(new PermItemContainer(this).getItems());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        new MaterialDialog.Builder(this)
                .title(R.string.permission_statement)
                .adapter(adapter, layoutManager)
                .positiveText(R.string.confirm)
                .show();
    }

    private void onIgnoreBatteryOptimizationSelected() {
        new MaterialDialog.Builder(this)
                .title(R.string.ignore_battery_optimization_statement)
                .content(R.string.ignore_battery_optimization_content)
                .positiveText(R.string.yes)
                .onPositive((dialog, which) -> ignoreBatteryOptimization())
                .negativeText(R.string.no)
                .show();
    }

    @SuppressLint("BatteryLife")
    private void ignoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm == null) {
            return;
        }

        if (pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)) {
            Toast.makeText(this, R.string.battery_optimization_ignored, Toast.LENGTH_LONG).show();
        } else {
            try {
                // 申请忽略电源优化
                SettingsUtils.requestIgnoreBatteryOptimization(this);
            } catch (Exception e) {
                try {
                    // 跳转至电源优化界面
                    SettingsUtils.gotoIgnoreBatteryOptimizationSettings(this);
                    Toast.makeText(this, R.string.ignore_battery_optimization_manually, Toast.LENGTH_LONG).show();
                } catch (Exception e1) {
                    Toast.makeText(this, R.string.ignore_battery_optimization_settings_failed, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
