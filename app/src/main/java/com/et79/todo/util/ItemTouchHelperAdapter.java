package com.et79.todo.util;

/**
 * Created by eisuke on 2016/11/22.
 */

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}

