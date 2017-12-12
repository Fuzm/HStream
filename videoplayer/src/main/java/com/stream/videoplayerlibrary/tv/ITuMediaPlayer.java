package com.stream.videoplayerlibrary.tv;

import android.view.Surface;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Seven-one on 2017/10/31 0031.
 */

public abstract class ITuMediaPlayer {

    public abstract void setDataSource(String path, Map<String, String> headers) throws IOException;

    public abstract void setSurface(Surface surface);

    public abstract void setAudioStreamType(int streamtype);

    public abstract void setScreenOnWhilePlaying(boolean screenOn);

    public abstract void prepareAsync() throws IllegalStateException;

    public abstract void start();

    public abstract void pause();

    public abstract void release();

    public abstract long getCurrentPosition();

    public abstract long getDuration();

    public abstract boolean isPlaying();

    public abstract void seekTo(long posi);

    public void setOption() {
    }

    /**
     * prepared listener
     */
    public interface OnPreparedListener {
        void onPrepared(final ITuMediaPlayer mp);
    }

    public void setOnPreparedListener(ITuMediaPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    protected ITuMediaPlayer.OnPreparedListener mOnPreparedListener;

    /**
     * completion listener
     */
    public interface OnCompletionListener {
        void onCompletion(final ITuMediaPlayer mp);
    }

    public void setOnCompletionListener(ITuMediaPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    protected ITuMediaPlayer.OnCompletionListener mOnCompletionListener;

    /**
     * info listener
     */
    public interface OnInfoListener {
        boolean onInfo(final ITuMediaPlayer mp, final int arg1, final int arg2);
    }

    public void setOnInfoListener(ITuMediaPlayer.OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    protected ITuMediaPlayer.OnInfoListener mOnInfoListener;

    /**
     * buffering update listener
     */
    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(final ITuMediaPlayer mp, final int percent);
    }

    public void setOnBufferingUpdateListener(ITuMediaPlayer.OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    protected ITuMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;

    /**
     * error listener
     */
    public interface OnErrorListener {
        boolean onError(final ITuMediaPlayer mp, final int i, final int i1);
    }

    public void setOnErrorListener(ITuMediaPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    protected ITuMediaPlayer.OnErrorListener mOnErrorListener;
}
