package com.stream.hstream.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;
import com.hippo.yorozuya.IntIdGenerator;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.HsUrl;
import com.stream.client.data.VideoDetailInfo;
import com.stream.client.data.VideoInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.client.parser.VideoUrlParser;
import com.stream.dao.DetailInfo;
import com.stream.dao.Favorite;
import com.stream.dao.SourceInfo;
import com.stream.download.DownloadDetail;
import com.stream.download.DownloadService;
import com.stream.download.DownloadUtil;
import com.stream.download.SubtitleDownloader;
import com.stream.enums.VideoFormat;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.R;
import com.stream.hstream.Setting;
//import com.stream.util.HSAssetManager;
import com.stream.util.LoadImageHelper;
import com.stream.util.StreamUtils;
import com.stream.util.SubtitleFileMananger;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;
import com.stream.videoplayerlibrary.tv.VideoPlayer;
import com.stream.widget.DrawableSearchEditText;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.stream.util.SubtitleFileMananger.SubtitleFileInfo;
import com.stream.widget.NumberAjustBar;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoTvHolder extends RecyclerView.ViewHolder{

    private static final String TAG = VideoTvHolder.class.getSimpleName();

    private AlertDialog mSubtitleDialog;
    private DrawableSearchEditText mEditText;

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
    private int mCurrentSourceIndex = 0;
    private VideoInfo mVideoInfo;
    private VideoDetailInfo mVideoDetailInfo;

    private List<VideoSourceInfo> mData = new ArrayList<>();
    private SourceAdapter mAdapter;
    private Spinner mSpinner;
    private AppCompatTextView mLoadMessage;
    private AppCompatTextView mOfferingDate;
    private Button mMenuButton;

    private TuVideoPlayer mVideoPlayer;
    private Handler mHandler;

    public VideoTvHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;

        mClient = HStreamApplication.getHsClient(context);
        mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_video_player);

        mLoadMessage = (AppCompatTextView) itemView.findViewById(R.id.load_message);
        mOfferingDate = (AppCompatTextView) itemView.findViewById(R.id.offering_date);
        mSpinner = (Spinner) itemView.findViewById(R.id.source_spinner);
        mAdapter = new SourceAdapter(mContext);
        mSpinner.setAdapter(mAdapter);

        mMenuButton = (Button) itemView.findViewById(R.id.menu_button);

        mHandler = new Handler();
    }

    /**
     * registe listener
     */
    private void registeListener() {
        //set spinner listener
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
                mCurrentSourceIndex = pos;
                mSpinner.setSelection(pos, true);
                startVideo(mVideoInfo.token);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        //register favorite listener
        mVideoPlayer.setOnFavoriteListener(new TuVideoPlayer.OnFavoriteListener() {
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
                    favorite.setSource_url(mVideoInfo.url);
                    favorite.setVideo_url(url);
                    favorite.setTime(System.currentTimeMillis());

                    HStreamDB.putFavorite(favorite);
                } else {
                    HStreamDB.removeFavorite(mVideoInfo.token);
                }
            }
        });

        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuDialog();
            }
        });
    }

    /**
     * open menu dialog
     */
    private void openMenuDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_list_menu, null);

        TextView downloadTextView = (TextView) view.findViewById(R.id.download_button);
        TextView requireTextView = (TextView) view.findViewById(R.id.refresh_button);
        TextView subtitleView = (TextView) view.findViewById(R.id.subtitle_button);

        //build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BottomDialog);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        Window dialogWin = dialog.getWindow();
        dialogWin.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWin.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWin.setAttributes(lp);

        dialog.show();

        //set required button listener
        requireTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.hide();
                //force refresh video source data
                requiredSourceInfo(mVideoInfo, true);
            }
        });

        //set download listener
        downloadTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();

                if(mVideoInfo == null) {
                    showTip("Not find video info");
                    return;
                }

                VideoSourceInfo videoSourceInfo = mData.get(mCurrentSourceIndex);
                if(videoSourceInfo == null) {
                    showTip("Not find video source can download");
                    return;
                }

                if(TextUtils.isEmpty(videoSourceInfo.videoUrl)) {
                    showTip("Not find video url, please wait the video source load");
                    return;
                }

                if(!DownloadUtil.checkUrlFormat(videoSourceInfo.videoUrl)) {
                    showTip("Not support the video format to download, please change the video source");
                    return;
                }

                DownloadDetail detail = new DownloadDetail();
                detail.setToken(mVideoInfo.token);
                detail.setTitle(mVideoInfo.title);
                detail.setThumb(mVideoInfo.thumb);
                detail.setDownloadUrl(videoSourceInfo.videoUrl);
                detail.setAlternativeName(mVideoDetailInfo.getAlternativeName());
                mContext.startService(DownloadService.newIntent(mContext, detail));

                //download subtitle
                DetailInfo detailInfo = HStreamDB.queryDetailInfo(mVideoInfo.token);
                if(detailInfo != null && !TextUtils.isEmpty(detailInfo.getSubtitle_path())) {
                    SubtitleDownloader.instance().start(detailInfo.getSubtitle_path(), mVideoInfo.title);
                }
            }
        });

        //open dialog for search subtitle
        subtitleView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.hide();
                openSubtitleDialog();
            }
        });
    }

    /**
     * open subtitile search dialog
     */
    private void openSubtitleDialog() {
        if(mSubtitleDialog == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_subtitle_search, null);
            mEditText = (DrawableSearchEditText) view.findViewById(R.id.subtitle_edit_text);
            NumberAjustBar numberAjustBar = (NumberAjustBar) view.findViewById(R.id.number_ajust);
            ListView subtitleList = (ListView) view.findViewById(R.id.subtitle_search_list);

            //set default search text
            String nativeName = getAlternativeName();
            if(!TextUtils.isEmpty(nativeName)) {
                mEditText.setText(nativeName);
            }

            //set adapter and item click listener
            List<SubtitleFileInfo> dataList = SubtitleFileMananger.querySubtitle(nativeName);
            final ArrayAdapter<SubtitleFileInfo> adapter = new ArrayAdapter<SubtitleFileInfo>(mContext, R.layout.item_search_subtitle_list, dataList);
            subtitleList.setAdapter(adapter);

            //build dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(view);
            mSubtitleDialog = builder.create();

            //search
            mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(!TextUtils.isEmpty(mEditText.getText().toString())) {
                        List<SubtitleFileMananger.SubtitleFileInfo> subtitleList = SubtitleFileMananger.querySubtitle(mEditText.getText().toString());
                        adapter.clear();
                        adapter.addAll(subtitleList);
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                    return false;
                }
            });

            //set list item click lister
            subtitleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SubtitleFileInfo subtitle = adapter.getItem(position);
                    loadSubtitle(subtitle.getFilePath());
                    mSubtitleDialog.hide();
                }
            });

            numberAjustBar.setDistance(0.5f);
            numberAjustBar.setNumberAjustListener(new NumberAjustBar.NumberAjustListener() {
                @Override
                public void ajust(float newValue, float oldValue) {
                    ajustSubtitleTime(newValue);
                }
            });

        } else {
            String text = mEditText.getText().toString();

            //set default search text
            String nativeName = getAlternativeName();
            if(!TextUtils.isEmpty(nativeName) && !nativeName.contains(text)) {
                mEditText.setText(nativeName);
                mEditText.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
            }
        }

        mSubtitleDialog.show();
    }

    /**
     * load subtitle
     * @param subtitle
     */
    private void loadSubtitle(String subtitle) {
        if(mVideoPlayer.isPlaying()) {
            //mVideoPlayer.loadSubtitleFromAssets(SubtitleFileMananger.getSubtitleDir() + subtitle);
            mVideoPlayer.loadSubtitle(subtitle);
        }

        //save detail info for subtitle
        DetailInfo detailInfo = HStreamDB.queryDetailInfo(mVideoInfo.token);
        if(detailInfo != null && !TextUtils.isEmpty(detailInfo.getToken())) {
            detailInfo.setSubtitle_path(subtitle);
            HStreamDB.putDetailInfo(detailInfo);
        }
    }

    /**
     * ajust subtitle time
     * @param value
     */
    private void ajustSubtitleTime(float value) {
        mVideoPlayer.ajustSubtitleTime(value);
    }

    /**
     * show tip
     * @param tip
     */
    private void showTip(String tip) {
        Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
    }

    /**
     * init holder
     * @param videoInfo
     */
    public void init(VideoInfo videoInfo) {
        //prevent for change data
        mVideoInfo = new VideoInfo(videoInfo);

        //set thumb
        if(mImageHelper == null) {
            mImageHelper = LoadImageHelper.with(mContext);
        }
        mImageHelper.load(mVideoInfo.token, mVideoInfo.thumb)
                    .into(mVideoPlayer.getThumb());

        //set player
        mVideoPlayer.setUp(null, mVideoInfo.title, TuVideoPlayer.MODE_NORMAL_SCREEN);

        //registe listener
        registeListener();
    }

    /**
     * get alternative name
     * @return
     */
    private String getAlternativeName() {
        String title = mVideoPlayer.getTitle().toString();
        if(!TextUtils.isEmpty(title)) {
            String[] titleArr = title.split("\n");
            if(titleArr.length > 1) {
                return titleArr[1];
            }
        }

        return null;
    }

    /**
     * clear holder
     */
    public void clear() {
        //mVideoPlayer.release();
        mVideoPlayer.setThumb(null);
        mCurrentTaskId = -1;
        mVideoInfo = null;
        mVideoDetailInfo = null;

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
            mSpinner.setSelection(0);
        }

        if(mSubtitleDialog != null) {
            mSubtitleDialog.dismiss();
            mSubtitleDialog = null;
        }

        loadMessageShow(true, R.string.source_loading);
        mOfferingDate.setText("");
    }

    /**
     * load message show
     * @param isShow
     * @param stringResId
     */
    private void loadMessageShow(boolean isShow, int stringResId) {
        if(isShow) {
            mLoadMessage.setText(mContext.getResources().getString(stringResId));
            mLoadMessage.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
        } else {
            mLoadMessage.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
        }
    }

    /**
     * require source, default no refresh
     * @param videoInfo
     */
    public void requiredSourceInfo(VideoInfo videoInfo) {
        requiredSourceInfo(videoInfo, false);
    }

    /**
     * require source
     * @param videoInfo
     * @param forceRefresh true it will be get data from web
     */
    public void requiredSourceInfo(VideoInfo videoInfo, boolean forceRefresh) {
        //Log.d("VideoTvHolder", name + "----" + sourceUrl);
        long cm = System.currentTimeMillis();
        if(videoInfo != null && videoInfo.url != null) {
            loadMessageShow(true, R.string.source_loading);

            mCurrentTaskId = mIdGenerator.nextId();
            mSourceRequest = new SourceRequest(videoInfo.token, videoInfo.title, videoInfo.url, mCurrentTaskId, forceRefresh);
            mSourceRequest.start();
        }

        Log.d(TAG, "required time: " + (System.currentTimeMillis() - cm));
    }

    /**
     * start video player
     */
    private void startVideo(final String token) {
        long cm = System.currentTimeMillis();
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

            mVideoPlayer.setVideoPath(playUrl, headers);

            mVideoPlayer.setOnPreParedListener(new TuVideoPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(VideoPlayer videoPlayer) {
                    String title = mVideoPlayer.getTitle().toString();
                    if(!TextUtils.isEmpty(title)) {
                        String[] titleArr = title.split("\n");
                        File file = null;
                        for(String fileName: titleArr) {
                            Log.d(TAG, "file name : " + fileName + ".ass");
                            file = new File(Setting.getSubtitleDir() + fileName + ".ass");
                            if(file.exists()) {
                                break;
                            }
                        }

                        //default load from file dir
                        if(file != null && file.exists()) {
                            mVideoPlayer.loadSubtitle(file);
                        } else {
                            //other, load from assets
                            DetailInfo detailInfo = HStreamDB.queryDetailInfo(token);
                            String subtitle = detailInfo.getSubtitle_path();
                            if(!TextUtils.isEmpty(subtitle)) {
                                //mVideoPlayer.loadSubtitleFromAssets(HSAssetManager.getSubtitleDir() + subtitle);
                                mVideoPlayer.loadSubtitle(subtitle);
                            }
                        }
                    }
                }
            });
        }

        Log.d(TAG, "start video : " + (System.currentTimeMillis() - cm));
    }



    /**
     * source request success
     * @param info
     * @param result
     * @param taskId
     * @param token
     */
    private void onRequiredDetailSuccess(VideoDetailInfo info, List<VideoSourceInfo> result, int taskId, String token) {
        if(mCurrentTaskId == taskId) {

            if(info != null) {
                mVideoDetailInfo = info;
                mVideoPlayer.setTitle(mVideoInfo.title + "\n" + mVideoDetailInfo.getAlternativeName());
                mOfferingDate.setText(mVideoDetailInfo.getOfferingDate());
            }

            if(result != null && result.size() > 0) {
                mData.clear();
                mData.addAll(result);
                mAdapter.notifyDataSetChanged();

                loadMessageShow(false, 0);
            } else {
                mData.clear();
                loadMessageShow(true, R.string.source_no_found);
            }

            startVideo(token);
        }
    }

    private void onRequiredDetailFail(int taskId) {
        if(mCurrentTaskId == taskId) {
            loadMessageShow(true, R.string.source_no_found);
            mVideoPlayer.suspend();
        }
    }

    /**
     * source adapter
     */
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

    /**
     * source request
     */
    private class SourceRequest extends Thread{

        private AtomicInteger requestNum = new AtomicInteger();
        private int mTaskId = -1;

        private String mToken;
        private String mTitle;
        private String mUrl;
        private boolean mForceRefresh = false;

        private VideoDetailInfo detailInfo;
        private List<VideoSourceInfo> sourceInfoList = new ArrayList<>();
        private List<HsRequest> requestList = new ArrayList<>();

        private SourceRequest(String token, String title, String url, int taskId, boolean forceRefresh) {
            Assert.assertNotNull("token not null", token);
            mToken = token;
            mTitle = title;
            mUrl = url;
            mTaskId = taskId;
            mForceRefresh = forceRefresh;
        }

        @Override
        public void run() {
            requiredSourceDetail(mUrl);
        }

        //source detail
        private void requiredSourceDetail(final String sourceUrl) {
            if(!mForceRefresh) {
                List<SourceInfo> sourceList = HStreamDB.querySoruceInfoByToken(mToken, true);
                if(sourceList != null && sourceList.size() > 0) {
                    sourceInfoList.clear();
                    for(SourceInfo source: sourceList) {
                        VideoSourceInfo info = new VideoSourceInfo();
                        info.name = source.getSource_name();
                        info.videoUrl = source.getVideo_url();
                        sourceInfoList.add(info);
                    }

                    DetailInfo info = HStreamDB.queryDetailInfo(mToken);
                    if(info != null) {
                        detailInfo = new VideoDetailInfo();
                        detailInfo.setAlternativeName(info.getAlternative_name());
                        detailInfo.setOfferingDate(info.getOffering_date());
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

                        if(detailInfo != null) {
                            DetailInfo detail = new DetailInfo();
                            detail.setToken(mToken);
                            detail.setVideo_title(mTitle);
                            detail.setAlternative_name(detailInfo.getAlternativeName());
                            detail.setOffering_date(detailInfo.getOfferingDate());
                            //detail.setDetail_url(null); TO DO

                            HStreamDB.putDetailInfo(detail);
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

            //auto load
            if(detailInfo != null) {
                autoLoadSubtitle(mToken, detailInfo.getAlternativeName());
            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    //notify the success
                    onRequiredDetailSuccess(detailInfo, sourceInfoList, mTaskId, mToken);
                }
            });
        }

        private void notifyFail() {
            onRequiredDetailFail(mTaskId);
        }

        /**
         * auto load subtitle bind the video, but when it query only one and not exist in detail info
         * @param token
         * @param query
         */
        private void autoLoadSubtitle(String token, String query) {
            List<SubtitleFileInfo> subtitleList = SubtitleFileMananger.querySubtitle(query);
            if(subtitleList != null && subtitleList.size() == 1) {
                DetailInfo detailInfo = HStreamDB.queryDetailInfo(token);
                if(TextUtils.isEmpty(detailInfo.getSubtitle_path())) {
                    detailInfo.setSubtitle_path(subtitleList.get(0).getFilePath());
                    HStreamDB.putDetailInfo(detailInfo);
                }
            }
        }
    }
}
