package com.stream.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hippo.yorozuya.collect.CollectionUtils;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.data.StreamDataBase;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;
import com.stream.hstream.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoHolder extends RecyclerView.ViewHolder{

    private static List<VideoHolder> registerHodler = new ArrayList<>();

    public final TextView title;
    public final LoadImageView thumb;
    private final FrameLayout mViewContainer;
    private StreamVideoView mVideoView;

    private final Context mContext;
    private HsClient mClient;
    private String sourceUrl;


    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addVideoView();
            mVideoView.showWaitBar();
            requiredSourceInfo();
        }
    };

    public VideoHolder(Context context, View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.title);
        mViewContainer = (FrameLayout) itemView.findViewById(R.id.view_container);
        thumb = (LoadImageView) mViewContainer.findViewById(R.id.thumb);

        itemView.setOnClickListener(mOnClickListener);
        mContext = context;
        mClient = HStreamApplication.getHsClient(context);
    }

    private void addVideoView() {
        StreamVideoView videoView = null;
        // check play num at the same time; when limit up the Setting, remove the first;
        if(registerHodler.size() >= Setting.MAX_LIST_VIEO_PLAY_NUM) {
            VideoHolder holder = registerHodler.get(0);
            //recycle video view;
            videoView = holder.getVideoView();
            //remove the video view;
            holder.removeVideoView();
            //set recyclable true;
            holder.setIsRecyclable(true);
            //remove from registerHolder;
            registerHodler.remove(0);
        }

        if(null == mVideoView) {
            mVideoView = new StreamVideoView(mContext);
            mVideoView.setMode(StreamVideoView.VIEW_MODE_SMALL);
            //mVideoView.setZOrderOnTop(true);
        } else {
            videoView.reset();
            mVideoView = videoView;
        }

        mVideoView.setTitle(title.getText().toString());
        mVideoView.loadBackground(thumb.getDrawable());

        mViewContainer.addView(mVideoView);
        registerHodler.add(this);
        this.setIsRecyclable(false);
    }

    private void removeVideoView() {
        mViewContainer.removeView(mVideoView);
        //mVideoView.release();
    }

    private StreamVideoView getVideoView() {
        return mVideoView;
    }

    private void requiredSourceInfo() {
        if(sourceUrl != null) {
            HsRequest request = new HsRequest();
            request.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
            request.setCallback(new VideoSourceListener());
            request.setArgs(sourceUrl);
            mClient.execute(request);
        }
    }

    private void onRequiredDetailSuccess(VideoSourceParser.Result result) {
        VideoSourceInfo videoSourceInfo = result.mVideoSourceInfoList.get(0);
        if(videoSourceInfo != null) {
            //addVideoView(videoSourceInfo.videoUrl);
            mVideoView.setVideoPath(videoSourceInfo.videoUrl);
            mVideoView.start();
        }
    }

    private class VideoSourceListener implements HsClient.Callback<VideoSourceParser.Result> {

        @Override
        public void onSuccess(VideoSourceParser.Result result) {
            onRequiredDetailSuccess(result);
        }

        @Override
        public void onFailure(Exception e) {
            mVideoView.hideWaitBar();
        }

        @Override
        public void onCancel() {
            mVideoView.hideWaitBar();
        }
    }
}
