package com.github.tianma8023.smscode.app.theme;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tianma8023.smscode.R;

import java.util.List;

import androidx.core.content.ContextCompat;

public class ThemeItemAdapter extends BaseQuickAdapter<ThemeItem, BaseViewHolder> {

    private Context mContext;

    public ThemeItemAdapter(Context context, List<ThemeItem> themeItemList) {
        super(R.layout.theme_item, themeItemList);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, ThemeItem item) {
        helper.setBackgroundColor(R.id.tv_color_item, ContextCompat.getColor(mContext, item.getColorValueRes()))
                .setText(R.id.tv_color_item, item.getColorNameRes());
    }
}
