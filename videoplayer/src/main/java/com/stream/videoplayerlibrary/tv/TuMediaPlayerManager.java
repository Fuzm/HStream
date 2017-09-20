package com.stream.videoplayerlibrary.tv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.stream.videoplayerlibrary.common.ResizeTextureView;

import java.io.IOException;

/**
 * Created by Fuzm on 2017/4/22 0022.
 */

public class TuMediaPlayerManager implements TextureView.SurfaceTextureListener{

    private static final String TAG = TuMediaPlayerManager.class.getSimpleName();

    private static final int HANDLER_PREPARE = 1;
    private static final int HANDLER_RELEASE = 2;

    private static TuMediaPlayerManager sTuMediaPlayerManager;
    private static TextureView sTextureView;
    private static SurfaceTexture sSavedSurfaceTexture;

    private VideoPlayer mVideoPlayer;
    private MediaPlayer mMediaPlayer;
    private String mUri;
    private int mAudioSession;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;
    private Handler mainThreadHandler;

    public static TuMediaPlayerManager instance() {
        if(sTuMediaPlayerManager == null) {
            sTuMediaPlayerManager = new TuMediaPlayerManager();
        }

        return sTuMediaPlayerManager;
    }

    private TuMediaPlayerManager() {
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler((mMediaHandlerThread.getLooper()));
        mainThreadHandler = new Handler();
    }

    public static TextureView initTextureView(Context context) {
        removeTextureView();
        sTextureView = new ResizeTextureView(context);
        sTextureView.setSurfaceTextureListener(TuMediaPlayerManager.instance());
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
                sTuMediaPlayerManager.mMediaPlayer.release();
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

    public void setVideoPath(String uri) {
        mUri = uri;
    }

    public String getVideoPath() {
        return mUri;
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
        mMediaPlayer.seekTo((int) posi);
    }

    public int getCurrentPosition() {
        if(mMediaPlayer == null) {
            return 0;
        }
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(final MediaPlayer mp) {
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

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        public  boolean onInfo(final MediaPlayer mp, final int arg1, final int arg2) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onInfo(mp, arg1, arg2);
                }
            });
            return false;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onBufferingUpdate(mp, percent);
                }
            });
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(final MediaPlayer mp) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onCompletion(mp);
                }
            });
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVideoPlayer.onError(mp, what, extra);
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
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setOnPreparedListener(mPreparedListener);
                        mMediaPlayer.setOnInfoListener(mInfoListener);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        mMediaPlayer.setOnErrorListener(mErrorListener);
                        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

                        //mMediaPlayer.setDataSource(getContext(), mUri, null);
                        mMediaPlayer.setDataSource(mUri);
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
