package com.stream.hstream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

/**
 * Created by Fuzm on 2017/5/13 0013.
 */

public class VideoPalyActivity extends AppCompatActivity {

    public static final String KEY_TITLE = "video_title";
    public static final String KEY_URL = "video_url";

    private TuVideoPlayer mVideoPlayer;
    private String mTitle;
    private String mUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_play);

        //handle arguments from intent or savedInstanceState;
        handleArguments(savedInstanceState);

        mVideoPlayer = (TuVideoPlayer) findViewById(R.id.video_player);
        mVideoPlayer.setUp(mUrl, mTitle, TuVideoPlayer.MODE_FULL_SCREEN);
    }

    private void handleArguments(Bundle savedInstanceState) {
        if(null == savedInstanceState) {
            Intent intent = getIntent();
            mTitle = intent.getStringExtra(KEY_TITLE);
            mUrl = intent.getStringExtra(KEY_URL);
        } else {
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mUrl = savedInstanceState.getString(KEY_URL);
        }
    }

}
