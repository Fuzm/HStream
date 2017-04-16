package com.stream.hstream;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.stream.client.parser.VideoSourceUrlParser;
import com.stream.util.LoadImageHelper;
import com.stream.widget.FloatingPercentView;
import com.stream.widget.StreamVideoView;

/**
 * Created by Fuzm on 2017/3/26 0026.
 */

public class VideoPlayActivity extends AppCompatActivity implements View.OnTouchListener{

    public static final String TAG = VideoPlayActivity.class.getSimpleName();
    public static final String KEY_VIDEO_TITLE = "video_title";
    public static final String KEY_VIDEO_THUMB = "video_thumb";
    public static final String KEY_VIDEO_URL = "video_url";
    public static final String KEY_SAVE_POSITION = "save_position";

    private String mVideoTitle;
    private String mVideoThumb;
    private String mVideoUrl;
    private StreamVideoView mStreamVideoView;
    private GestureDetector mGestureDetector;
    private FloatingPercentView mPercentView;

    private VideoGestureListener mGestureListener;

    private int mSavePosition = 0;

    private void onInit() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        mVideoTitle = intent.getStringExtra(KEY_VIDEO_TITLE);
        mVideoThumb = intent.getStringExtra(KEY_VIDEO_THUMB);
        mVideoUrl = intent.getStringExtra(KEY_VIDEO_URL);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        onInit();
        setContentView(R.layout.activity_video_play);
        if(savedInstanceState != null) {
            mSavePosition = savedInstanceState.getInt(KEY_SAVE_POSITION);
        }

        mStreamVideoView = (StreamVideoView) findViewById(R.id.stream_video_view);
        mStreamVideoView.setTitle(mVideoTitle);
        mStreamVideoView.addBackButtonListener(new StreamVideoView.OnBackClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mStreamVideoView.setOnTouchListener(this);
        // load background image
        mStreamVideoView.loadBackground(mVideoThumb, mVideoThumb);

        mPercentView = (FloatingPercentView) findViewById(R.id.percent_view);
        mGestureListener = new VideoGestureListener();
        mGestureDetector = new GestureDetector(this, mGestureListener);

        if(mVideoUrl != null && !"".equals(mVideoUrl)) {
            mStreamVideoView.setVideoPath(mVideoUrl);
            mStreamVideoView.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mSavePosition = mStreamVideoView.getCurrentPosition();
        outState.putInt(KEY_SAVE_POSITION, mSavePosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mStreamVideoView.release();
    }

    private void checkEdgeFlag(MotionEvent e) {
        float x = e.getX();
        int width = VideoPlayActivity.this.getWindow().getDecorView().getWidth();

        if(x < width/2) {
            e.setEdgeFlags(MotionEvent.EDGE_LEFT);
        } else {
            e.setEdgeFlags(MotionEvent.EDGE_RIGHT);
        }
    }

    //改变屏幕亮度
    public void setLightness(float lightness){
        WindowManager.LayoutParams layoutParams =getWindow().getAttributes();
        layoutParams.screenBrightness =layoutParams.screenBrightness+lightness/255f;
        if(layoutParams.screenBrightness>1){
            layoutParams.screenBrightness=1;
        }else if(layoutParams.screenBrightness<0.0){
            layoutParams.screenBrightness=0.0f;
        }
        getWindow().setAttributes(layoutParams);

        mPercentView.setPercent(layoutParams.screenBrightness);
        mPercentView.showByType(FloatingPercentView.TYPE_LIGHT);
    }

    //加减音量
    public void setAudio(int volume){
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        //当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //最大音量
        int maxVolume =audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = currentVolume + volume;

        //Log.d(TAG, "current: " + currentVolume + ", max :" + maxVolume + ", add :" + volume);

        if(currentVolume >= 0 && currentVolume<=maxVolume){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);

            mPercentView.setPercent(currentVolume / (float) maxVolume);
            mPercentView.showByType(FloatingPercentView.TYPE_VOLUMN);
        }else {
            return;
        }
    }

    public void speedVideo(int mesc) {
        int pos = mStreamVideoView.getCurrentPosition();
        pos += mesc; // milliseconds
        mStreamVideoView.seekTo(pos);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkEdgeFlag(event);
                break;
            case MotionEvent.ACTION_UP:
                mGestureListener.clearStatus();
                break;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    public class VideoGestureListener implements GestureDetector.OnGestureListener {

        private boolean mUpAndDown = false;
        private boolean mLeftAndRight = false;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(TAG, "distance X: " + distanceX + ", distance Y :" + distanceY);
            if(!mLeftAndRight && (mUpAndDown || Math.abs(distanceY) > Math.abs(distanceX))) {
                mUpAndDown = true;
                if(distanceY > 0 && Math.abs(distanceY) > 2) {
                    if (e1.getEdgeFlags() == MotionEvent.EDGE_LEFT) {
                        setLightness(5);
                    } else if (e1.getEdgeFlags() == MotionEvent.EDGE_RIGHT) {
                        setAudio(1);
                    }
                } else if(distanceY < 0 && Math.abs(distanceY) > 2 && Math.abs(distanceY) > Math.abs(distanceX)) {
                    if (e1.getEdgeFlags() == MotionEvent.EDGE_LEFT) {
                        setLightness(-5);
                    } else if (e1.getEdgeFlags() == MotionEvent.EDGE_RIGHT) {
                        setAudio(-1);
                    }
                }
            } else {
                mLeftAndRight = true;
                //control progress
                if(distanceX > 0 && Math.abs(distanceX) > 2) {
                    speedVideo(-15000);
                } else if(distanceX < 0 && Math.abs(distanceX) > 2) {
                    speedVideo(15000);
                }
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void clearStatus(){
            mUpAndDown = false;
            mLeftAndRight = false;
        }
    }


}
