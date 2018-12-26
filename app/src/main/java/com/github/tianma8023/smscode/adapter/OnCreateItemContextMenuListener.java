package com.github.tianma8023.smscode.adapter;

import android.view.ContextMenu;
import android.view.View;

public interface OnCreateItemContextMenuListener {

    void onCreateItemContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, int position);

}
