package com.stream.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.yorozuya.collect.CollectionUtils;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.data.StreamDataBase;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;
import com.stream.hstream.Setting;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoHolder extends RecyclerView.ViewHolder{

    private static List<VideoHolder> registerHodler = new ArrayList<>();

    private View mRoot;
    private LoadImageView mThumb;
    private VideoTextureView mVideoView;
    private MediaControl mMediaControl;

    private final Context mContext;
    private HsClient mClient;

    private String mSourceUrl;
    private String videoUrl;
    private boolean isPrepared = false;
    private boolean mFail = false;
    private boolean isRunning = false;

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mMediaControl.hideWaitBar();
            mThumb.setVisibility(View.GONE);

            mRoot.setOnClickListener(null);
            mMediaControl.setOnPlayButtonClick(null);
            isRunning = false;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isRunning) {
                isRunning = true;
                mMediaControl.showWaitBar();
                if(mFail) {
                    mFail = false;
                    requiredSourceInfo(mSourceUrl);
                } else {
                    prepare();
                }
                setIsRecyclable(false);
            }
        }
    };

    public VideoHolder(Context context, View itemView) {
        super(itemView);

        mRoot = itemView;
        mContext = context;
        mClient = HStreamApplication.getHsClient(context);

        mThumb = (LoadImageView) itemView.findViewById(R.id.list_thumb);
        mMediaControl = (MediaControl) itemView.findViewById(R.id.media_control);
        mVideoView = (VideoTextureView) itemView.findViewById(R.id.list_video_view);
        mVideoView.setMediaController(mMediaControl);

        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mMediaControl.setOnPlayButtonClick(mOnClickListener);
        mRoot.setOnClickListener(mOnClickListener);
    }

    public void setTitle(String text) {
        mMediaControl.setTitle(text);
    }

    public void setThumb(String token, String thumb) {
        mThumb.load(token, thumb);
    }

    private void releaseFirst() {
        if(registerHodler.size() >= Setting.MAX_LIST_VIEO_PLAY_NUM) {
            VideoHolder holder = registerHodler.get(0);
            holder.reset();
            registerHodler.remove(0);
        }
        registerHodler.add(this);
    }

    private void reset() {
        mVideoView.suspend();
    }

    public void requiredSourceInfo(String sourceUrl) {
        if(sourceUrl != null) {
            mSourceUrl = sourceUrl;
            HsRequest request = new HsRequest();
            request.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
            request.setCallback(new VideoSourceListener());
            request.setArgs(sourceUrl);
            mClient.execute(request);
        }
    }

    private void startHolderVideo() {
        if(videoUrl != null) {
            mVideoView.setVideoPath(videoUrl);
            mVideoView.start();
        } else {
            Toast.makeText(mContext, R.string.VideoView_error_text_unknown, Toast.LENGTH_SHORT).show();
        }
    }

    private void prepare() {
        if(isPrepared) {
            startHolderVideo();
        } else {
            isPrepared = true;
        }
    }

    private void resetPrepare() {
        mMediaControl.setOnPlayButtonClick(mOnClickListener);
        mRoot.setOnClickListener(mOnClickListener);
        mMediaControl.hideWaitBar();
    }

    private void onRequiredDetailSuccess(VideoSourceParser.Result result) {
        VideoSourceInfo videoSourceInfo = result.mVideoSourceInfoList.get(0);
        if(videoSourceInfo != null) {
            videoUrl = videoSourceInfo.videoUrl;
            prepare();
        }
    }

    private class VideoSourceListener implements HsClient.Callback<VideoSourceParser.Result> {

        @Override
        public void onSuccess(VideoSourceParser.Result result) {
            onRequiredDetailSuccess(result);
        }

        @Override
        public void onFailure(Exception e) {
            resetPrepare();
            mFail = true;
            isRunning = false;
            //Toast.makeText(mContext, R.string.gl_get_source_fail, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            resetPrepare();
            mFail = true;
            isRunning = false;
        }
    }
}
