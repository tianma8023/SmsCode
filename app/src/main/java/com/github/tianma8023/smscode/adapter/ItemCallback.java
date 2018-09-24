package com.github.tianma8023.smscode.adapter;

import android.view.ContextMenu;
import android.view.View;

public interface ItemCallback<E> {

    void onItemClicked(E item, int position);

    boolean onItemLongClicked(E item, int position);

    void onCreateItemContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, E item, int position);
}
