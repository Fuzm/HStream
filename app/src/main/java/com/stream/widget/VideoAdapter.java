package com.stream.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stream.client.data.VideoInfo;
import com.stream.dao.Favorite;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.R;
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
        return new VideoTvHolder(mContext, mInflater.inflate(R.layout.item_video_list, parent, false));
    }

       @Override
    public void onBindViewHolder(final VideoTvHolder holder, final int position) {
        final VideoInfo videoInfo = getDataAt(position);

        //clear holder old info, because it will recycle
        holder.clear();

        holder.mVideoPlayer.setUp(null, videoInfo.title, TuVideoPlayer.MODE_NORMAL_SCREEN);
        holder.mVideoPlayer.setOnFavoriteListener(new FavoriteVideoListener(videoInfo));
        holder.setThumb(mContext, videoInfo.token, videoInfo.thumb);
        holder.setSourceUrl(videoInfo.url);
        holder.requiredSourceInfo(videoInfo.title, videoInfo.url);

        holder.mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, position);
            }
        });

        holder.mRequireButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.requiredSourceInfo(videoInfo.title, videoInfo.url);
            }
        });
    }

    public abstract VideoInfo getDataAt(int position);

    public abstract void onItemClick(View view, int position);

    public class FavoriteVideoListener implements TuVideoPlayer.OnFavoriteListener {

        private VideoInfo mVideoInfo;

        public FavoriteVideoListener(VideoInfo videoInfo) {
            mVideoInfo = videoInfo;
        }

        @Override
        public boolean isFavorited() {
            return HStreamDB.existeFavorite(mVideoInfo.token);
        }

        @Override
        public void onFavorite(String url) {
            if(!HStreamDB.existeFavorite(mVideoInfo.token)) {
                Favorite favorite = new Favorite();
                favorite.setToken(mVideoInfo.token);
                favorite.setTitle(mVideoInfo.title);
                favorite.setThumb(mVideoInfo.thumb);
                favorite.setSourceUrl(mVideoInfo.url);
                favorite.setVideoUrl(url);
                favorite.setTime(System.currentTimeMillis());

                HStreamDB.putFavorite(favorite);
            } else {
                HStreamDB.removeFavorite(mVideoInfo.token);
            }
        }
    }
}
