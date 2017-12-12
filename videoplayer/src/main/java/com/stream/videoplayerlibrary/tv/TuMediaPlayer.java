package com.stream.videoplayerlibrary.tv;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Seven-one on 2017/10/31 0031.
 */

public class TuMediaPlayer extends ITuMediaPlayer{

    private MediaPlayer mMediaPlayer;

    public TuMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared(TuMediaPlayer.this);
                }
            }
        });
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if(mOnInfoListener != null) {
                    return mOnInfoListener.onInfo(TuMediaPlayer.this, what, extra);
                } else {
                    return false;
                }
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion(TuMediaPlayer.this);
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(mOnErrorListener != null) {
                    return mOnErrorListener.onError(TuMediaPlayer.this, what, extra);
                } else {
                    return false;
                }
            }
        });
        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if(mOnBufferingUpdateListener != null) {
                    mOnBufferingUpdateListener.onBufferingUpdate(TuMediaPlayer.this, percent);
                }
            }
        });
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException {
        mMediaPlayer.setDataSource(path);
    }

    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    public void setAudioStreamType(int streamtype) {
        mMediaPlayer.setAudioStreamType(streamtype);
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        mMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    public void prepareAsync() throws IllegalStateException {
        mMediaPlayer.prepareAsync();
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void release() {
        mMediaPlayer.release();
    }

    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void seekTo(long posi) {
        mMediaPlayer.seekTo((int) posi);
    }
}
