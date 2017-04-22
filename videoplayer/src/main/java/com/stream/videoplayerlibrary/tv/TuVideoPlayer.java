package com.stream.videoplayerlibrary.tv;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.stream.videoplayerlibrary.R;
import com.stream.videoplayerlibrary.common.VideoUtils;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Fuzm on 2017/4/19 0019.
 */

public final class TuVideoPlayer extends FrameLayout implements View.OnClickListener{

    private static final String TAG = TuVideoPlayer.class.getSimpleName();

    private static final int MAX_PROGRESS = 1000;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;

    private static final int MODE_NORMAL_SCREEN = 1;
    private static final int MODE_FULL_SCREEN = 2;

    private Context mContext;
    private String mUrl;
    private int mCurrentState = STATE_IDLE;
    private boolean mShowing = false;
    private int mShowTime = 3000;
    private int mCurrentPosition = -1;
    private int mCurrentScreenMode = MODE_NORMAL_SCREEN;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private ViewGroup mParent;
    private VideoTextureView mVideoView;
    private AppCompatImageView mThumb;
    //part of header
    private View mVideoTop;
    private AppCompatImageView mBackButton;
    private TextView mTextTitle;
    //part of center
    private View mVideoCenter;
    private AppCompatImageView mPlayButton;
    private ProgressBar mWaitBar;
    //part of bottom
    private View mVideoBottom;
    private SeekBar mSeekBar;
    private TextView mTimeText;
    private TextView mTotalText;
    private AppCompatImageView mScreenControlButton;

    private OnClickListener mOnClickListener;

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if(mOnClickListener != null) {
                mOnClickListener.onClick(v);
            }

            if(mCurrentState != STATE_PLAYING) {
                waitUi();
                mCurrentState = STATE_PREPARING;
                openVideo();
            }
        }
    };

    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(mCurrentState == STATE_PLAYING) {
                hide();
            }
            return false;
        }
    };

    private OnTouchListener mLayoutTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    show(0);
                    break;
                case MotionEvent.ACTION_UP:
                    show(mShowTime);
                    break;
            }
            return true;
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            openVideo();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (mVideoView != null && mShowing && mVideoView.isPlaying()) {
                postDelayed(mShowProgress, MAX_PROGRESS - (pos % MAX_PROGRESS));
            }
        }
    };

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) {
                return;
            }

            int duration = mVideoView.getDuration();
            int newPosition = (duration * progress) / MAX_PROGRESS;
            mVideoView.seekTo(newPosition);
            if(mTimeText != null) {
                mTimeText.setText(stringForTime(newPosition));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            show(3600000);
            removeCallbacks(mShowProgress);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setProgress();
            post(mShowProgress);
        }
    };

    private final Runnable mBufferWait = new Runnable() {
        private boolean isWait = false;
        @Override
        public void run() {
            if(mVideoView != null) {
                int oldPosition = mCurrentPosition;
                mCurrentPosition = mVideoView.getCurrentPosition();
                if(oldPosition == mCurrentPosition && mVideoView.isPlaying()) {
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
        initView();
    }

    private TuVideoPlayer getSecondFloor() {
        TuVideoPlayer player = new TuVideoPlayer(mContext);
        player.setTitle(mTextTitle.getText());
        player.setThumb(mThumb.getDrawable());
        player.setVideoPath(mUrl);
        player.seekTo(mVideoView.getCurrentPosition());
        player.mCurrentScreenMode = MODE_FULL_SCREEN;

        return player;
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tu_video_player, this);

        //video
        mVideoView = (VideoTextureView) view.findViewById(R.id.video_textureview);
        mThumb = (AppCompatImageView) view.findViewById(R.id.thumb);
        mVideoView.setOnPreparedListener(mOnPreparedListener);

        //video header
        mVideoTop = view.findViewById(R.id.video_top);
        mBackButton = (AppCompatImageView) view.findViewById(R.id.back_button);
        mTextTitle = (TextView) view.findViewById(R.id.view_title);

        //video center
        mVideoCenter = view.findViewById(R.id.video_center);
        mPlayButton = (AppCompatImageView) view.findViewById(R.id.play_button);
        mWaitBar = (ProgressBar) view.findViewById(R.id.wait_bar);
        mVideoCenter.setOnClickListener(this);
        mVideoCenter.setOnTouchListener(mTouchListener);
        mPlayButton.setOnClickListener(this);

        //video bottom
        mVideoBottom = view.findViewById(R.id.video_bottom);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mTimeText = (TextView) view.findViewById(R.id.time_text);
        mTotalText = (TextView) view.findViewById(R.id.total_time_text);
        mScreenControlButton = (AppCompatImageView) view.findViewById(R.id.screen_control);

        mSeekBar.setMax(MAX_PROGRESS);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mScreenControlButton.setOnClickListener(this);

        //setUiVisiable(VISIBLE, VISIBLE, VISIBLE, GONE, GONE);
        changeUiForInit();
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        this.setOnTouchListener(mLayoutTouchListener);
    }

    public void setTitle(CharSequence title) {
        mTextTitle.setText(title);
    }

    public void setThumb(Drawable drawable) {
        mThumb.setImageDrawable(drawable);
    }

    public AppCompatImageView getThumb(){
        return mThumb;
    }

    public void seekTo(int posi) {
        mVideoView.seekTo(posi);
    }

    public void setVideoPath(String url) {
        mUrl = url;
        mVideoView.setVideoPath(url);
        openVideo();
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

    private void openVideo() {
        if(checkPrepared()) {
            mVideoView.start();
            mCurrentState = STATE_PLAYING;
            show(3000);
            post(mBufferWait);
        } else {

        }
    }

    private void waitUi() {
        if(mCurrentState == STATE_PLAYING) {
            //setUiVisiable(GONE, VISIBLE, GONE, VISIBLE, VISIBLE);
            changeUiForWaitBuffer();
        } else {
            //setUiVisiable(VISIBLE, VISIBLE, GONE, VISIBLE, GONE);
            changeUiForWaitStart();
        }
    }

    private void updatePlayButton() {
        if(mVideoView.isPlaying()) {
            mPlayButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPlayButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    private void show(int timeout) {
        mShowing = true;
        //setUiVisiable(GONE, VISIBLE, VISIBLE, GONE, VISIBLE);
        changeUiForShow();
        updatePlayButton();
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
        return (mVideoView.isPrepared() &&
                mCurrentState == STATE_PREPARING &&
                mUrl != null);
    }

    private int setProgress() {
        if(mVideoView == null) {
            return 0;
        }

        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        if(mSeekBar != null) {
            if(duration > 0) {
                int pos = MAX_PROGRESS * position / duration;
                mSeekBar.setProgress(pos);
            }
            int percent = mVideoView.getBufferPercentage();
            int second = percent * MAX_PROGRESS / 100;
            mSeekBar.setSecondaryProgress(second);
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

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void reset() {
        mVideoView.pause();
        updatePlayButton();

        if(mCurrentState == STATE_PLAYING) {
            mCurrentState = STATE_PREPARING;
        } else {
            mCurrentState = STATE_IDLE;
        }

        removeCallbacks(mBufferWait);
        removeCallbacks(mShowProgress);
        removeCallbacks(mFateOut);

        changeUiForInit();
    }

    private void changeUiForInit() {
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

    public void startFullScreen() {
        AppCompatActivity activity =  VideoUtils.getAppCompActivity(mContext);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

        Window window = activity.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TuVideoPlayer fullPlayer = new TuVideoPlayer(mContext);
        fullPlayer.setTitle(mTextTitle.getText());
        fullPlayer.setThumb(mThumb.getDrawable());
        fullPlayer.setVideoPath(mUrl);
        fullPlayer.seekTo(mVideoView.getCurrentPosition());
        fullPlayer.mCurrentScreenMode = MODE_FULL_SCREEN;

        window.addContentView(fullPlayer, lp);
        fullPlayer.mPlayButton.performClick();
        fullPlayer.updateSrceenControlView(mCurrentScreenMode);
    }

    public void endFullScreen() {
        AppCompatActivity activity = VideoUtils.getAppCompActivity(mContext);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ViewGroup parent = (ViewGroup) this.getParent();
        parent.removeView(this);
        updateSrceenControlView(mCurrentScreenMode);
    }

    public void updateSrceenControlView(int mode){
        if(mode == MODE_FULL_SCREEN) {
            mScreenControlButton.setImageResource(R.drawable.ic_screen_normal);
        } else {
            mScreenControlButton.setImageResource(R.drawable.ic_screen_full);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.screen_control) {

            if(mCurrentScreenMode == MODE_NORMAL_SCREEN) {
                startFullScreen();
            } else {
                endFullScreen();
            }

        } else if(v.getId() == R.id.video_center) {
            if(mOnClickListener != null) {
                mOnClickListener.onClick(v);
            }

            if(mCurrentState != STATE_PLAYING) {
                waitUi();
                mCurrentState = STATE_PREPARING;
                openVideo();
            }

        } else if(v.getId() == R.id.play_button) {
            if(mCurrentState == STATE_PLAYING) {
                if(mVideoView.isPlaying()) {
                    mVideoView.pause();
                    removeCallbacks(mFateOut);
                    mShowTime = 0;
                } else {
                    mVideoView.start();
                    mShowTime = 3000;
                }
                show(mShowTime);
                updatePlayButton();
            } else {
                onClick(mVideoCenter);
            }
        }
    }
}
