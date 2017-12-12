package com.stream.download;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.SimpleHandler;
import com.hippo.yorozuya.collect.SparseJLArray;
import com.stream.dao.DetailInfo;
import com.stream.dao.DownloadInfo;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.HStreamDB;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Fuzm on 2017/5/3 0003.
 */

public class DownloadManager implements DownloadQueen.DownloadWorkListener{

    private static final String TAG = DownloadManager.class.getSimpleName();

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_WAIT = 1;
    public static final int STATE_DOWNLOAD = 2;
    public static final int STATE_FINISH = 3;
    public static final int STATE_FAILED = 4;

    private Context mContext;
    private DownloadListener mDownloadListener;
    private DownloadInfoListener mDownloadInfoListener;

    private final SparseJLArray<DownloadQueen.WorkInfo> mWorkInfoMap = new SparseJLArray();
    private final SparseJLArray<DownloadDetail> mDownloadDetailMap = new SparseJLArray();
    private final LinkedList<DownloadDetail> mDefaultList;

    private SpeedReminder mSpeedReminder;
    private DownloadQueen mDownloadQueen;

    public DownloadManager(Context context) {
        mContext = context;

        mDownloadQueen = DownloadQueen.obtainQueen(HStreamApplication.getOkHttpClient(mContext));
        mDownloadQueen.setDownloadWorkListener(this);

        List<DownloadInfo> list = HStreamDB.queryAllDownloadInfo();
        mDefaultList = new LinkedList<>();

        Log.d(TAG, "load download info :" + list.size());
        for(DownloadInfo info: list) {
            //info.setState(STATE_NONE);
            if(info.getState() != STATE_FINISH) {
                info.setState(STATE_NONE);
            }

            DownloadQueen.WorkInfo workInfo = DownloadQueen.buildWork(info.getTitle(), info.getUrl());
            //workInfo.setOnDownLoaderListener(this);
            DownloadDetail detail = new DownloadDetail(info);
            mDownloadDetailMap.put(workInfo.getWId(), detail);
            mWorkInfoMap.put(workInfo.getWId(), workInfo);
            mDefaultList.add(detail);
        }

        mSpeedReminder = new SpeedReminder();
    }

    public List<DownloadDetail> getDownloadInfoList() {
        return mDefaultList;
    }

    private DownloadQueen.WorkInfo getWorkByToken(String token) {
        for(int i = 0; i < mDownloadDetailMap.size(); i++) {
            long workId = mWorkInfoMap.keyAt(i);
            DownloadDetail info = mDownloadDetailMap.get(workId);
            if(token.equals(info.getToken())) {
                return mWorkInfoMap.get(workId);
            }
        }
        return null;
    }

    public void startDownload(DownloadDetail param) {
        if(param.getDownloadUrl() != null) {
            DownloadDetail detailInfo = null;
            DownloadQueen.WorkInfo workInfo = getWorkByToken(param.getToken());
            if(workInfo != null) {
                detailInfo = mDownloadDetailMap.get(workInfo.getWId());

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(detailInfo);
                }

            } else {
                //add to database for download info
                DownloadInfo info = new DownloadInfo();
                info.setToken(param.getToken());
                info.setTitle(param.getTitle());
                info.setThumb(param.getThumb());
                info.setUrl(param.getDownloadUrl());
                info.setAlternative_name(param.getAlternativeName());
                info.setTime(System.currentTimeMillis());
                info.setState(STATE_WAIT);
                HStreamDB.putDownloadInfo(info);

                //build work info for download
                detailInfo = new DownloadDetail(param);
                detailInfo.setState(STATE_WAIT);
                workInfo = DownloadQueen.buildWork(detailInfo.getTitle(), detailInfo.getDownloadUrl());
                mWorkInfoMap.put(workInfo.getWId(), workInfo);
                mDownloadDetailMap.put(workInfo.getWId(), detailInfo);
                mDefaultList.add(detailInfo);
            }

            start(workInfo.getWId());

            if(mDownloadInfoListener != null) {
                mDownloadInfoListener.onAdd(detailInfo);
            }

            if(mDownloadListener != null) {
                mDownloadListener.onStart(detailInfo);
            }
        }
    }

    public void startRangeDownload(String[] tokenList) {
        for(String token: tokenList) {
            DownloadQueen.WorkInfo workInfo = getWorkByToken(token);
            if(workInfo != null) {
                DownloadDetail info = mDownloadDetailMap.get(workInfo.getWId());
                info.setState(STATE_WAIT);
                HStreamDB.updateDownloadInfoState(info.getToken(), STATE_WAIT);

                start(workInfo.getWId());

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(info);
                }
            }
        }
    }

    public void startDownloadAll() {
        for(int i=0; i<mWorkInfoMap.size(); i++) {
            int workId = (int) mWorkInfoMap.keyAt(i);

            DownloadDetail info = mDownloadDetailMap.get(workId);
            if(info.getState() != STATE_FINISH) {
                info.setState(STATE_WAIT);
                HStreamDB.updateDownloadInfoState(info.getToken(), STATE_WAIT);

                start(workId);

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(info);
                }
            }
        }
    }

    private void start(int workId) {
        DownloadQueen.WorkInfo workInfo = mWorkInfoMap.get(workId);
        if(workInfo != null) {
            mDownloadQueen.regiesteWork(workInfo);
            mDownloadQueen.start();
            mSpeedReminder.start();
        } else {
            Log.d(TAG, "not found the download info");
        }
    }

    public void stopDownload(String token){
        DownloadQueen.WorkInfo workInfo = getWorkByToken(token);
        if(workInfo != null) {
            mDownloadQueen.stopWork(workInfo);

            boolean ccAll = mSpeedReminder.onDone(workInfo.getWId());
            if(ccAll) {
                mSpeedReminder.stop();
            }

            DownloadDetail cancelInfo = mDownloadDetailMap.get(workInfo.getWId());
            cancelInfo.setState(STATE_NONE);
            HStreamDB.updateDownloadInfoState(cancelInfo.getToken(), STATE_NONE);

            if(mDownloadInfoListener != null) {
                mDownloadInfoListener.onCancel(cancelInfo);
            }

        } else {
            Log.d(TAG, "can not find download task thread");
        }
    }

    public void stopAllDownload() {
        mDownloadQueen.stopAll();
        //close speed remainder;
        mSpeedReminder.onFinish();
        mSpeedReminder.stop();

        List<DownloadDetail> list = getDownloadInfoList();
        for(DownloadDetail info: list) {
            if(info.getState() != STATE_FINISH) {
                info.setState(STATE_NONE);
                HStreamDB.updateDownloadInfoState(info.getToken(), STATE_NONE);
            }
        }

        if(mDownloadInfoListener != null) {
            mDownloadInfoListener.onUpdateAll();
        }
    }

    public void deleteDownload(DownloadDetail info) {
        stopDownload(info.getToken());
        //delete from database
        HStreamDB.removeDownloadInfo(info.getToken());
        //remove from cache
        DownloadQueen.WorkInfo workInfo = getWorkByToken(info.getToken());
        List<DownloadDetail> list = getDownloadInfoList();
        int position = list.indexOf(info);

        mDefaultList.remove(info);
        mDownloadDetailMap.remove(workInfo.getWId());
        mWorkInfoMap.remove(workInfo.getWId());
        //delete file
        DownloadUtil.deleteFile(workInfo.getFileName());

        //update listener
        if(mDownloadInfoListener != null) {
            mDownloadInfoListener.onRemove(info, position);
        }
    }

    public boolean isIdle() {
        return mDownloadQueen != null && mDownloadQueen.isIdle();
    }

    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
    }

    public void setDownloadInfoListener(DownloadInfoListener listener) {
        mDownloadInfoListener = listener;
    }

    @Override
    public void onStart(DownloadQueen.WorkInfo workInfo) {
        NotifyTask task = new NotifyTask();
        task.setOnStartData(workInfo.getWId());
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onDownload(DownloadQueen.WorkInfo workInfo, long contentLength, long finished, int bytesRead) {
        NotifyTask task = new NotifyTask();
        task.setOnDownloadData(workInfo.getWId(), contentLength, finished, bytesRead);
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onFinish(DownloadQueen.WorkInfo workInfo) {
        NotifyTask task = new NotifyTask();
        task.setOnFinishData(workInfo.getWId());
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onFailure(DownloadQueen.WorkInfo workInfo, String msg) {
        NotifyTask task = new NotifyTask();
        task.setOnFailureData(workInfo.getWId());
        SimpleHandler.getInstance().post(task);
    }

    @Override
    public void onGet410(DownloadQueen.WorkInfo workInfo) {
        NotifyTask task = new NotifyTask();
        task.setOnGet410Data(workInfo.getWId());
        SimpleHandler.getInstance().post(task);
    }

    protected class NotifyTask implements Runnable {

        private static final int TYPE_ON_START_DATA = 0;
        private static final int TYPE_ON_DOWNLOAD_DATA = 1;
        private static final int TYPE_ON_FINISH_DATA = 2;
        private static final int TYPE_ON_FAILURE_DATA = 3;
        private static final int TYPE_ON_GET_410_DATA = 4;

        private int mWorkId;
        private int mType;
        private long mContentLength;
        private long mFinished;
        private int mBytesRead;

        public void setOnStartData(int workId) {
            mType = TYPE_ON_START_DATA;
            mWorkId = workId;
        }

        public void setOnDownloadData(int workId, long contentLength, long finished, int bytesRead) {
            mType = TYPE_ON_DOWNLOAD_DATA;
            mWorkId = workId;
            mContentLength = contentLength;
            mFinished = finished;
            mBytesRead = bytesRead;
        }

        public void setOnFinishData(int workId) {
            mType = TYPE_ON_FINISH_DATA;
            mWorkId = workId;
        }

        public void setOnFailureData(int workId) {
            mType = TYPE_ON_FAILURE_DATA;
            mWorkId = workId;
        }

        public void setOnGet410Data(int workId) {
            mType = TYPE_ON_GET_410_DATA;
            mWorkId = workId;
        }


        @Override
        public void run() {
            switch (mType) {
                case TYPE_ON_START_DATA:
                    mSpeedReminder.onAdd(mWorkId);

                    DownloadDetail startInfo = mDownloadDetailMap.get(mWorkId);
                    startInfo.setState(STATE_DOWNLOAD);
                    HStreamDB.updateDownloadInfoState(startInfo.getToken(), STATE_DOWNLOAD);

                    if(mDownloadInfoListener != null) {
                        mDownloadInfoListener.onUpdate(startInfo);
                    }
                    break;

                case TYPE_ON_DOWNLOAD_DATA:
                    mSpeedReminder.onDownload(mWorkId, mContentLength, mFinished, mBytesRead);
                    return;

                case TYPE_ON_FINISH_DATA:
                    boolean isAll = mSpeedReminder.onDone(mWorkId);
                    if(isAll) {
                        mSpeedReminder.stop();
                    }

                    DownloadDetail finishInfo = mDownloadDetailMap.get(mWorkId);
                    finishInfo.setState(STATE_FINISH);
                    HStreamDB.updateDownloadInfoState(finishInfo.getToken(), STATE_FINISH);

                    if(mDownloadInfoListener != null) {
                        mDownloadInfoListener.onUpdate(finishInfo);
                    }

                    if(mDownloadListener != null) {
                        mDownloadListener.onFinish(finishInfo);
                    }

                    break;

                case TYPE_ON_FAILURE_DATA:
                    boolean checkAll = mSpeedReminder.onDone(mWorkId);
                    if(checkAll) {
                        mSpeedReminder.stop();
                    }

                    DownloadDetail failInfo = mDownloadDetailMap.get(mWorkId);
                    failInfo.setState(STATE_FAILED);
                    HStreamDB.updateDownloadInfoState(failInfo.getToken(), STATE_FAILED);

                    if(mDownloadInfoListener != null) {
                        mDownloadInfoListener.onUpdate(failInfo);
                    }
                    break;

                case TYPE_ON_GET_410_DATA :
                    mSpeedReminder.onDone(mWorkId);

                    //DownloadInfo fourInfo = mDownloadDetailMap.get(mWorkId);
                    //start(mWorkId);
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

        public void onAdd(int workId) {
            SpeedInfo speedInfo = new SpeedInfo();
            speedInfo.contentLength = 0;
            speedInfo.finished = 0;
            speedInfo.bytesRead = 0;
            speedInfo.speed = -1;
            speedInfo.oldSpeed = -1;
            mSpeedMap.put(workId, speedInfo);
        }

        public void onDownload(int workId, long contentLength, long finished, int bytesRead) {
            SpeedInfo info = mSpeedMap.get(workId, null);
            if(info != null) {
                info.contentLength = contentLength;
                info.finished = finished;
                info.bytesRead += bytesRead;

                mSpeedMap.put(workId, info);
                mBytesRead += bytesRead;
            }
        }

        public boolean onDone(int workId) {
            mSpeedMap.delete(workId);

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
            for(int i=0; i<mSpeedMap.size(); i++) {
                int workId = mSpeedMap.keyAt(i);
                SpeedInfo speedInfo = mSpeedMap.get(workId);
                long bytesRead = speedInfo.bytesRead;
                long oldSpeed = speedInfo.oldSpeed;

                long newSpeed = bytesRead / 2;
                if (oldSpeed != -1) {
                    newSpeed = (long) MathUtils.lerp(oldSpeed, newSpeed, 0.75f);
                }

                //next time use
                speedInfo.oldSpeed = newSpeed;
                speedInfo.bytesRead = 0;

                DownloadDetail info = mDownloadDetailMap.get(workId);
                if(info != null) {
                    info.setSpeed(speedInfo.oldSpeed);
                    info.setTotal(speedInfo.contentLength);
                    info.setFinished(speedInfo.finished);
                }

                total += speedInfo.contentLength;
                totalFinished += speedInfo.finished;

                if(mDownloadInfoListener != null) {
                    mDownloadInfoListener.onUpdate(info);
                }
            }

            if(mDownloadListener != null) {
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

        void onAdd(DownloadDetail info);

        void onUpdate(DownloadDetail info);

        void onUpdateAll();

        void onCancel(DownloadDetail info);

        void onRemove(DownloadDetail info, int position);
    }

    public interface DownloadListener {

        /**
         * Start download
         */
        void onStart(DownloadDetail info);

        /**
         * Update download speed
         */
        void onDownload(long speed, long total, long finished);

        /**
         * Download done
         */
        void onFinish(DownloadDetail info);

    }

}
