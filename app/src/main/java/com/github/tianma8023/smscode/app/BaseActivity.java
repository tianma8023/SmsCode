package com.github.tianma8023.smscode.app;

import android.os.Bundle;

import com.github.tianma8023.smscode.app.theme.ThemeItemContainer;
import com.github.tianma8023.smscode.utils.SPUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
    }

    private void initTheme() {
        int index = SPUtils.getCurrentThemeIndex(this);
        setTheme(ThemeItemContainer.get().getItemAt(index).getThemeRes());
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
////        MobclickAgent.onResume(this);
//    }


//    @Override
//    protected void onPause() {
//        super.onPause();
////        MobclickAgent.onPause(this);
//    }
}
