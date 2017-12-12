package com.stream.videoplayerlibrary.tv;

import android.view.Surface;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Seven-one on 2017/10/31 0031.
 */

public class TuIjkMediaPlayer extends ITuMediaPlayer {

    private IjkMediaPlayer mIjkMediaPlayer;

    public TuIjkMediaPlayer() {
        mIjkMediaPlayer = new IjkMediaPlayer();



        mIjkMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                if(mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared(TuIjkMediaPlayer.this);
                }
            }
        });
        mIjkMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if(mOnInfoListener != null) {
                    return mOnInfoListener.onInfo(TuIjkMediaPlayer.this, i, i1);
                } else {
                    return false;
                }
            }
        });
        mIjkMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                if(mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion(TuIjkMediaPlayer.this);
                }
            }
        });
        mIjkMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                if(mOnErrorListener != null) {
                    return mOnErrorListener.onError(TuIjkMediaPlayer.this, i, i1);
                } else {
                    return false;
                }
            }
        });
        mIjkMediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
                if(mOnBufferingUpdateListener != null) {
                    mOnBufferingUpdateListener.onBufferingUpdate(TuIjkMediaPlayer.this, i);
                }
            }
        });
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) throws IOException {
        mIjkMediaPlayer.setDataSource(path, headers);

//        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
//        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
//        mIjkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8);
        //mIjkMediaPlayer.setOption(1, "analyzemaxduration", 100L);
        mIjkMediaPlayer.setOption(1, "probesize", 10240L);
        mIjkMediaPlayer.setOption(1, "flush_packets", 1L);
        mIjkMediaPlayer.setOption(4, "packet-buffering", 0L);
        mIjkMediaPlayer.setOption(4, "framedrop", 1L);
    }

    @Override
    public void setSurface(Surface surface) {
        mIjkMediaPlayer.setSurface(surface);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        mIjkMediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mIjkMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mIjkMediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        mIjkMediaPlayer.start();
    }

    @Override
    public void pause() {
        mIjkMediaPlayer.pause();
    }

    @Override
    public void release() {
        mIjkMediaPlayer.release();
    }

    @Override
    public long getCurrentPosition() {
        return mIjkMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mIjkMediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mIjkMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long posi) {
        mIjkMediaPlayer.seekTo(posi);
    }
}
