package com.github.tianma8023.smscode.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
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
import com.github.tianma8023.smscode.app.faq.FaqFragment;
import com.github.tianma8023.smscode.app.theme.ItemCallback;
import com.github.tianma8023.smscode.app.theme.ThemeItem;
import com.github.tianma8023.smscode.app.theme.ThemeItemAdapter;
import com.github.tianma8023.smscode.constant.IPrefConstants;
import com.github.tianma8023.smscode.service.SmsObserveService;

import java.util.ArrayList;
import java.util.List;

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

    private List<ThemeItem> mThemeItemList;
    // current theme index
    private int mCurThemeIndex;
    private MaterialDialog mThemeChooseDialog;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(
                IPrefConstants.REMOTE_PREF_NAME, MODE_PRIVATE);

        initTheme();
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // init main fragment
        ThemeItem curThemeItem = mThemeItemList.get(mCurThemeIndex);
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
    protected void onPause() {
        super.onPause();

        boolean enable = mPreferences.getBoolean(IPrefConstants.KEY_ENABLE,
                IPrefConstants.KEY_ENABLE_DEFAULT);
        String listenMode = mPreferences.getString(IPrefConstants.KEY_LISTEN_MODE,
                IPrefConstants.KEY_LISTEN_MODE_STANDARD);
        if (enable && IPrefConstants.KEY_LISTEN_MODE_COMPATIBLE.equals(listenMode)) {
            SmsObserveService.startMe(this);
        } else {
            SmsObserveService.stopMe(this);
        }
    }

    private void initTheme() {
        mThemeItemList = loadThemeColorItems();
        mCurThemeIndex = mPreferences.getInt(IPrefConstants.KEY_CURRENT_THEME_INDEX,
                IPrefConstants.KEY_CURRENT_THEME_INDEX_DEFAULT);
        // check current theme index in case of exception.
        if(mCurThemeIndex < 0 || mCurThemeIndex >= mThemeItemList.size()) {
            mCurThemeIndex = IPrefConstants.KEY_CURRENT_THEME_INDEX_DEFAULT;
        }
        setTheme(mThemeItemList.get(mCurThemeIndex).getThemeRes());
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
        if (IPrefConstants.KEY_CHOOSE_THEME.equals(key)) {
            onChooseThemePreferenceClicked();
        }
    }

    private void onNestedPreferenceClicked(String key, String title) {
        Fragment newFragment = null;
        if (IPrefConstants.KEY_ENTRY_AUTO_INPUT_CODE.equals(key)) {
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

    private ItemCallback<ThemeItem> mThemeItemCallback = new ItemCallback<ThemeItem>() {
        @Override
        public void onItemClicked(ThemeItem item, int position) {
            if (mThemeChooseDialog != null && mThemeChooseDialog.isShowing()) {
                mThemeChooseDialog.dismiss();
            }

            if (mCurThemeIndex == position) {
                return;
            }
            mPreferences.edit()
                    .putInt(IPrefConstants.KEY_CURRENT_THEME_INDEX, position)
                    .apply();
            recreate();
        }
    };

    private void onChooseThemePreferenceClicked() {
        if (mThemeItemList == null || mThemeItemList.isEmpty()) {
            mThemeItemList = loadThemeColorItems();
        }
        ThemeItemAdapter adapter = new ThemeItemAdapter(this, mThemeItemList);
        adapter.setItemCallback(mThemeItemCallback);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        mThemeChooseDialog = new MaterialDialog.Builder(this)
                .title(R.string.pref_choose_theme_title)
                .adapter(adapter, layoutManager)
                .negativeText(R.string.cancel)
                .build();

        RecyclerView recyclerView = mThemeChooseDialog.getRecyclerView();
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        mThemeChooseDialog.show();
    }

    private List<ThemeItem> loadThemeColorItems() {
        List<ThemeItem> themeItems = new ArrayList<>();
        @StringRes int[] colorNameResArray = {
                R.string.color_default,
                R.string.red,
                R.string.pink,
                R.string.yellow,
                R.string.green,
                R.string.blue,
                R.string.violet,
                R.string.black,
        };
        @ColorRes int[] colorValueResArray = {
                R.color.colorPrimaryDark,
                R.color.colorPrimaryDark_red,
                R.color.colorPrimaryDark_pink,
                R.color.colorPrimaryDark_yellow,
                R.color.colorPrimaryDark_green,
                R.color.colorPrimaryDark_blue,
                R.color.colorPrimary_violet,
                R.color.colorPrimary_black,
        };
        @StyleRes int[] themeResArray = {
                R.style.AppTheme,
                R.style.AppTheme_Red,
                R.style.AppTheme_Pink,
                R.style.AppTheme_Yellow,
                R.style.AppTheme_Green,
                R.style.AppTheme_Blue,
                R.style.AppTheme_Violet,
                R.style.AppTheme_Black,
        };

        for(int i = 0; i < colorNameResArray.length; i++) {
            themeItems.add(new ThemeItem(
                    colorNameResArray[i],
                    colorValueResArray[i],
                    themeResArray[i]
            ));
        }
        return themeItems;
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
        getMenuInflater().inflate(R.menu.home_menu, menu);
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
        permStateWebView.loadUrl("file:///android_res/raw/perm_state.html");
        new MaterialDialog.Builder(this)
                .title(R.string.permission_statement)
                .customView(permStateWebView, false)
                .positiveText(R.string.confirm)
                .show();
    }
}