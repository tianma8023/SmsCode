package com.github.tianma8023.smscode.app.permissions;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tianma8023.smscode.R;

import java.util.List;

public class PermItemAdapter extends BaseQuickAdapter<PermItem, BaseViewHolder> {

    public PermItemAdapter(@Nullable List<PermItem> data) {
        super(R.layout.permission_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PermItem item) {
        helper.setText(R.id.tv_perm_title, item.getTitle())
                .setText(R.id.tv_perm_content, item.getContent());
    }
}
