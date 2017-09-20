package com.stream.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hippo.yorozuya.IntIdGenerator;
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
import java.util.Currency;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoTvHolder extends RecyclerView.ViewHolder{

    //public static List<HsRequest> sRequests = new CopyOnWriteArrayList<>();

    private HsClient mClient;
    private Context mContext;
    private HsRequest mRequest;

    /**
     * Unikey in ConacoTask is WeakRefrence, if not save LoadImageHelper's refrence,
     * when the LoadImageHelper is allocated, the Unikey will invalid;
     * So, save the refrence until VideoTvHolder allocated or recycled;
     */
    private LoadImageHelper mImageHelper;
    private IntIdGenerator mIdGenerator = new IntIdGenerator();
    private int mCurrentTaskId = -1;
    private String mSourceUrl = null;

    public TuVideoPlayer mVideoPlayer;
    public AppCompatImageView mDownloadButton;
    public AppCompatButton mRequireButton;
    public AppCompatTextView mMessageText;

    public VideoTvHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;

        mClient = HStreamApplication.getHsClient(context);
        mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_video_player);
        mDownloadButton = (AppCompatImageView) itemView.findViewById(R.id.download_button);
        mRequireButton = (AppCompatButton) itemView.findViewById(R.id.require_button);
        mMessageText = (AppCompatTextView) itemView.findViewById(R.id.message_text);

        mVideoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requiredSourceInfo(null, mSourceUrl);
            }
        });

        //this.setIsRecyclable(false);
    }

    public void clear() {
        mVideoPlayer.release();
        mVideoPlayer.setThumb(null);
        mMessageText.setText(null);
        mCurrentTaskId = -1;

        //clear image load task for out date
        if(mImageHelper != null) {
            mImageHelper.cancel();
        }

        //clear request
        if(mRequest != null) {
            mRequest.cancel();
            mRequest = null;
        }
    }

    public void setSourceUrl(String sourceUrl) {
        mSourceUrl = sourceUrl;
    }

    public void setThumb(Context context, String token, String thumb) {
        mImageHelper = LoadImageHelper.with(context)
                .load(token, thumb)
                .into(mVideoPlayer.getThumb());
    }

    public void requiredSourceInfo(String name, String sourceUrl) {
        //Log.d("VideoTvHolder", name + "----" + sourceUrl);
        if(sourceUrl != null) {
            int taskId = mIdGenerator.nextId();
            mCurrentTaskId = taskId;
            mRequest = new HsRequest();
            mRequest.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
            mRequest.setCallback(new VideoSourceListener(taskId));
            mRequest.setArgs(sourceUrl);
            mClient.execute(mRequest);
        }
    }

    private void onRequiredDetailSuccess(VideoSourceParser.Result result, int taskId) {
        if(mCurrentTaskId == taskId) {
            if(result.mVideoSourceInfoList != null && result.mVideoSourceInfoList.size() > 0) {
                VideoSourceInfo videoSourceInfo = result.mVideoSourceInfoList.get(0);
                if(videoSourceInfo != null && videoSourceInfo.videoUrl != null) {
                    //Log.d("VideoTvHolder", videoSourceInfo.name + "----" + videoSourceInfo.videoUrl);
                    mVideoPlayer.setVideoPath(videoSourceInfo.videoUrl);
                    return;
                }
            }

            //no video source
            //Toast.makeText(mContext, "no video source can be play", Toast.LENGTH_SHORT).show();
            mMessageText.setText(mContext.getResources().getString(R.string.gl_get_source_fail));
        }
    }

    private class VideoSourceListener implements HsClient.Callback<VideoSourceParser.Result> {

        private int mTaskId;

        public VideoSourceListener(int taskId) {
            mTaskId = taskId;
        }

        @Override
        public void onSuccess(VideoSourceParser.Result result) {
            onRequiredDetailSuccess(result, mTaskId);
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.gl_get_source_fail, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {

        }
    }
}
