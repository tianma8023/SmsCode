package com.github.tianma8023.smscode.app.permissions;

import android.content.Context;
import android.content.res.Resources;

import com.github.tianma8023.smscode.R;

import java.util.ArrayList;
import java.util.List;

public class PermItemContainer {

    private List<PermItem> mItems = new ArrayList<>();

    public PermItemContainer(Context context) {
        loadItems(context);
    }

    private void loadItems(Context context) {
        Resources res = context.getResources();
        String[] titles = res.getStringArray(R.array.perm_title_list);
        String[] contents = res.getStringArray(R.array.perm_content_list);
        for(int i = 0; i < titles.length; i++) {
            mItems.add(new PermItem(titles[i], contents[i]));
        }
    }

    public List<PermItem> getItems() {
        return mItems;
    }

}
