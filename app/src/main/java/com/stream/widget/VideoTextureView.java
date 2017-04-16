package com.stream.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by Fuzm on 2017/4/16 0016.
 */

public class VideoTextureView extends TextureView {

    private MediaPlayer mMediaPlayer;
    private Uri mUri;

    private Surface mSurface;

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurface = new Surface(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            if(mp != null) {
                mp.start();
            }
        }
    };


    public VideoTextureView(Context context) {
        this(context, null);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoPath(String url) {
        setVideoPath(Uri.parse(url));
    }

    public void setVideoPath(Uri uri) {
        openVideo();
    }

    private void openVideo() {

        if(mUri == null) {
            return;
        }

        try {

            mMediaPlayer = new MediaPlayer();

            mMediaPlayer.setOnPreparedListener(mPreparedListener);

            mMediaPlayer.setDataSource(getContext(), mUri, null);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            this.setSurfaceTextureListener(mSurfaceTextureListener);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
