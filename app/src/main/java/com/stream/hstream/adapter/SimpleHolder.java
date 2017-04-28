package com.stream.hstream.adapter;

import android.view.View;

/**
 * Created by Fuzm on 2017/4/27 0027.
 */

public class SimpleHolder {

    private final View itemView;

    public SimpleHolder(View itemView) {
        if (itemView == null) {
            throw new IllegalArgumentException("itemView may not be null");
        }
        this.itemView = itemView;
    }

    protected View getItemView() {
        return itemView;
    }
}
