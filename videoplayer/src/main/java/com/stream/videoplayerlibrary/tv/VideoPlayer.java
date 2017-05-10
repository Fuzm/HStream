package com.stream.videoplayerlibrary.tv;

import android.media.MediaPlayer;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Fuzm on 2017/4/22 0022.
 */

public interface VideoPlayer<E> {

    void onPrepared(E mp);

    boolean onInfo(E mp, int arg1, int arg2);

    void onBufferingUpdate(E mp, int percent);

    void onCompletion(E mp);

    boolean onError(E mp, int what, int extra);

    void onRelease();

    VideoPlayer getParentPlayer();

    int getCurrentScreenMode();
}
