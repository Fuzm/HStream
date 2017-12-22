package com.stream.videoplayerlibrary.tv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.stream.videoplayerlibrary.common.ResizeTextureView;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Fuzm on 2017/4/22 0022.
 */

public class TuMediaPlayerManager implements TextureView.SurfaceTextureListener{

    private static final String TAG = TuMediaPlayerManager.class.getSimpleName();

    private static final int HANDLER_PREPARE = 1;
    private static final int HANDLER_RELEASE = 2;

    private static TuMediaPlayerManager sTuMediaPlayerManager;
    private TextureView mTextureView;
    private SurfaceTexture mSavedSurfaceTexture;

    private VideoPlayer mVideoPlayer;
    private ITuMediaPlayer mMediaPlayer;

    private String mUri;
    private Map<String, String> mHeaders;

//    private int mAudioSession;
//    private int mSurfaceWidth;
//    private int mSurfaceHeight;

    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;
    private Handler mainThreadHandler;

    private TuMediaPlayerManager() {
        mMediaHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_URGENT_AUDIO);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler((mMediaHandlerThread.getLooper()));
        mainThreadHandler = new Handler();
    }

    public synchronized static TuMediaPlayerManager instance() {
        if(sTuMediaPlayerManager == null) {
            sTuMediaPlayerManager = new TuMediaPlayerManager();
        }

        return sTuMediaPlayerManager;
    }

    public synchronized static void releaseManager() {
        if(sTuMediaPlayerManager != null) {
            sTuMediaPlayerManager.release(true);
        }
    }

    public synchronized static void pauseManager() {
        if(sTuMediaPlayerManager != null && sTuMediaPlayerManager.mVideoPlayer != null) {
            sTuMediaPlayerManager.mVideoPlayer.pause();
        }
    }

    protected synchronized void release(boolean clearState) {
        if(sTuMediaPlayerManager != null) {

            if(sTuMediaPlayerManager.getVideoPlayer() != null) {
                sTuMediaPlayerManager.getVideoPlayer().onRelease(clearState);
            }

            if( sTuMediaPlayerManager.mMediaPlayer != null) {
                sTuMediaPlayerManager.releaseMediaPlayer();
            }

            mTextureView = null;
            mSavedSurfaceTexture = null;
            mVideoPlayer = null;
        }
    }

    private synchronized void initTextureView(Context context) {
        removeTextureView();
        mTextureView = new ResizeTextureView(context);
        mTextureView.setSurfaceTextureListener(TuMediaPlayerManager.instance());
    }

    private void removeTextureView() {
        mSavedSurfaceTexture = null;
        if (mTextureView != null && mTextureView.getParent() != null) {
            ((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
        }
    }

    public TextureView getCurrentTextureView() {
        return mTextureView;
    }

    public void setVideoPlayer(VideoPlayer player) {
        if(mVideoPlayer != null && !isCurrentVideoPlayer(player)) {
            mVideoPlayer.onRelease(true);
        }

        mVideoPlayer = player;
    }

    public VideoPlayer getVideoPlayer() {
        return mVideoPlayer;
    }

    public boolean isCurrentVideoPlayer(VideoPlayer player) {
        if(mVideoPlayer == player) {
            return true;
        } else {
            return false;
        }
    }

    public TextureView openVideo(Context context, String uri, Map<String, String> headers) {
//        if(mMediaPlayer != null) {
//            releaseMediaPlayer();
//        }

        mUri = uri;
        mHeaders = headers;

        initTextureView(context);
        return mTextureView;
    }

    private void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    private void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    protected void start() {
        if(mMediaPlayer != null)
            mMediaPlayer.start();
    }

    protected void pause() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    protected void seekTo(long posi) {
        if(mMediaPlayer != null) {
            mMediaPlayer.seekTo(posi);
        }
    }

    protected boolean isPlaying() {
        if(mMediaPlayer != null) {
            return  mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    protected long getCurrentPosition() {
        long position = 0;
        if(mMediaPlayer == null) {
            return position;
        }

        try {
            position = mMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            Log.d(TAG, "get current positon by pleayer-" + mMediaPlayer.toString() + "-hashcode-" + mMediaPlayer.hashCode());
            e.printStackTrace();
            position = 0;
        }
        return position;
    }

    protected long getDuration() {
        return mMediaPlayer.getDuration();
    }

    private ITuMediaPlayer.OnPreparedListener mPreparedListener = new ITuMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final ITuMediaPlayer mp) {
            mMediaPlayer.start();
            if(mVideoPlayer != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoPlayer.onPrepared(mp);
                    }
                });
            }
        }
    };

    private ITuMediaPlayer.OnInfoListener mInfoListener = new ITuMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(final ITuMediaPlayer mp, final int i, final int i1) {
            if(mVideoPlayer != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoPlayer.onInfo(mp, i, i1);
                    }
                });
            }

            return false;
        }
    };

    private ITuMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new ITuMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(final ITuMediaPlayer mp, final int i) {
            if(mVideoPlayer != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVideoPlayer != null) {
                            mVideoPlayer.onBufferingUpdate(mp, i);
                        }
                    }
                });
            }
        }
    };

    private ITuMediaPlayer.OnCompletionListener mCompletionListener = new ITuMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(final ITuMediaPlayer mp) {
            if(mVideoPlayer != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoPlayer.onCompletion(mp);
                    }
                });
            }
        }
    };

    private ITuMediaPlayer.OnErrorListener mErrorListener = new ITuMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(final ITuMediaPlayer mp, final int i, final int i1) {
            if(mVideoPlayer != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoPlayer.onError(mp, i, i1);
                    }
                });
            }
            return true;
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        mSurfaceWidth = width;
//        mSurfaceHeight = height;

        if(mSavedSurfaceTexture != null) {
            mTextureView.setSurfaceTexture(mSavedSurfaceTexture);
        } else {
            mSavedSurfaceTexture = surface;
            prepare();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mSavedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class MediaHandler extends Handler {

        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case HANDLER_PREPARE:
                    try {
                        if(mMediaPlayer != null) {
                            mMediaPlayer.release();
                        }

                        Log.d(TAG, "play video address : " + mUri);
                        mMediaPlayer = new TuIjkMediaPlayer();
                        mMediaPlayer.setOnPreparedListener(mPreparedListener);
                        mMediaPlayer.setOnInfoListener(mInfoListener);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        mMediaPlayer.setOnErrorListener(mErrorListener);
                        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

                        Log.d(TAG, "play url: " + mUri);
                        mMediaPlayer.setDataSource(mUri, mHeaders);
                        mMediaPlayer.setSurface(new Surface(mSavedSurfaceTexture));
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setScreenOnWhilePlaying(true);
                        mMediaPlayer.prepareAsync();
                    } catch (IOException ex) {
                        Log.w(TAG, "Unable to open content: " + mUri, ex);
                        mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
                        return;
                    } catch (IllegalArgumentException ex) {
                        Log.w(TAG, "Unable to open content: " + mUri, ex);
                        mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
                        return;
                    } finally {
                        //mPendingSubtitleTracks.clear();
                    }
                    break;

                case HANDLER_RELEASE:
                    if(null != mMediaPlayer) {
                        mMediaPlayer.release();
                    }
                    break;
            }
        }
    }

}
