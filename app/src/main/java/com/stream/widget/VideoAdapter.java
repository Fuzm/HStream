package com.stream.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.stream.client.HsCacheKeyFactory;
import com.stream.client.data.VideoInfo;
import com.stream.hstream.R;
import com.stream.hstream.VideoListFragment;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public abstract class VideoAdapter extends RecyclerView.Adapter<VideoHolder>{

    private final LayoutInflater mInflater;
    private final Resources mResources;
    private final RecyclerView mRecyclerView;
    private final RecyclerView.LayoutManager mLayoutManager;

    public VideoAdapter(@NonNull LayoutInflater inflater, @NonNull Resources resources,
                            @NonNull RecyclerView recyclerView, Context context) {
        mInflater = inflater;
        mResources = resources;
        mRecyclerView = recyclerView;
        mLayoutManager = new LinearLayoutManager(context);

        mRecyclerView.setAdapter(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoHolder(mInflater.inflate(R.layout.item_video_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {
        VideoInfo videoInfo = getDataAt(position);

        holder.title.setText(videoInfo.title);
        holder.thumb.load(videoInfo.token, videoInfo.thumb);
    }

    public abstract VideoInfo getDataAt(int position);
}
