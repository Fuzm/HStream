package com.stream.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stream.client.HsCacheKeyFactory;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.VideoInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.HsCallback;
import com.stream.hstream.R;
import com.stream.hstream.VideoListFragment;
import com.stream.hstream.VideoPlayActivity;
import com.stream.util.LoadImageHelper;
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
        //return new VideoHolder(mContext, mInflater.inflate(R.layout.item_video_grid, parent, false));
        return new VideoTvHolder(mContext, mInflater.inflate(R.layout.item_video_list, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoTvHolder holder, int position) {
        VideoInfo videoInfo = getDataAt(position);

        holder.mVideoPlayer.setTitle(videoInfo.title);
        LoadImageHelper.with(mContext)
                .load(videoInfo.token, videoInfo.thumb)
                .into(holder.mVideoPlayer.getThumb());
        holder.requiredSourceInfo(videoInfo.url);
    }

    public abstract VideoInfo getDataAt(int position);
}
