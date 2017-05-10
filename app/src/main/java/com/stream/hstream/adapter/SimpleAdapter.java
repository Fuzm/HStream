package com.stream.hstream.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Fuzm on 2017/4/27 0027.
 */

public abstract class SimpleAdapter<E extends SimpleHolder> extends BaseAdapter {

    public boolean areAllItemsEnabled() {
        return true;
    }

    public boolean isEnabled(int position) {
        return true;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        E holder = null;
        if(convertView == null) {
            holder = onCreateViewHolder(parent);
            convertView = holder.getItemView();
            convertView.setTag(holder);
        } else {
            holder = (E) convertView.getTag();
        }

        onBindViewHolder(holder, position);
        return convertView;
    }

    public abstract E onCreateViewHolder(ViewGroup parent);

    public abstract void onBindViewHolder(E holder, int position);
}
