package com.stream.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.SimpleHandler;
import com.hippo.yorozuya.collect.SparseJBArray;
import com.hippo.yorozuya.collect.SparseJLArray;
import com.stream.client.data.VideoInfo;
import com.stream.dao.DownloadInfo;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.MainActivity;
import com.stream.hstream.R;
import com.stream.hstream.VideoDownloadFragment;
import com.stream.scene.StageActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by Fuzm on 2017/5/3 0003.
 */

public class DownloadService extends Service implements DownloadManager.DownloadListener{

    private static final String TAG = DOWNLOAD_SERVICE.getClass().getSimpleName();

    public static final String KEY_VIDEO_INFO = "video_info";
    public static final String KEY_DOWN_INFO = "download_info";
    public static final String KEY_TOKEN_LIST = "token_list";

    public static final String ACTION_START = "start";
    public static final String ACTION_START_RANGE = "start_range";
    public static final String ACTION_START_ALL = "start_all";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_STOP_RANGE = "stop_range";
    public static final String ACTION_STOP_CURRENT = "stop_current";
    public static final String ACTION_STOP_ALL = "stop_all";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DELETE_RANGE = "delete_range";

    public static final String ACTION_CLEAR = "clear";

    private static final int ID_DOWNLOADING = 1;
    private static final int ID_DOWNLOADED = 2;

    private VideoInfo mVideoInfo;
    private DownloadManager mDownloadManager;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mDownloadingBuilder;
    private NotificationCompat.Builder mDownloadedBuilder;
    private NotificationDelay mDownloadingDelay;
    private NotificationDelay mDownloadedDelay;

    @Override
    public void onCreate() {
        super.onCreate();

        mDownloadManager = HStreamApplication.getDownloadManager(this);
        mDownloadManager.setDownloadListener(this);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if(ACTION_START == intent.getAction()) {
            mVideoInfo = intent.getParcelableExtra(KEY_VIDEO_INFO);
            mDownloadManager.startDownload(mVideoInfo);
        } else if(ACTION_START_RANGE == intent.getAction()) {
            String[] tokenList = intent.getStringArrayExtra(KEY_TOKEN_LIST);
            mDownloadManager.startRangeDownload(tokenList);
        } else if (ACTION_START_ALL == intent.getAction()) {
            mDownloadManager.startDownloadAll();
        } else if(ACTION_STOP_ALL == intent.getAction()) {
            mDownloadManager.stopAllDownload();
        } else if (ACTION_CLEAR.equals(intent.getAction())) {
            //clear();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mNotifyManager = null;
        if(mDownloadManager != null) {
            mDownloadManager.setDownloadListener(null);
            mDownloadManager = null;
        }

        mDownloadingBuilder = null;
        if(mDownloadingDelay != null) {
            mDownloadingDelay.release();
        }

        mDownloadedBuilder = null;
        if(mDownloadedDelay != null) {
            mDownloadedDelay.release();
        }
    }

    private void ensureDownloadingBuilder() {
        if (mDownloadingBuilder != null) {
            return;
        }

        Intent stopAllIntent = new Intent(this, DownloadService.class);
        stopAllIntent.setAction(ACTION_STOP_ALL);
        PendingIntent piStopAll = PendingIntent.getService(this, 0, stopAllIntent, 0);

        mDownloadingBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .addAction(R.drawable.ic_pause_x24, getString(R.string.stat_download_action_stop_all), piStopAll)
                .setShowWhen(false);

        mDownloadingDelay = new NotificationDelay(this, mNotifyManager, mDownloadingBuilder, ID_DOWNLOADING);
    }

    private void ensureDownloadedBuilder() {
        if (mDownloadedBuilder != null) {
            return;
        }

        Intent clearIntent = new Intent(this, DownloadService.class);
        clearIntent.setAction(ACTION_CLEAR);
        PendingIntent piClear = PendingIntent.getService(this, 0, clearIntent, 0);

        Bundle bundle = new Bundle();
        //bundle.putString(DownloadsScene.KEY_ACTION, DownloadsScene.ACTION_CLEAR_DOWNLOAD_SERVICE);
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setAction(StageActivity.ACTION_START_SCENE);
        activityIntent.putExtra(StageActivity.KEY_SCENE_NAME, VideoDownloadFragment.class.getName());
        activityIntent.putExtra(StageActivity.KEY_SCENE_ARGS, bundle);
        PendingIntent piActivity = PendingIntent.getActivity(DownloadService.this, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mDownloadedBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.stat_download_done_title))
                .setDeleteIntent(piClear)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentIntent(piActivity);

        mDownloadedDelay = new NotificationDelay(this, mNotifyManager, mDownloadedBuilder, ID_DOWNLOADED);
    }

    private void onUpdate(long speed, long total, long finished) {
        if (mNotifyManager == null) {
            return;
        }

        ensureDownloadingBuilder();

        if (speed < 0) {
            speed = 0;
        }

        String text = FileUtils.humanReadableByteCount(speed, true) + "/S";
        int progress = total != 0 ? (int)(finished / (double)total * 100) : 0;
        mDownloadingBuilder
                .setContentTitle(getString(R.string.stat_download_loading_title))
                .setContentText(text)
//                .setContentInfo(total == -1 || finished == -1
//                        ? null : FileUtils.humanReadableByteCount(finished, true)
//                                    + "/" +  FileUtils.humanReadableByteCount(total, true))
                .setProgress(100, progress, false);

        mDownloadingDelay.startForeground();
    }

    @Override
    public void onStart(DownloadInfo info) {
        if (mNotifyManager == null) {
            return;
        }

        ensureDownloadingBuilder();

        Bundle bundle = new Bundle();
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setAction(StageActivity.ACTION_START_SCENE);
        activityIntent.putExtra(StageActivity.KEY_SCENE_NAME, VideoDownloadFragment.class.getName());
        activityIntent.putExtra(StageActivity.KEY_SCENE_ARGS, bundle);
        PendingIntent piActivity = PendingIntent.getActivity(DownloadService.this, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mDownloadingBuilder
                .setTicker(info.getTitle() + getString(R.string.ticket_download_info_add))
                .setContentTitle(getString(R.string.stat_download_loading_title))
                .setContentText(null)
                .setContentInfo(null)
                .setProgress(0, 0, true)
                .setContentIntent(piActivity);

        mDownloadingDelay.startForeground();
    }

    @Override
    public void onDownload(long speed, long total, long finished) {
        onUpdate(speed, total, finished);
    }

    @Override
    public void onFinish(DownloadInfo info) {
        ensureDownloadedBuilder();

        mDownloadedBuilder
                .setTicker(info.getTitle() + getString(R.string.ticket_download_info_complete))
                .setContentText(info.getTitle())
                .setWhen(System.currentTimeMillis());
        mDownloadedDelay.show();

        checkStopSelf();
    }

    private void checkStopSelf() {
        if (mDownloadManager == null || mDownloadManager.isIdle()) {
            stopForeground(true);
            stopSelf();
        }
    }

    private static class NotificationDelay implements Runnable {

        @IntDef({OPS_NOTIFY, OPS_CANCEL, OPS_START_FOREGROUND})
        @Retention(RetentionPolicy.SOURCE)
        private @interface Ops {}

        private static final int OPS_NOTIFY = 0;
        private static final int OPS_CANCEL = 1;
        private static final int OPS_START_FOREGROUND = 2;

        private static final long DELAY = 1000; // 1s

        private Service mService;
        private final NotificationManager mNotifyManager;
        private final NotificationCompat.Builder mBuilder;
        private final int mId;

        private long mLastTime;
        private boolean mPosted;
        // false for show, true for cancel
        @Ops
        private int mOps;

        public NotificationDelay(Service service, NotificationManager notifyManager,
                                 NotificationCompat.Builder builder, int id) {
            mService = service;
            mNotifyManager = notifyManager;
            mBuilder = builder;
            mId = id;
        }

        public void release() {
            mService = null;
        }

        public void show() {
            if (mPosted) {
                mOps = OPS_NOTIFY;
            } else {
                long now = SystemClock.currentThreadTimeMillis();
                if (now - mLastTime > DELAY) {
                    // Wait long enough, do it now
                    mNotifyManager.notify(mId, mBuilder.build());
                } else {
                    // Too quick, post delay
                    mOps = OPS_NOTIFY;
                    mPosted = true;
                    SimpleHandler.getInstance().postDelayed(this, DELAY);
                }
                mLastTime = now;
            }
        }

        public void cancel() {
            if (mPosted) {
                mOps = OPS_CANCEL;
            } else {
                long now = SystemClock.currentThreadTimeMillis();
                if (now - mLastTime > DELAY) {
                    // Wait long enough, do it now
                    mNotifyManager.cancel(mId);
                } else {
                    // Too quick, post delay
                    mOps = OPS_CANCEL;
                    mPosted = true;
                    SimpleHandler.getInstance().postDelayed(this, DELAY);
                }
            }
        }

        public void startForeground() {
            if (mPosted) {
                mOps = OPS_START_FOREGROUND;
            } else {
                long now = SystemClock.currentThreadTimeMillis();
                if (now - mLastTime > DELAY) {
                    // Wait long enough, do it now
                    if (mService != null) {
                        mService.startForeground(mId, mBuilder.build());
                    }
                } else {
                    // Too quick, post delay
                    mOps = OPS_START_FOREGROUND;
                    mPosted = true;
                    SimpleHandler.getInstance().postDelayed(this, DELAY);
                }
            }
        }

        @Override
        public void run() {
            mPosted = false;
            switch (mOps) {
                case OPS_NOTIFY:
                    mNotifyManager.notify(mId, mBuilder.build());
                    break;
                case OPS_CANCEL:
                    mNotifyManager.cancel(mId);
                    break;
                case OPS_START_FOREGROUND:
                    if (mService != null) {
                        mService.startForeground(mId, mBuilder.build());
                    }
                    break;
            }
        }
    }

}
