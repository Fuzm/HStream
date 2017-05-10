package com.stream.download;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.SimpleHandler;
import com.hippo.yorozuya.collect.SparseJLArray;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.VideoInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.dao.DownloadInfo;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.HStreamDB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Fuzm on 2017/5/3 0003.
 */

public class DownloadManager implements DownLoader.OnDownLoaderListener{

    private static final String TAG = DownloadManager.class.getSimpleName();
    private static final int MAX_POOL_SIZE = 5;

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_WAIT = 1;
    public static final int STATE_DOWNLOAD = 2;
    public static final int STATE_FINISH = 3;
    public static final int STATE_FAILED = 4;

    private Context mContext;
    private OkHttpClient mHttpClient;
    private HsClient mClient;
    private DownloadListener mDownloadListener;
    private DownloadInfoListener mDownloadInfoListener;

    private final SparseJLArray<DownLoader> mDownLoaderMap = new SparseJLArray();
    private final SparseJLArray<DownloadInfo> mDownloadInfoMap = new SparseJLArray();
    private final SparseJLArray<HsRequest> mRequestMap = new SparseJLArray<>();

    private ThreadPoolExecutor mPoolExecutor;
    private SpeedReminder mSpeedReminder;

    public DownloadManager(Context context) {
        mContext = context;
        mHttpClient = HStreamApplication.getOkHttpClient(mContext);
        mClient = HStreamApplication.getHsClient(mContext);

        mPoolExecutor = new ThreadPoolExecutor(MAX_POOL_SIZE, MAX_POOL_SIZE,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        List<DownloadInfo> list = HStreamDB.queryAllDownloadInfo();
        Log.d(TAG, "load download info :" + list.size());
        for(DownloadInfo info: list) {
            info.setState(STATE_NONE);

            DownLoader downLoader = DownLoader.createLoader(mHttpClient, info.getTitle(), info.getUrl());
            downLoader.setOnDownLoaderListener(this);

            mDownloadInfoMap.put(downLoader.getLoaderId(), info);
            mDownLoaderMap.put(downLoader.getLoaderId(), downLoader);
        }

        mSpeedReminder = new SpeedReminder();
    }

    public List<DownloadInfo> getDownloadInfoList() {
        List<DownloadInfo> list = new ArrayList<>();
        for(int i = 0; i < mDownloadInfoMap.size(); i++) {
            list.add(mDownloadInfoMap.get(mDownLoaderMap.keyAt(i)));
        }
        return list;
    }

    private DownLoader getDownloaderByToken(String token) {
        DownLoader loader = null;
        for(int i = 0; i < mDownloadInfoMap.size(); i++) {
            long loaderId = mDownLoaderMap.keyAt(i);
            DownloadInfo info = mDownloadInfoMap.get(loaderId);
            if(token.equals(info.getToken())) {
                loader = mDownLoaderMap.get(loaderId);
                break;
            }
        }

        return loader;
    }

    public void startDownload(VideoInfo videoInfo) {
        if(videoInfo.url != null) {
            DownloadInfo info = null;
            DownLoader downLoader = getDownloaderByToken(videoInfo.token);
            if(downLoader != null) {
                info = mDownloadInfoMap.get(downLoader.getLoaderId());

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(info);
                }

            } else {
                info = new DownloadInfo();
                info.setToken(videoInfo.token);
                info.setTitle(videoInfo.title);
                info.setThumb(videoInfo.thumb);
                info.setSourceUrl(videoInfo.url);
                //info.setUrl(url);
                info.setTime(System.currentTimeMillis());
                info.setState(STATE_DOWNLOAD);

                HStreamDB.putDownloadInfo(info);

                downLoader = DownLoader.createLoader(mHttpClient, info.getTitle());
                downLoader.setOnDownLoaderListener(this);

                mDownloadInfoMap.put(downLoader.getLoaderId(), info);
                mDownLoaderMap.put(downLoader.getLoaderId(), downLoader);
            }

            if(info.getUrl() != null && info.getUrl().length() > 0) {
                start(downLoader.getLoaderId());
            } else {
                requireDonwloadSource(downLoader.getLoaderId(), info.getSourceUrl());
            }

            if(mDownloadInfoListener != null) {
                mDownloadInfoListener.onAdd(info);
            }

            if(mDownloadListener != null) {
                mDownloadListener.onStart();
            }
        }
    }

    private void requireDonwloadSource(int loaderId, String url) {
        Log.d(TAG, "require download source: " + url);
        HsRequest request = new HsRequest();
        request.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
        request.setCallback(new VideoSourceListener(loaderId));
        request.setArgs(url);
        mClient.execute(request);

        mRequestMap.put(loaderId, request);
    }

    public void startRangeDownload(String[] tokenList) {
        for(String token: tokenList) {
            DownLoader loader = getDownloaderByToken(token);
            if(loader != null) {
                DownloadInfo info = mDownloadInfoMap.get(loader.getLoaderId());
                info.setState(STATE_DOWNLOAD);
                HStreamDB.putDownloadInfo(info);

                if(info.getUrl() != null && info.getUrl().length()>0) {
                    start(loader.getLoaderId());
                } else {
                    requireDonwloadSource(loader.getLoaderId(), info.getSourceUrl());
                }

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(info);
                }
            }
        }
    }

    public void startDownloadAll() {
        for(int i=0; i<mDownLoaderMap.size(); i++) {
            int loaderId = (int) mDownLoaderMap.keyAt(i);

            DownloadInfo info = mDownloadInfoMap.get(loaderId);
            info.setState(STATE_DOWNLOAD);
            HStreamDB.putDownloadInfo(info);

            if(info.getUrl() != null && info.getUrl().length()>0) {
                start(loaderId);
            } else {
                requireDonwloadSource(loaderId, info.getSourceUrl());
            }

            if(mDownloadInfoListener != null) {
                mDownloadInfoListener.onUpdate(info);
            }
        }
    }

    private void start(int loaderId) {
        DownLoader downLoader = mDownLoaderMap.get(loaderId);
        if(downLoader != null) {
            DownloadInfo info = mDownloadInfoMap.get(loaderId);

            //only state is STATE_DOWNLOAD, it can be execute;
            if(info.getState() == STATE_DOWNLOAD) {
                downLoader.setOnDownLoaderListener(this);
                mPoolExecutor.execute(downLoader);

                mSpeedReminder.start();
            }
        } else {
            Log.d(TAG, "not found the download info");
        }
    }

    public void stopDownload(DownloadInfo info){
        DownLoader loader = getDownloaderByToken(info.getToken());
        if(loader != null) {
            loader.stop();

            boolean ccAll = mSpeedReminder.onDone(loader.getLoaderId());
            if(ccAll) {
                mSpeedReminder.stop();
            }

            DownloadInfo cancelInfo = mDownloadInfoMap.get(loader.getLoaderId());
            cancelInfo.setState(STATE_NONE);

            HStreamDB.putDownloadInfo(cancelInfo);

            if(mDownloadInfoListener != null) {
                mDownloadInfoListener.onCancel(cancelInfo);
            }

        } else {
            Log.d(TAG, "can not find download task thread");
        }
    }

    public void stopAllDownload() {
        mPoolExecutor.shutdownNow();
        //close speed remainder;
        mSpeedReminder.onFinish();
        mSpeedReminder.stop();

        if(mDownloadInfoListener != null) {
            mDownloadInfoListener.onUpdateAll();
        }
    }

    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    public void setDownloadInfoListener(DownloadInfoListener listener) {
        mDownloadInfoListener = listener;
    }

    @Override
    public void onDownload(int loaderId, long contentLength, long finished, int bytesRead) {
        NotifyTask task = new NotifyTask();
        task.setOnDownloadData(loaderId, contentLength, finished, bytesRead);
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onFinish(int loaderId) {
        NotifyTask task = new NotifyTask();
        task.setOnFinishData(loaderId);
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onFailure(int loaderId, String msg) {
        NotifyTask task = new NotifyTask();
        task.setOnFailureData(loaderId);
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onGet410(int loaderId) {
        NotifyTask task = new NotifyTask();
        task.setOnGet410Data(loaderId);
        SimpleHandler.getInstance().post(task);
    }

    private class VideoSourceListener implements HsClient.Callback<VideoSourceParser.Result> {

        private int mLoaderId;

        public VideoSourceListener(int loaderId) {
            mLoaderId = loaderId;
        }

        @Override
        public void onSuccess(VideoSourceParser.Result result) {
            Log.d(TAG, "current thread: " + Thread.currentThread().getName());

            VideoSourceInfo videoSourceInfo = result.mVideoSourceInfoList.get(0);
            if(videoSourceInfo != null && videoSourceInfo.videoUrl != null) {
                Log.d(TAG, "get video url: " + videoSourceInfo.videoUrl);
                DownloadInfo info = mDownloadInfoMap.get(mLoaderId);
                info.setUrl(videoSourceInfo.videoUrl);
                HStreamDB.putDownloadInfo(info);

                //update downloader url, when the old url invalid;
                DownLoader downLoader = mDownLoaderMap.get(mLoaderId);
                downLoader.setUrl(videoSourceInfo.videoUrl);

                start(mLoaderId);
            } else {
                DownloadInfo info = mDownloadInfoMap.get(mLoaderId);
                info.setState(STATE_FAILED);
                HStreamDB.putDownloadInfo(info);

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(info);
                }
            }
        }

        @Override
        public void onFailure(Exception e) {
            Log.d(TAG, "get video url error, " + e.getMessage());
            //Toast.makeText(mContext, R.string.gl_get_source_fail, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "request already canceled");
        }
    }

    protected class NotifyTask implements Runnable {

        private static final int TYPE_ON_DOWNLOAD_DATA = 1;
        private static final int TYPE_ON_FINISH_DATA = 2;
        private static final int TYPE_ON_FAILURE_DATA = 3;
        private static final int TYPE_ON_GET_410_DATA = 4;

        private int mLoaderId;
        private int mType;
        private long mContentLength;
        private long mFinished;
        private int mBytesRead;

        public void setOnDownloadData(int loaderId, long contentLength, long finished, int bytesRead) {
            mType = TYPE_ON_DOWNLOAD_DATA;
            mLoaderId = loaderId;
            mContentLength = contentLength;
            mFinished = finished;
            mBytesRead = bytesRead;
        }

        public void setOnFinishData(int loaderId) {
            mType = TYPE_ON_FINISH_DATA;
            mLoaderId = loaderId;
        }

        public void setOnFailureData(int loaderId) {
            mType = TYPE_ON_FAILURE_DATA;
            mLoaderId = loaderId;
        }

        public void setOnGet410Data(int loaderId) {
            mType = TYPE_ON_GET_410_DATA;
            mLoaderId = loaderId;
        }


        @Override
        public void run() {
            switch (mType) {
                case TYPE_ON_DOWNLOAD_DATA:
                    mSpeedReminder.onDownload(mLoaderId, mContentLength, mFinished, mBytesRead);
                    return;

                case TYPE_ON_FINISH_DATA:
                    boolean isAll = mSpeedReminder.onDone(mLoaderId);
                    if(isAll) {
                        mSpeedReminder.stop();
                    }

                    DownloadInfo finishInfo = mDownloadInfoMap.get(mLoaderId);
                    finishInfo.setState(STATE_FINISH);

                    HStreamDB.putDownloadInfo(finishInfo);
                    break;

                case TYPE_ON_FAILURE_DATA:
                    boolean checkAll = mSpeedReminder.onDone(mLoaderId);
                    if(checkAll) {
                        mSpeedReminder.stop();
                    }

                    DownloadInfo failInfo = mDownloadInfoMap.get(mLoaderId);
                    failInfo.setState(STATE_FAILED);
                    HStreamDB.putDownloadInfo(failInfo);

                    if(mDownloadInfoListener != null) {
                        mDownloadInfoListener.onUpdate(failInfo);
                    }
                    break;

                case TYPE_ON_GET_410_DATA :
                    mSpeedReminder.onDone(mLoaderId);

                    DownloadInfo fourInfo = mDownloadInfoMap.get(mLoaderId);
                    requireDonwloadSource(mLoaderId, fourInfo.getSourceUrl());
                    break;
            }

        }
    }

    protected class SpeedReminder implements Runnable {

        private boolean mStop = true;

        private long mOldSpeed;
        private long mBytesRead;
        private final SparseArray<SpeedInfo> mSpeedMap = new SparseArray();

        public void start() {
            if (mStop) {
                mStop = false;
                SimpleHandler.getInstance().post(this);
            }
        }

        public void stop() {
            if (!mStop) {
                mStop = true;
                mOldSpeed = -1;
                mBytesRead = 0;
                mSpeedMap.clear();
                SimpleHandler.getInstance().removeCallbacks(this);
            }
        }

        public void onDownload(int loaderId, long contentLength, long finished, int bytesRead) {
            SpeedInfo info = mSpeedMap.get(loaderId, null);
            if(info == null) {
                info = new SpeedInfo();
                info.contentLength = contentLength;
                info.finished = finished;
                info.bytesRead = bytesRead;
                info.speed = -1;
                info.oldSpeed = -1;
            } else {
                info.contentLength = contentLength;
                info.finished = finished;
                info.bytesRead += bytesRead;
            }

            mSpeedMap.put(loaderId, info);
            mBytesRead += bytesRead;
        }

        public boolean onDone(int loaderId) {
            mSpeedMap.delete(loaderId);

            if(mSpeedMap.size() == 0){
                return true;
            } else {
                return false;
            }
        }

        public void onFinish() {
            mSpeedMap.clear();
        }

        @Override
        public void run() {
            int total = 0;
            int totalFinished = 0;
            List<DownloadInfo> list = new ArrayList<>();
            for(int i=0; i<mSpeedMap.size(); i++) {
                int loaderId = mSpeedMap.keyAt(i);
                SpeedInfo speedInfo = mSpeedMap.get(loaderId);
                long bytesRead = speedInfo.bytesRead;
                long oldSpeed = speedInfo.oldSpeed;

                long newSpeed = bytesRead / 2;
                if (oldSpeed != -1) {
                    newSpeed = (long) MathUtils.lerp(oldSpeed, newSpeed, 0.75f);
                }

                //next time use
                speedInfo.oldSpeed = newSpeed;
                speedInfo.bytesRead = 0;

                DownloadInfo info = mDownloadInfoMap.get(loaderId);
                if(info != null) {
                    info.setSpeed(speedInfo.oldSpeed);
                    info.setTotal(speedInfo.contentLength);
                    info.setFinished(speedInfo.finished);
                    list.add(info);
                }

                total += speedInfo.contentLength;
                totalFinished += speedInfo.finished;
            }

            if(mDownloadInfoListener != null && list.size() > 0) {
                mDownloadInfoListener.onUpdateAll();
            }

            if(mDownloadListener != null && list.size() > 0) {
                long newSpeed = mBytesRead / 2;
                if (mOldSpeed != -1) {
                    newSpeed = (long) MathUtils.lerp(mOldSpeed, newSpeed, 0.75f);
                }

                mOldSpeed = newSpeed;
                mBytesRead = 0;

                mDownloadListener.onDownload(newSpeed, total, totalFinished);
            }

            if (!mStop) {
                SimpleHandler.getInstance().postDelayed(this, 2000);
            }
        }

        private class SpeedInfo {
            public long contentLength;
            public long finished;
            public long bytesRead;
            public long speed;
            public long oldSpeed;
        }

    }

    public interface DownloadInfoListener {

        void onAdd(DownloadInfo info);

        void onUpdate(DownloadInfo info);

        void onUpdateAll();

        void onCancel(DownloadInfo info);
    }

    public interface DownloadListener {

        /**
         * Start download
         */
        void onStart();

        /**
         * Update download speed
         */
        void onDownload(long speed, long total, long finished);

        /**
         * Download done
         */
        void onFinish();

        /**
         * Download done
         */
        void onCancel();
    }

}
