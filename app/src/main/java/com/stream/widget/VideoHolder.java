package com.stream.widget;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoHolder extends RecyclerView.ViewHolder{

    public final LoadImageView thumb;
    public final TextView title;

    public VideoHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.title);
        thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
    }
}
