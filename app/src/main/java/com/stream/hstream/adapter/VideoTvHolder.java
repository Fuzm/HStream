package com.stream.hstream.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import com.danikula.videocache.HttpProxyCacheServer;
import com.hippo.yorozuya.IntIdGenerator;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.HsUrl;
import com.stream.client.data.VideoDetailInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.client.parser.VideoUrlParser;
import com.stream.dao.SourceInfo;
import com.stream.enums.VideoFormat;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.R;
import com.stream.hstream.Setting;
import com.stream.util.LoadImageHelper;
import com.stream.util.StreamUtils;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoTvHolder extends RecyclerView.ViewHolder{

    private HsClient mClient;
    private Context mContext;
    private SourceRequest mSourceRequest;

    /**
     * Unikey in ConacoTask is WeakRefrence, if not save LoadImageHelper's refrence,
     * when the LoadImageHelper is allocated, the Unikey will invalid;
     * So, save the refrence until VideoTvHolder allocated or recycled;
     */
    private LoadImageHelper mImageHelper;
    private IntIdGenerator mIdGenerator = new IntIdGenerator();
    private int mCurrentTaskId = -1;
    //private String mSourceUrl = null;
    private int mCurrentSourceIndex = 0;

    private List<VideoSourceInfo> mData = new ArrayList<>();
    private SourceAdapter mAdapter;
    private Spinner mSpinner;
    private AppCompatTextView mLoadMessage;

    public TuVideoPlayer mVideoPlayer;
    public AppCompatImageView mDownloadButton;
    public AppCompatImageView mRequireButton;
    public AppCompatTextView mMessageText;

    public VideoTvHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;

        mClient = HStreamApplication.getHsClient(context);
        mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_video_player);
        mDownloadButton = (AppCompatImageView) itemView.findViewById(R.id.download_button);
        mRequireButton = (AppCompatImageView) itemView.findViewById(R.id.refresh_button);
        mMessageText = (AppCompatTextView) itemView.findViewById(R.id.message_text);

        mLoadMessage = (AppCompatTextView) itemView.findViewById(R.id.load_message);
        mSpinner = (Spinner) itemView.findViewById(R.id.source_spinner);
        mAdapter = new SourceAdapter(mContext);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
                mCurrentSourceIndex = pos;
                mSpinner.setSelection(pos, true);
                startVideo();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

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
        if(mSourceRequest != null) {
            mSourceRequest.cancel();
            mSourceRequest = null;
        }

        if(mData.size() != 0) {
            mData.clear();
            mAdapter.notifyDataSetChanged();
        }

        mLoadMessage.setText(mContext.getResources().getString(R.string.source_loading));
        mSpinner.setVisibility(View.GONE);
        mLoadMessage.setVisibility(View.VISIBLE);
    }

    public void setThumb(Context context, String token, String thumb) {
        mImageHelper = LoadImageHelper.with(context)
                .load(token, thumb)
                .into(mVideoPlayer.getThumb());
    }

    /**
     * require source, default no refresh
     * @param token
     * @param title
     * @param sourceUrl
     */
    public void requiredSourceInfo(String token, String title, String sourceUrl) {
        requiredSourceInfo(token, title, sourceUrl, false);
    }

    /**
     * require source
     * @param token
     * @param title
     * @param sourceUrl
     * @param forceRefresh true it will be get data from web
     */
    public void requiredSourceInfo(String token, String title, String sourceUrl, boolean forceRefresh) {
        //Log.d("VideoTvHolder", name + "----" + sourceUrl);
        if(sourceUrl != null) {
            mCurrentTaskId = mIdGenerator.nextId();
            mSourceRequest = new SourceRequest(token, title);
            mSourceRequest.start(sourceUrl, mCurrentTaskId, forceRefresh);
            mLoadMessage.setText(mContext.getResources().getString(R.string.source_loading));
        }
    }

    private void startVideo() {
        if(mData.size() > 0 && mCurrentSourceIndex < mData.size()) {
            VideoSourceInfo videoSourceInfo = mData.get(mCurrentSourceIndex);

            String playUrl = videoSourceInfo.videoUrl;
            if(!StreamUtils.checkVideoFormat(playUrl, VideoFormat.m3u8)) {
                HttpProxyCacheServer cacheServer = HStreamApplication.getHttpProxyCacheServer(mContext);
                playUrl = cacheServer.getProxyUrl(videoSourceInfo.videoUrl);
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("origin", HsUrl.getDomain());
            headers.put("referer", videoSourceInfo.url);

            mVideoPlayer.release();
            mVideoPlayer.setVideoPath(playUrl, headers);
        }
    }

    private void onRequiredDetailSuccess(VideoDetailInfo info, List<VideoSourceInfo> result, int taskId) {
        if(mCurrentTaskId == taskId) {

            if(info != null) {
                mVideoPlayer.setTitle(mVideoPlayer.getTitle() + "\n" + info.getAlternativeName());
            }

            if(result != null && result.size() > 0) {
                mData.clear();
                mData.addAll(result);
                mAdapter.notifyDataSetChanged();

                mSpinner.setVisibility(View.VISIBLE);
                mLoadMessage.setVisibility(View.GONE);
            } else {
                mData.clear();
                mLoadMessage.setText(mContext.getResources().getString(R.string.source_no_found));

                mSpinner.setVisibility(View.GONE);
                mLoadMessage.setVisibility(View.VISIBLE);

            }

            startVideo();
        }
    }

    private void onRequiredDetailFail(int taskId) {
        if(mCurrentTaskId == taskId) {
            mLoadMessage.setText(mContext.getResources().getString(R.string.source_no_found));
            mVideoPlayer.release();
        }
    }

    private class SourceAdapter extends BaseAdapter {

        private Context mContext;

        public SourceAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VideoSourceInfo info = (VideoSourceInfo) getItem(position);

            View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            CheckedTextView textView = (CheckedTextView) view.findViewById(android.R.id.text1);
            textView.setText(info.name);
            return view;
        }
    }

    private class SourceRequest {

        private AtomicInteger requestNum = new AtomicInteger();
        private int mTaskId = -1;

        private String mToken;
        private String mTitle;
        private boolean mForceRefresh = false;

        private VideoDetailInfo detailInfo;
        private List<VideoSourceInfo> sourceInfoList = new ArrayList<>();
        private List<HsRequest> requestList = new ArrayList<>();

        private SourceRequest(String token, String title) {
            Assert.assertNotNull("token not null", token);
            mToken = token;
            mTitle = title;
        }

        public void start(String url, int taskId) {
            start(url, taskId, false);
        }

        public void start(String url, int taskId, boolean forceRefresh) {
            Assert.assertNotNull("start url not null", url);
            mTaskId = taskId;
            mForceRefresh = forceRefresh;
            requiredSourceDetail(url);
        }

        //source detail
        private synchronized void requiredSourceDetail(final String sourceUrl) {
            if(!mForceRefresh) {
                List<SourceInfo> sourceList = HStreamDB.querySoruceInfoByToken(mToken, true);
                if(sourceList != null && sourceList.size() > 0) {
                    for(SourceInfo source: sourceList) {
                        VideoSourceInfo info = new VideoSourceInfo();
                        info.name = source.getSource_name();
                        info.videoUrl = source.getVideo_url();
                        sourceInfoList.add(info);
                    }

                    notifySuccess();
                    return;
                }
            }

            //request web for get data
            final HsRequest request = new HsRequest();
            request.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
            request.setArgs(sourceUrl);
            request.setCallback(new HsClient.Callback<VideoSourceParser.Result>() {

                @Override
                public void onSuccess(VideoSourceParser.Result result) {

                    switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
                        case Setting.WEB_STREAM:
                            //stream need video url by require
                            for(VideoSourceInfo info: result.mVideoSourceInfoList) {
                                if (info.url != null && !info.url.equals("")) {
                                    requireSourceInfo(info);
                                }
                            }
                            break;
                        case Setting.WEB_MUCHO:
                            if(result.mDetailInfo != null) {
                                detailInfo = result.mDetailInfo;
                            }

                            //mucho can find video url
                            for(VideoSourceInfo info: result.mVideoSourceInfoList) {
                                info.url = sourceUrl;
                                sourceInfoList.add(info);
                            }

                            break;
                        default:
                            ;
                    }

                    complete(request);
                }

                @Override
                public void onFailure(Exception e) {
                    complete(request);
                }

                @Override
                public void onCancel() {
                    complete(request);
                }

            });

            mClient.execute(request);
            requestList.add(request);
            requestNum.addAndGet(1);
        }

        //source url info
        public void requireSourceInfo(final VideoSourceInfo info) {
            if(info != null && info.url != null) {
                final HsRequest request = new HsRequest();
                request.setMethod(HsClient.METHOD_GET_VIDEO_URL);
                request.setArgs(info.url);
                request.setCallback(new HsClient.Callback<VideoUrlParser.Result>() {

                    @Override
                    public void onSuccess(VideoUrlParser.Result result) {
                        if(result.url != null && !"".equals(result.url.trim())) {
                            info.videoUrl = result.url;
                            sourceInfoList.add(info);
                        }
                        complete(request);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        complete(request);
                    }

                    @Override
                    public void onCancel() {
                        complete(request);
                    }

                });

                mClient.execute(request);
                requestList.add(request);
                requestNum.addAndGet(1);
            }
        }

        /**
         * check complete
         * @param request
         */
        private void complete(HsRequest request) {
            synchronized (requestList) {

                requestList.remove(request);
                requestNum.decrementAndGet();

                if(requestNum.get() == 0) {
                    if(sourceInfoList.size() == 0) {
                        notifyFail();
                    } else {
                        //clear old source info
                        HStreamDB.deleteByToken(mToken);
                        //cache to database
                        for(VideoSourceInfo info: sourceInfoList) {
                            SourceInfo source = new SourceInfo();
                            source.setToken(mToken);
                            source.setVideo_title(mTitle);
                            source.setSource_url(info.url);
                            source.setSource_name(info.name);
                            source.setVideo_url(info.videoUrl);
                            source.setUpd_time(System.currentTimeMillis());

                            HStreamDB.putSourceInfo(source);
                        }

                        notifySuccess();
                    }
                }
            }
        }

        public synchronized void cancel() {
            synchronized (requestList) {
                for (HsRequest request : requestList) {
                    request.cancel();
                }

                requestList.clear();
            }
        }

        private void notifySuccess() {
            //notify the success
            onRequiredDetailSuccess(detailInfo, sourceInfoList, mTaskId);
        }

        private void notifyFail() {
            onRequiredDetailFail(mTaskId);
        }
    }

}
