package com.github.tianma8023.smscode.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.tianma8023.smscode.R;
import com.github.tianma8023.smscode.adapter.BaseItemCallback;
import com.github.tianma8023.smscode.adapter.ItemCallback;
import com.github.tianma8023.smscode.app.faq.FaqFragment;
import com.github.tianma8023.smscode.app.theme.ThemeItem;
import com.github.tianma8023.smscode.app.theme.ThemeItemAdapter;
import com.github.tianma8023.smscode.app.theme.ThemeItemContainer;
import com.github.tianma8023.smscode.constant.PrefConst;
import com.github.tianma8023.smscode.service.SmsObserveService;
import com.github.tianma8023.smscode.utils.ResUtils;
import com.github.tianma8023.smscode.utils.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class HomeActivity extends BaseActivity implements
        SettingsFragment.OnPreferenceClickCallback {

    @BindView(R.id.toolbar) Toolbar mToolbar;

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

        // init main fragment
        int index = SPUtils.getCurrentThemeIndex(this);
        ThemeItem curThemeItem = ThemeItemContainer.get().getItemAt(index);
        SettingsFragment settingsFragment = SettingsFragment.newInstance(curThemeItem);
        settingsFragment.setOnPreferenceClickCallback(this);
        mFragmentManager = getFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.home_content, settingsFragment)
                .commit();
        mCurrentFragment = settingsFragment;

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

    @Override
    public void onPreferenceClicked(String key, String title, boolean nestedPreference) {
        if (nestedPreference) {
            onNestedPreferenceClicked(key, title);
            return;
        }
        if (PrefConst.KEY_CHOOSE_THEME.equals(key)) {
            onChooseThemePreferenceClicked();
        }
    }

    private void onNestedPreferenceClicked(String key, String title) {
        Fragment newFragment = null;
        if (PrefConst.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
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
        if(actionBar != null) {
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

    private ItemCallback<ThemeItem> mThemeItemCallback = new BaseItemCallback<ThemeItem>() {
        @Override
        public void onItemClicked(ThemeItem item, int position) {
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
            adapter.setItemCallback(mThemeItemCallback);
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_perm_state, null);
        WebView permStateWebView = dialogView.findViewById(R.id.perm_state_webview);
        String data = ResUtils.loadRawRes(this, R.raw.perm_state);
        permStateWebView.loadDataWithBaseURL("file:///android_asset/",
                data, "text/html", "utf-8", null);
        new MaterialDialog.Builder(this)
                .title(R.string.permission_statement)
                .customView(permStateWebView, false)
                .positiveText(R.string.confirm)
                .show();
    }
}
