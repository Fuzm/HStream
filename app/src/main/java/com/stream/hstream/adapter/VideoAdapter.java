package com.stream.hstream.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stream.client.data.VideoInfo;
import com.stream.dao.Favorite;
import com.stream.download.DownloadService;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.R;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public abstract class VideoAdapter extends RecyclerView.Adapter<VideoTvHolder> {

    private final LayoutInflater mInflater;
    private final Resources mResources;
    private final RecyclerView mRecyclerView;
    private final RecyclerView.LayoutManager mLayoutManager;

    private final Context mContext;

    public VideoAdapter(@NonNull LayoutInflater inflater, @NonNull Resources resources,
                            @NonNull RecyclerView recyclerView, Context context) {
        mContext = context;
        mInflater = inflater;
        mResources = resources;
        mRecyclerView = recyclerView;
        mLayoutManager = new LinearLayoutManager(context);

        mRecyclerView.setAdapter(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public VideoTvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoTvHolder(mContext, mInflater.inflate(R.layout.item_video_list, parent, false));
    }

       @Override
    public void onBindViewHolder(final VideoTvHolder holder, final int position) {
        long cm = System.currentTimeMillis();
        final VideoInfo videoInfo = getDataAt(position);

        //clear holder old info, because it will recycle
        holder.clear();
        holder.init(videoInfo);
        holder.requiredSourceInfo(videoInfo);
        Log.d("VideoAdapter", "set holder: " + (System.currentTimeMillis() - cm));
    }

    public abstract VideoInfo getDataAt(int position);
}
