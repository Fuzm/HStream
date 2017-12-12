package com.stream.hstream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.stream.util.HSAssetManager;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

/**
 * Created by Fuzm on 2017/5/13 0013.
 */

public class VideoPalyActivity extends AppCompatActivity {

    private static final String KEY_TITLE = "video_title";
    private static final String KEY_URL = "video_url";
    private static final String KEY_SUBTITLE = "video_subtitle";

    private TuVideoPlayer mVideoPlayer;
    private String mTitle;
    private String mUrl;
    private String mSubtitle;

    public static Intent newIntent(Context context, String title, String videoPath) {
        return newIntent(context, title, videoPath, null);
    }

    public static Intent newIntent(Context context, String title, String videoPath, String subtitle) {
        Intent intent = new Intent(context, VideoPalyActivity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_URL, videoPath);
        intent.putExtra(KEY_SUBTITLE, subtitle);

        return intent;
    }

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

        //load subtitle
        if(!TextUtils.isEmpty(mSubtitle)) {
            mVideoPlayer.loadSubtitleFromAssets(HSAssetManager.getSubtitleDir() + mSubtitle);
        }
    }

    private void handleArguments(Bundle savedInstanceState) {
        if(null == savedInstanceState) {
            Intent intent = getIntent();
            mTitle = intent.getStringExtra(KEY_TITLE);
            mUrl = intent.getStringExtra(KEY_URL);
            mSubtitle = intent.getStringExtra(KEY_SUBTITLE);
        } else {
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mUrl = savedInstanceState.getString(KEY_URL);
            mSubtitle = savedInstanceState.getString(KEY_SUBTITLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_TITLE, mTitle);
        outState.putString(KEY_URL, mUrl);
        outState.putString(KEY_SUBTITLE, mSubtitle);
    }
}
