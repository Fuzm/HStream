package com.stream.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;
import com.stream.hstream.Setting;
import com.stream.util.LoadImageHelper;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoTvHolder extends RecyclerView.ViewHolder{

    private HsClient mClient;
    /**
     * Unikey in ConacoTask is WeakRefrence, if not save LoadImageHelper's refrence,
     * when the LoadImageHelper is allocated, the Unikey will invalid;
     * So, save the refrence until VideoTvHolder allocated or recycled;
     */
    private LoadImageHelper mImageHelper;

    public TuVideoPlayer mVideoPlayer;
    public AppCompatImageView mDownloadButton;

    public VideoTvHolder(Context context, View itemView) {
        super(itemView);

        mClient = HStreamApplication.getHsClient(context);
        mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_video_player);
        mDownloadButton = (AppCompatImageView) itemView.findViewById(R.id.download_button);
    }

    public void setThumb(Context context, String token, String thumb) {
        mImageHelper = LoadImageHelper.with(context)
                .load(token, thumb)
                .into(mVideoPlayer.getThumb());
    }

    public void requiredSourceInfo(String sourceUrl) {
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
        if(videoSourceInfo != null && videoSourceInfo.videoUrl != null) {
            mVideoPlayer.setVideoPath(videoSourceInfo.videoUrl);
        } else {
        }
    }

    private class VideoSourceListener implements HsClient.Callback<VideoSourceParser.Result> {

        @Override
        public void onSuccess(VideoSourceParser.Result result) {
            onRequiredDetailSuccess(result);
        }

        @Override
        public void onFailure(Exception e) {
            //Toast.makeText(mContext, R.string.gl_get_source_fail, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
        }
    }
}
