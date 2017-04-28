package com.stream.videoplayerlibrary.tv;

import android.media.MediaPlayer;

/**
 * Created by Fuzm on 2017/4/22 0022.
 */

public interface VideoPlayer {

    void onPrepared(MediaPlayer mp);

    boolean onInfo(MediaPlayer mp, int arg1, int arg2);

    void onBufferingUpdate(MediaPlayer mp, int percent);

    void onCompletion(MediaPlayer mp);

    boolean onError(MediaPlayer mp, int what, int extra);

    void onRelease();

    VideoPlayer getParentPlayer();

    int getCurrentScreenMode();
}
