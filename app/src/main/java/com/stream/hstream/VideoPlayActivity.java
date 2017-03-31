package com.stream.hstream;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

/**
 * Created by Fuzm on 2017/3/26 0026.
 */

public class VideoPlayActivity extends AppCompatActivity  {

    public static final String TAG = VideoPlayActivity.class.getSimpleName();
    public static final String KEY_VIDEO_URL = "video_url";

    private VideoView mVideoView;
    private MediaController mMediaController;
    private String mVideoUrl;
    private PointF mOriginPoint;
    private PointF mCurrentPoint;
    private GestureDetector mGestureDetector;

    private void onInit() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        mVideoUrl = intent.getStringExtra(KEY_VIDEO_URL);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_video_play);

        onInit();

        mVideoView = (VideoView) findViewById(R.id.videoView);
        mMediaController = new MediaController(this);
        mMediaController.show(0);
        mVideoView.setMediaController(mMediaController);
        if(mVideoUrl != null && !"".equals(mVideoUrl)) {
            mVideoView.setVideoURI(Uri.parse(mVideoUrl));
        }

        mVideoView.setLongClickable(true);
        //mVideoView.setOnTouchListener(this);

        mGestureDetector = new GestureDetector(this, new VideoGestureListener());
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        mVideoView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mVideoView.start();
        mVideoView.setFocusable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mVideoView != null) {
            mVideoView = null;
            mMediaController = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        Log.d(TAG, "touch enter");
//        return mGestureDetector.onTouchEvent(event);
//
//        /*switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN :
//                mOriginPoint = new PointF(event.getX(), event.getY());
//                break;
//            case MotionEvent.ACTION_MOVE :
//                mCurrentPoint = new PointF(event.getX(), event.getY());
//
//                Log.d(TAG, "Edge flag: " + event.getEdgeFlags());
//                if(event.getEdgeFlags() == MotionEvent.EDGE_RIGHT) {
//                    float distance = mCurrentPoint.y - mOriginPoint.y;
//                    Log.d(TAG, "move distance: " + distance);
//                    if(distance > 0 && Math.abs(distance) > 10) {
//                        setAudio(10);
//                    } else if(distance < 0 && Math.abs(distance) > 10){
//                        setAudio(-10);
//                    }
//                }
//                break;
//        }
//
//        return false;*/
//    }

    //改变屏幕亮度
    public void setLightness(float lightness){
        WindowManager.LayoutParams layoutParams =getWindow().getAttributes();
        layoutParams.screenBrightness =layoutParams.screenBrightness+lightness/255f;
        if(layoutParams.screenBrightness>1){
            layoutParams.screenBrightness=1;
        }else if(layoutParams.screenBrightness<0.2){
            layoutParams.screenBrightness=0.2f;
        }
        getWindow().setAttributes(layoutParams);
    }

    //加减音量
    public void setAudio(int volume){
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        //当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //最大音量
        int maxVolume =audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = currentVolume + volume;

        Log.d(TAG, "current: " + currentVolume + ", max :" + maxVolume + ", add :" + volume);

        if(currentVolume >= 0 && currentVolume<=maxVolume){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);
        }else {
            return;
        }
    }

    public class VideoGestureListener implements GestureDetector.OnGestureListener {

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
            Log.d(TAG, "edge flag: " + (e1.getEdgeFlags()&MotionEvent.EDGE_LEFT) + ", distance y :" + distanceY);
            if(distanceY > 0 && Math.abs(distanceY) > 5 ) {
                setAudio(1);
            } else if(distanceY < 0 && Math.abs(distanceY) > 5) {
                setAudio(-1);
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
    }


}
