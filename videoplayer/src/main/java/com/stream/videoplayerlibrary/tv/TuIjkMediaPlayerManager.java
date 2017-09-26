package com.stream.videoplayerlibrary.tv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.stream.videoplayerlibrary.common.ResizeTextureView;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Fuzm on 2017/4/22 0022.
 */

public class TuIjkMediaPlayerManager implements TextureView.SurfaceTextureListener{

    private static final String TAG = TuIjkMediaPlayerManager.class.getSimpleName();

    private static final int HANDLER_PREPARE = 1;
    private static final int HANDLER_RELEASE = 2;

    private static TuIjkMediaPlayerManager sTuMediaPlayerManager;
    private static TextureView sTextureView;
    private static SurfaceTexture sSavedSurfaceTexture;

    private VideoPlayer mVideoPlayer;
    private IjkMediaPlayer mMediaPlayer;

    private String mUri;
    private Map<String, String> mHeaders;

    private int mAudioSession;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;
    private Handler mainThreadHandler;

    private TuIjkMediaPlayerManager() {
        mMediaHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_URGENT_AUDIO);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler((mMediaHandlerThread.getLooper()));
        mainThreadHandler = new Handler();
    }

    public synchronized static TuIjkMediaPlayerManager instance() {
        if(sTuMediaPlayerManager == null) {
            sTuMediaPlayerManager = new TuIjkMediaPlayerManager();
        }

        return sTuMediaPlayerManager;
    }

    public synchronized static TuIjkMediaPlayerManager getManager() {
        return sTuMediaPlayerManager;
    }

    public static TextureView initTextureView(Context context) {
        removeTextureView();
        sTextureView = new ResizeTextureView(context);
        sTextureView.setSurfaceTextureListener(TuIjkMediaPlayerManager.instance());
        return sTextureView;
    }

    public static void removeTextureView() {
        sSavedSurfaceTexture = null;
        if (sTextureView != null && sTextureView.getParent() != null) {
            ((ViewGroup) sTextureView.getParent()).removeView(sTextureView);
        }
    }

    public static void releaseManager() {
        releaseManager(false);
    }

    public static void releaseManager(boolean currentPlayer) {
        if(sTuMediaPlayerManager != null) {
            if(!currentPlayer && sTuMediaPlayerManager.mVideoPlayer != null) {
                sTuMediaPlayerManager.mVideoPlayer.onRelease();
                sTuMediaPlayerManager.mVideoPlayer = null;
            }
            if( sTuMediaPlayerManager.mMediaPlayer != null) {
                sTuMediaPlayerManager.releaseMediaPlayer();
            }

            sTextureView = null;
            sSavedSurfaceTexture = null;
            //sTuMediaPlayerManager.mUri = null;
        }
    }

    public static TextureView getCurrentTextureView() {
        return sTextureView;
    }

    public void setVideoPlayer(VideoPlayer player) {
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

    public void setVideoPath(String uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
    }

    public String getVideoPath() {
        return mUri;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void seekTo(long posi) {
        mMediaPlayer.seekTo(posi);
    }

    public long getCurrentPosition() {
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

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final IMediaPlayer iMediaPlayer) {
            mMediaPlayer.start();
            if(mVideoPlayer != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoPlayer.onPrepared(iMediaPlayer);
                    }
                });
            }
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(final IMediaPlayer iMediaPlayer, final int i, final int i1) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onInfo(iMediaPlayer, i, i1);
                }
            });
            return false;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(final IMediaPlayer iMediaPlayer, final int i) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mVideoPlayer != null) {
                        mVideoPlayer.onBufferingUpdate(iMediaPlayer, i);
                    }
                }
            });
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(final IMediaPlayer iMediaPlayer) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onCompletion(iMediaPlayer);
                }
            });
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(final IMediaPlayer iMediaPlayer, final int i, final int i1) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onError(iMediaPlayer, i, i1);
                }
            });
            return true;
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        if(sSavedSurfaceTexture != null) {
            sTextureView.setSurfaceTexture(sSavedSurfaceTexture);
        } else {
            sSavedSurfaceTexture = surface;
            prepare();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return sSavedSurfaceTexture == null;
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
                        mMediaPlayer = new IjkMediaPlayer();
                        mMediaPlayer.setOnPreparedListener(mPreparedListener);
                        mMediaPlayer.setOnInfoListener(mInfoListener);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        mMediaPlayer.setOnErrorListener(mErrorListener);
                        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8);
                        mMediaPlayer.setOption(1, "analyzemaxduration", 100L);
                        mMediaPlayer.setOption(1, "probesize", 10240L);
                        mMediaPlayer.setOption(1, "flush_packets", 1L);
                        mMediaPlayer.setOption(4, "packet-buffering", 0L);
                        mMediaPlayer.setOption(4, "framedrop", 1L);

                        //mMediaPlayer.setDataSource(getContext(), mUri, null);
                        Log.d(TAG, "play url: " + mUri);
                        mMediaPlayer.setDataSource(mUri, mHeaders);
                        mMediaPlayer.setSurface(new Surface(sSavedSurfaceTexture));
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
