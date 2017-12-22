package com.stream.videoplayerlibrary.tv;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.stream.videoplayerlibrary.R;
import com.stream.videoplayerlibrary.common.FloatingPercentView;
import com.stream.videoplayerlibrary.common.VideoUtils;
import com.stream.videoplayerlibrary.widget.SubtitleText;

import java.io.File;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Fuzm on 2017/4/19 0019.
 */

public final class TuVideoPlayer extends FrameLayout
        implements View.OnClickListener, View.OnTouchListener, VideoPlayer<ITuMediaPlayer>{

    private static final String TAG = TuVideoPlayer.class.getSimpleName();

    private static final int MAX_PROGRESS = 1000;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    public static final int MODE_NORMAL_SCREEN = 1;
    public static final int MODE_FULL_SCREEN = 2;

    //private static TuVideoPlayer sFullScreenPlayer;

    private Context mContext;
    private String mUrl;
    private Map<String, String> mHeaders;

    private int mCurrentState = STATE_IDLE;
    private int mCurrentScreenMode = MODE_NORMAL_SCREEN;
    private boolean mShowing = false;
    private int mShowTime = 3000;
    private long mCurrentPosition = -1;
    private int mCurrentBufferPercentage = 0;
    private int mSeekWhenPrepared = 0;

    private GestureDetector mGestureDetector;
    private VideoGestureListener mGestureListener;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    //private TuIjkMediaPlayerManager mManager;
    private TuMediaPlayerManager mManager;
    private TuVideoPlayer mParentPlayer;
    private ViewGroup mTextureViewContainer;
    private AppCompatImageView mThumb;
    private FloatingPercentView mPercentView;
    //part of header
    private ViewGroup mVideoTop;
    private AppCompatImageView mBackButton;
    private TextView mTextTitle;
    private AppCompatImageView mFavoriteButton;
    //part of center
    private ViewGroup mVideoCenter;
    private AppCompatImageView mPlayButton;
    private ProgressBar mWaitBar;
    //part of bottom
    private ViewGroup mVideoBottom;
    private SeekBar mSeekBar;
    private TextView mTimeText;
    private TextView mTotalText;
    private AppCompatImageView mScreenControlButton;
    //subtitle
    private SubtitleText mSubtitleText;

    private OnFavoriteListener mFavoriteListener;
    private OnPreparedListener mPreParedListener;

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) {
                return;
            }

            long duration = mManager.getDuration();
            long newPosition = (duration * progress) / MAX_PROGRESS;
            mManager.seekTo(newPosition);
            if(mTimeText != null) {
                mTimeText.setText(stringForTime(newPosition));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            show(3600000);
            removeCallbacks(mShowProgress);
            removeCallbacks(mBufferWait);

            //slove slide problem
            ViewParent parent = getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                parent = parent.getParent();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setProgress();
            post(mShowProgress);
            post(mBufferWait);
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            long pos = setProgress();
            if (mShowing && mCurrentState == STATE_PLAYING) {
                postDelayed(mShowProgress, MAX_PROGRESS - (pos % MAX_PROGRESS));
            }
        }
    };

    private final Runnable mBufferWait = new Runnable() {
        private boolean isWait = false;
        @Override
        public void run() {
            if(isShown() && (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE)) {
                long oldPosition = mCurrentPosition;
                mCurrentPosition = mManager.getCurrentPosition();
                if(oldPosition == mCurrentPosition && mCurrentState == STATE_PLAYING ) {
                    isWait = true;
                    waitUi();
                } else {
                    if(isWait) {
                        isWait = false;
                        hide();
                    }
                }

                postDelayed(this, MAX_PROGRESS - (mCurrentPosition % MAX_PROGRESS));
            }

            //不再显示区内时，自动停止
            if(!isShown() && mManager != null) {
                Log.d(TAG, "frame not show, pause the video player");
                //mManager.releaseMediaPlayer();
                release(true);
            }
        }
    };

    private Runnable mFateOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public TuVideoPlayer(Context context) {
        this(context, null);
    }

    public TuVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TuVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        //mManager = TuIjkMediaPlayerManager.instance();
        mManager = TuMediaPlayerManager.instance();

        initView();
    }

    private void initView() {
        Log.d(TAG, "init videw :" + this.hashCode());
        View view = LayoutInflater.from(mContext).inflate(R.layout.tu_video_player, this);

        //video
        mTextureViewContainer = (ViewGroup) view.findViewById(R.id.textureview_container);
        mThumb = (AppCompatImageView) view.findViewById(R.id.thumb);
        mPercentView = (FloatingPercentView) view.findViewById(R.id.percent_view);

        //video header
        mVideoTop = (ViewGroup) view.findViewById(R.id.video_top);
        mBackButton = (AppCompatImageView) view.findViewById(R.id.back_button);
        mTextTitle = (TextView) view.findViewById(R.id.view_title);

        //video center
        mVideoCenter = (ViewGroup) view.findViewById(R.id.video_center);
        mPlayButton = (AppCompatImageView) view.findViewById(R.id.play_button);
        mWaitBar = (ProgressBar) view.findViewById(R.id.wait_bar);
        mVideoCenter.setOnClickListener(this);
        mVideoCenter.setOnTouchListener(this);
        mPlayButton.setOnClickListener(this);

        //video bottom
        mVideoBottom = (ViewGroup) view.findViewById(R.id.video_bottom);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mTimeText = (TextView) view.findViewById(R.id.time_text);
        mTotalText = (TextView) view.findViewById(R.id.total_time_text);
        mScreenControlButton = (AppCompatImageView) view.findViewById(R.id.screen_control);

        //subtitle
        mSubtitleText = (SubtitleText) view.findViewById(R.id.subtitle_text);

        mSeekBar.setMax(MAX_PROGRESS);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mScreenControlButton.setOnClickListener(this);

        //setUiVisiable(VISIBLE, VISIBLE, VISIBLE, GONE, GONE);
        setCurrentStateAndUi(STATE_IDLE);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        //this.setOnTouchListener(this);
    }

    /*public void setCurrentScreenMode(int screenMode) {
        Log.d(TAG, "set current screen mode:" + screenMode);
        mCurrentScreenMode = screenMode;
        setCurrentStateAndUi(STATE_IDLE);
    }*/

    public void setUp(String uri, String title, int screenMode) {
        mUrl = uri;
        mCurrentScreenMode = screenMode;

        if(screenMode == MODE_NORMAL_SCREEN) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.tu_video_player_normal_top, mVideoTop);
            mTextTitle = (TextView) view.findViewById(R.id.view_title);

        } else if(screenMode == MODE_FULL_SCREEN) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.tu_video_player_fullscreen_top, mVideoTop);
            mTextTitle = (TextView) view.findViewById(R.id.view_title);
            mBackButton = (AppCompatImageView) view.findViewById(R.id.back_button);
            mFavoriteButton = (AppCompatImageView) view.findViewById(R.id.favorite_button);

            //updateFavoriteButton();
            mBackButton.setOnClickListener(this);
            mFavoriteButton.setOnClickListener(this);

            mGestureListener = new VideoGestureListener();
            mGestureDetector = new GestureDetector(mContext, mGestureListener);

            //if full screen, set subtitle text size large
            mSubtitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }

        setTitle(title);
        setCurrentStateAndUi(STATE_IDLE);
        mThumb.setImageDrawable(null);
    }

    /**
     * load subtitle
     * @param file
     */
    public void loadSubtitle(File file) {
        if(file != null && mSubtitleText != null) {
            mSubtitleText.load(this, file);
        }
    }

    /**
     * load subtitle for path
     * @param filePath
     */
    public void loadSubtitle(String filePath) {
        File file = new File(filePath);
        if(file.exists()) {
            loadSubtitle(file);
        }
    }

    /**
     * load subtitle from assets dir
     * @param assetsPath
     */
    public void loadSubtitleFromAssets(String assetsPath) {
        if(!TextUtils.isEmpty(assetsPath)) {
            mSubtitleText.load(this, assetsPath);
        }
    }

    /**
     * ajust subtitle show time
     * @param ajustTime
     */
    public void ajustSubtitleTime(float ajustTime) {
        if(mSubtitleText != null) {
            mSubtitleText.setAjustTime(ajustTime);
        }
    }

    private void release(boolean clearState) {
        mManager.release(clearState);
    }

    public void start() {
        setCurrentStateAndUi(STATE_PREPARING);
        prepareMediaPlayer();
    }

    @Override
    public void pause() {
        setCurrentStateAndUi(STATE_PAUSE);
        if(mManager != null) {
            mManager.pause();
        }
    }

    public void suspend() {
        release(false);
    }

    public void setTitle(CharSequence title) {
        mTextTitle.setText(title);
    }

    public CharSequence getTitle() {
        return mTextTitle.getText();
    }

    public void setThumb(Drawable drawable) {
        mThumb.setImageDrawable(drawable);
    }

    public AppCompatImageView getThumb(){
        return mThumb;
    }

    public void seekTo(int posi) {
        mManager.seekTo(posi);
    }

    public String getVideoPath() {
        return mUrl;
    }

    public void setVideoPath(String url) {
        setVideoPath(url, null);
    }

    public void setVideoPath(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
        prepareMediaPlayer();
    }

    private void setUiVisiable(int videoTop, int videoCenter, int videoBottom, int thumb, int playButton, int waitBar) {
        mThumb.setVisibility(thumb);
        // part of header
        mVideoTop.setVisibility(videoTop);
        // part of center
        mVideoCenter.setVisibility(videoCenter);
        mPlayButton.setVisibility(playButton);
        mWaitBar.setVisibility(waitBar);

        // part of bottom
        mVideoBottom.setVisibility(videoBottom);
    }

    private void setCurrentStateAndUi(int state) {
        mCurrentState = state;
        switch (state) {
            case STATE_IDLE:
                Log.d(TAG, "init player");
//                if(!mManager.isCurrentVideoPlayer(this)) {
//                    mManager.releaseMediaPlayer();
//                }
                changeUiForInit();
                break;
            case STATE_PREPARING:
                changeUiForWaitStart();
                break;
            case STATE_PLAYING:
            case STATE_PAUSE:
                updatePlayButton();
                show(3000);
                break;
        }
    }

    private void prepareMediaPlayer() {
        if(!checkPrepared()) return;
        Log.d(TAG, "prepare mediaplayer by instance-" + this.hashCode());

        //request audio
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mManager.setVideoPlayer(this);
        TextureView textureView = mManager.openVideo(mContext, mUrl, mHeaders);
        addTextureView(textureView);
    }

    private void addTextureView(TextureView textureView) {
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        mTextureViewContainer.addView(textureView, layoutParams);
    }

    private void waitUi() {
        if(mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE) {
            //setUiVisiable(GONE, VISIBLE, GONE, VISIBLE, VISIBLE);
            changeUiForWaitBuffer();
        } else {
            //setUiVisiable(VISIBLE, VISIBLE, GONE, VISIBLE, GONE);
            changeUiForWaitStart();
        }
    }

    private void show(int timeout) {
        mShowing = true;
        changeUiForShow();
        post(mShowProgress);
        if(timeout != 0) {
            removeCallbacks(mFateOut);
            postDelayed(mFateOut, timeout);
        }
    }

    private void hide() {
        mShowing = false;
        removeCallbacks(mShowProgress);
        //setUiVisiable(GONE, GONE, GONE, GONE, GONE);
        changeUiForHide();
    }

    private boolean checkPrepared() {
        return (mCurrentState != STATE_IDLE &&
                mUrl != null);
    }

    private long setProgress() {
        long position = mManager.getCurrentPosition();
        long duration = mManager.getDuration();
        if(mSeekBar != null) {
            if(duration > 0) {
                int pos = (int)(MAX_PROGRESS * position / duration);
                mSeekBar.setProgress(pos);
            }
//            int percent = mCurrentBufferPercentage;
//            int second = percent * MAX_PROGRESS / 100;
//            mSeekBar.setSecondaryProgress(second);
            //Log.d(TAG, "percent : " + percent + ",second : " + second + ", secondary progress : " + mSeekBar.getSecondaryProgress());
        }

        if(mTotalText != null) {
            mTotalText.setText(stringForTime(duration));
        }

        if(mTimeText != null) {
            mTimeText.setText(stringForTime(position));
        }

        return position;
    }

    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;

        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void changeUiForInit() {
        updatePlayButton();
        setUiVisiable(VISIBLE, VISIBLE, GONE, VISIBLE, VISIBLE, GONE);
    }

    private void changeUiForWaitStart() {
        setUiVisiable(VISIBLE, VISIBLE, GONE, VISIBLE, GONE, VISIBLE);
    }

    private void changeUiForWaitBuffer() {
        setUiVisiable(VISIBLE, VISIBLE, VISIBLE, GONE, GONE, VISIBLE);
    }

    private void changeUiForShow() {
        setUiVisiable(VISIBLE, VISIBLE, VISIBLE, GONE, VISIBLE, GONE);
    }

    private void changeUiForHide() {
        setUiVisiable(GONE, GONE, GONE, GONE, GONE, GONE);
    }

    private void updatePlayButton() {
        if(mCurrentState == STATE_PLAYING) {
            mPlayButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPlayButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    /**
     * update screen control button visibility
     * @param mode
     */
    private void updateScreenControlView(int mode){
        if(mode == MODE_FULL_SCREEN) {
            if(mParentPlayer == null) {
                //no parent player, can not change mode for normal
                mScreenControlButton.setVisibility(GONE);
            } else {
                mScreenControlButton.setImageResource(R.drawable.ic_screen_normal);
            }
        } else {
            mScreenControlButton.setImageResource(R.drawable.ic_screen_full);
        }
    }

    public void startFullScreen() {
        AppCompatActivity activity =  VideoUtils.getAppCompActivity(mContext);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Window window = activity.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mTextureViewContainer.removeView(mManager.getCurrentTextureView());

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TuVideoPlayer fullPlayer = new TuVideoPlayer(mContext);
        window.addContentView(fullPlayer, lp);

        fullPlayer.setUp(mUrl, mTextTitle.getText().toString(), MODE_FULL_SCREEN);
        //fullPlayer.mCurrentScreenMode = MODE_FULL_SCREEN;
        fullPlayer.mParentPlayer = this;
        fullPlayer.setCurrentStateAndUi(mCurrentState);
        fullPlayer.addTextureView(mManager.getCurrentTextureView());
        fullPlayer.updatePlayButton();
        fullPlayer.updateScreenControlView(fullPlayer.mCurrentScreenMode);
        fullPlayer.setThumb(mThumb.getDrawable());
        fullPlayer.setOnFavoriteListener(mFavoriteListener);

        //load subtitle
        if(mSubtitleText.getSubtitleFile() != null) {
            fullPlayer.loadSubtitle(mSubtitleText.getSubtitleFile());
        } else if(!TextUtils.isEmpty(mSubtitleText.getSubtitleAssetsPath())){
            fullPlayer.loadSubtitleFromAssets(mSubtitleText.getSubtitleAssetsPath());
        }

        mManager.setVideoPlayer(fullPlayer);
    }

    public void clearFullScreen() {
        AppCompatActivity activity = VideoUtils.getAppCompActivity(mContext);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ViewGroup parent = (ViewGroup) this.getParent();
        parent.removeView(this);
        mTextureViewContainer.removeView(mManager.getCurrentTextureView());

        mParentPlayer.addTextureView(mManager.getCurrentTextureView());
        mParentPlayer.setCurrentStateAndUi(mCurrentState);
        mParentPlayer.updatePlayButton();
        mParentPlayer.updateScreenControlView(mParentPlayer.mCurrentScreenMode);
        mManager.setVideoPlayer(mParentPlayer);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.screen_control) {
            if(mCurrentScreenMode == MODE_NORMAL_SCREEN) {
                startFullScreen();
            } else {
                clearFullScreen();
            }

        } else if(v.getId() == R.id.video_center) {
            if(mCurrentState == STATE_IDLE) {
                setCurrentStateAndUi(STATE_PREPARING);
                prepareMediaPlayer();
            }

        } else if(v.getId() == R.id.play_button) {
            if(mCurrentState == STATE_PLAYING) {
                removeCallbacks(mFateOut);
                mManager.pause();
                mShowTime = 0;
                setCurrentStateAndUi(STATE_PAUSE);
            } else if(mCurrentState == STATE_PAUSE) {
                mManager.start();
                mShowTime = 3000;
                setCurrentStateAndUi(STATE_PLAYING);
            } else {
                onClick(mVideoCenter);
            }

        } else if(v.getId() == R.id.back_button) {
            if(getParentPlayer() != null) {
                clearFullScreen();
            } else {
                AppCompatActivity activity = VideoUtils.getAppCompActivity(mContext);
                activity.onBackPressed();
            }
        } else if(v.getId() == R.id.favorite_button) {
            if(mFavoriteListener != null) {
                mFavoriteListener.onFavorite(mUrl);
                updateFavoriteButton();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.video_center) {
            if(mCurrentState == STATE_PLAYING) {
                hide();
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkEdgeFlag(event);
                break;
            case MotionEvent.ACTION_UP:
                if(mPercentView.isShow()){
                    mPercentView.hide();
                } else {
                    show(mShowTime);
                }
                break;
        }

        if(mCurrentScreenMode == MODE_FULL_SCREEN && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING) {
            mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    public void updateFavoriteButton() {
        if(mFavoriteListener != null && mFavoriteListener.isFavorited()) {
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_24dp);
        } else {
            mFavoriteButton.setImageResource(R.drawable.ic_favorite_none_24dp);
        }
    }

    @Override
    public void onPrepared(ITuMediaPlayer mp) {
        if(mCurrentState != STATE_PREPARING) {
            Log.d(TAG, "current state is not preparing, cannot play the video");
            return;
        }

        if(mPreParedListener != null) {
            mPreParedListener.onPrepared(this);
        }

        if (mSeekWhenPrepared != 0) {
            mManager.seekTo(mSeekWhenPrepared);
            mSeekWhenPrepared = 0;
        } else {
            long position = VideoUtils.getSavedProgress(getContext(), mUrl);
            if (position != 0) {
                mManager.seekTo(position);
            }
        }

        setCurrentStateAndUi(STATE_PLAYING);
        post(mBufferWait);
    }

    @Override
    public boolean onInfo(ITuMediaPlayer mp, int arg1, int arg2) {
        return false;
    }

    @Override
    public void onBufferingUpdate(ITuMediaPlayer mp, int percent) {
        //Log.d(TAG, "buffer percent: " + percent);
        mCurrentBufferPercentage = percent;

        int second = percent * MAX_PROGRESS / 100;
        mSeekBar.setSecondaryProgress(second);
    }

    @Override
    public void onCompletion(ITuMediaPlayer mp) {
        Log.d(TAG, "completion mp : " + mp.hashCode());

        if (mCurrentScreenMode == STATE_PLAYING) {
            VideoUtils.saveProgress(getContext(), mUrl, 0);
        }

        //remove listener
        removeCallbacks(mBufferWait);
        removeCallbacks(mFateOut);
        removeCallbacks(mShowProgress);
        //init state
        setCurrentStateAndUi(STATE_IDLE);
        //cancel subtitle view
        mSubtitleText.cancel();

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(null);
    }

    @Override
    public boolean onError(ITuMediaPlayer mp, int what, int extra) {
        release(true);

        Toast.makeText(mContext, "无法播放该视频", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onRelease(boolean clearState) {
        Log.d(TAG, "release video player : " + this.hashCode());
        if (mCurrentState == STATE_PLAYING) {
            long position = mManager.getCurrentPosition();
            VideoUtils.saveProgress(getContext(), mUrl, position);
        }

        //remove listener
        removeCallbacks(mBufferWait);
        removeCallbacks(mFateOut);
        removeCallbacks(mShowProgress);
        //init state
        if(clearState) {
            setCurrentStateAndUi(STATE_IDLE);
        }
        //clear view
        mTextureViewContainer.removeView(mManager.getCurrentTextureView());
        //cancel subtitle view
        mSubtitleText.cancel();

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(null);
    }

    @Override
    public VideoPlayer getParentPlayer() {
        return mParentPlayer;
    }

    @Override
    public int getCurrentScreenMode() {
        return mCurrentScreenMode;
    }

    @Override
    public boolean isPlaying() {
        if(mManager != null) {
            return mManager.isPlaying();
        } else {
            return false;
        }
    }

    @Override
    public long getCurrentPosition() {
        if(mManager != null) {
            return mManager.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /**
     * 回退事件
     */
    public static boolean backPress() {
        TuVideoPlayer player = (TuVideoPlayer) TuMediaPlayerManager.instance().getVideoPlayer();
        if(player != null) {
            if(player.getParentPlayer() != null) {
                player.clearFullScreen();
                return true;
            } else {
                //TuIjkMediaPlayerManager.releaseManager();
                player.release(true);
                return false;
            }
        } else {
            return false;
        }
    }

    //检查触摸位置
    private void checkEdgeFlag(MotionEvent e) {
        float x = e.getX();
        int width = VideoUtils.getAppCompActivity(mContext).getWindow().getDecorView().getWidth();

        if(x < width/2) {
            e.setEdgeFlags(MotionEvent.EDGE_LEFT);
        } else {
            e.setEdgeFlags(MotionEvent.EDGE_RIGHT);
        }
    }

    //改变屏幕亮度
    public void setLightness(float lightness){
        WindowManager.LayoutParams layoutParams = VideoUtils.getAppCompActivity(mContext).getWindow().getAttributes();
        layoutParams.screenBrightness =layoutParams.screenBrightness+lightness/255f;
        if(layoutParams.screenBrightness>1){
            layoutParams.screenBrightness=1;
        }else if(layoutParams.screenBrightness<0.0){
            layoutParams.screenBrightness=0.0f;
        }
        VideoUtils.getAppCompActivity(mContext).getWindow().setAttributes(layoutParams);

        mPercentView.setPercent(layoutParams.screenBrightness);
        mPercentView.showByType( FloatingPercentView.TYPE_LIGHT);
    }

    //加减音量
    public void setAudio(int volume){
        AudioManager audioManager = (AudioManager) VideoUtils.getAppCompActivity(mContext).getSystemService(Context.AUDIO_SERVICE);
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

    //快进
    public void speedVideo(int mesc) {
        long pos = mManager.getCurrentPosition();
        pos += mesc; // milliseconds
        mManager.seekTo(pos);
    }

    private class VideoGestureListener implements GestureDetector.OnGestureListener {

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
            Log.d(TAG,"edge: " + e1.getEdgeFlags() +  "distance X: " + distanceX + ", distance Y :" + distanceY);
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
            return true;
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

    public void setOnFavoriteListener(OnFavoriteListener listener) {
        mFavoriteListener = listener;
        if(mCurrentScreenMode == MODE_FULL_SCREEN) {
            updateFavoriteButton();
            mFavoriteButton.setVisibility(VISIBLE);
        }
    }

    public interface OnFavoriteListener {

        boolean isFavorited();

        void onFavorite(String url);
    }

    /**
     * prepared listener
     * @param listener
     */
    public void setOnPreParedListener(OnPreparedListener listener) {
        mPreParedListener = listener;
    }

    public interface OnPreparedListener {

        void onPrepared(VideoPlayer videoPlayer);
    }

    public static class FullScreenListener implements SensorEventListener {

        private long lastAutoFullscreenTime;
        private Context mContext;
        private SensorManager mSensorManager;

        public FullScreenListener(Context context) {
            mContext = context;
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        }

        public void registerListener() {
            Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        public void unregistetListener() {
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(TuMediaPlayerManager.instance() != null) {
                VideoPlayer videoPlayer = TuMediaPlayerManager.instance().getVideoPlayer();
                if(videoPlayer != null && videoPlayer.getCurrentScreenMode() == MODE_FULL_SCREEN) {
                    final float x = event.values[SensorManager.DATA_X];
                    float y = event.values[SensorManager.DATA_Y];
                    float z = event.values[SensorManager.DATA_Z];

                    Log.d(TAG, "current x : " + x + ", current y : " + y);
                    //过滤掉用力过猛会有一个反向的大数值
                    if (((x > -15 && x < -5) || (x < 15 && x > 5)) && Math.abs(y) < 1.5) {
                        if ((System.currentTimeMillis() - lastAutoFullscreenTime) > 5000) {

                            AppCompatActivity activity =  VideoUtils.getAppCompActivity(mContext);
                            int orientation = -1;
                            if (x > 0) {
                                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            } else {
                                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                            }

                            //if orientation same to current, nothing to do
                            if(activity.getRequestedOrientation() != orientation) {
                                activity.setRequestedOrientation(orientation);
                            }

                            lastAutoFullscreenTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
