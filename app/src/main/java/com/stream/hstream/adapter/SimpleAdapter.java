package com.stream.hstream.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.stream.hstream.R;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;
import com.stream.widget.VideoTvHolder;

import java.util.List;

/**
 * Created by Fuzm on 2017/4/27 0027.
 */

public abstract class SimpleAdapter<E extends SimpleHolder> extends BaseAdapter {

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
