package com.stream.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.stream.hstream.R;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Fuzm on 2017/4/17 0017.
 */

public class MediaControl extends FrameLayout {

    private static final String TAG = StreamVideoView.class.getSimpleName();
    private static final int sDefaultTimeout = 3000;
    private static final int MAX_PROGRESS = 1000;

    private View mAnchor;
    private View mHeader;
    private View mCenter;
    private View mBottom;
    private View mDecor;
    private WindowManager mWindowManager;
    private Window mWindow;
    private MediaController.MediaPlayerControl mPlayer;

    private LinearLayout mViewHeader;
    private ImageView mBackButton;
    private TextView mTitleView;

    private FrameLayout mViewCenter;
    private ImageView mPlayButton;
    private ProgressBar mWaitBar;

    private LinearLayout mViewBottom;
    private AppCompatSeekBar mSeekBar;
    private TextView mTimeView;
    private TextView mTotalTimeView;

    private Context mContext;
    private boolean mShowing = false;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private int mCurrentPosition = -1;
    private StreamVideoView.OnBackClickListener mOnBackClickListener;
    private OnClickListener mOnPlayButtonClick;

    private OnClickListener mPlayButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mOnPlayButtonClick != null) {
                mOnPlayButtonClick.onClick(v);
            }

            if(mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    show();
                } else {
                    mPlayer.start();
                    show(sDefaultTimeout);
                }
                updatePlayButton();
            }
        }
    };

    private OnClickListener mBackButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mOnBackClickListener.onClick(v);
        }
    };

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) {
                return;
            }

            int duration = mPlayer.getDuration();
            int newPosition = (duration * progress) / MAX_PROGRESS;
            mPlayer.seekTo(newPosition);
            if(mTimeView != null) {
                mTimeView.setText(stringForTime(newPosition));
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

    // This is called whenever mAnchor's layout bound changes
    private final OnLayoutChangeListener mLayoutChangeListener =new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right,
                                   int bottom, int oldLeft, int oldTop, int oldRight,
                                   int oldBottom) {
            //updateFloatingWindowLayout();
        }
    };


    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (mPlayer != null && mShowing && mPlayer.isPlaying()) {
                //Log.d(TAG, "progress delayed time: " + (MAX_PROGRESS - (pos % MAX_PROGRESS)));
                postDelayed(mShowProgress, MAX_PROGRESS - (pos % MAX_PROGRESS));
            }
        }
    };

    private final Runnable mBufferWait = new Runnable() {

        @Override
        public void run() {
            if(mPlayer != null) {
                int oldPosition = mCurrentPosition;
                mCurrentPosition = mPlayer.getCurrentPosition();
                if(oldPosition == mCurrentPosition && mPlayer.isPlaying()) {
                    showWaitBar();
                } else {
                    hideWaitBar();
                }

                postDelayed(this, MAX_PROGRESS - (mCurrentPosition % MAX_PROGRESS));
            }
        }
    };

    public MediaControl(Context context) {
        this(context, null);
    }

    public MediaControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View center = initControllerViewCenter(inflate);
        View header = initControllerViewHeader(inflate);

        FrameLayout.LayoutParams headerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        addView(header, headerParams);
        addView(center, centerParams);
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        if (mAnchor != null) {
            mAnchor.removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mAnchor = view;
        if (mAnchor != null) {
            mAnchor.addOnLayoutChangeListener(mLayoutChangeListener);
        }

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        frameParams.gravity = Gravity.BOTTOM;

        //removeAllViews();
        View v = makeControllerView();
        v.setVisibility(GONE);
        addView(v, frameParams);
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBottom = initControllerViewBottom(inflate);
        return mBottom;
    }

    private View initControllerViewBottom(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.widget_media_control_bottom, null);

        // part of bottom
        mViewBottom = (LinearLayout) view.findViewById(R.id.view_bottom);
        mTimeView = (TextView) mViewBottom.findViewById(R.id.time_text);
        mTotalTimeView = (TextView) mViewBottom.findViewById(R.id.total_time_text);
        mSeekBar = (AppCompatSeekBar) mViewBottom.findViewById(R.id.seek_bar);
        mSeekBar.setMax(MAX_PROGRESS);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        return view;
    }

    private View initControllerViewCenter(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.widget_media_control_center, null);

        // part of center
        mViewCenter = (FrameLayout) view.findViewById(R.id.view_center);
        mPlayButton = (ImageView) mViewCenter.findViewById(R.id.play_button);
        mWaitBar = (ProgressBar) mViewCenter.findViewById(R.id.wait_bar);
        mPlayButton.setOnClickListener(mPlayButtonClick);

        return view;
    }

    private View initControllerViewHeader(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.widget_media_control_header, null);

        // part of header
        mViewHeader = (LinearLayout) view.findViewById(R.id.view_header);
        mBackButton = (ImageView) mViewHeader.findViewById(R.id.back_button);
        mTitleView = (TextView) mViewHeader.findViewById(R.id.view_title);
        mBackButton.setOnClickListener(mBackButtonClick);

        return view;
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setTitleBarShow(boolean visibility) {
        //mTitleBarShow = visibility;
        mViewHeader.setVisibility(VISIBLE);
    }

    public void setOnPlayButtonClick(OnClickListener listener) {
        mOnPlayButtonClick = listener;
    }

    public void addBackButtonListener(StreamVideoView.OnBackClickListener listener) {
        mBackButton.setVisibility(VISIBLE);
        mOnBackClickListener = listener;
    }

    private void updatePlayButton() {
        if(mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayButton.setImageResource(R.drawable.ic_media_pause);
            } else {
                mPlayButton.setImageResource(R.drawable.ic_media_play);
            }
        }
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void show(int timeout) {
        if (mViewHeader == null || mViewBottom == null) {
            return;
        }
        if (!mShowing) {
            mShowing = true;
            this.setVisibility(VISIBLE);
            mViewBottom.setVisibility(VISIBLE);
        }
        updatePlayButton();
        post(mShowProgress);
        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    protected void hide() {
//        if (mViewHeader == null || mViewBottom == null) {
//            return;
//        }

        if (mShowing) {
            removeCallbacks(mShowProgress);

            mShowing = false;
//            mViewHeader.setVisibility(GONE);
//            mViewBottom.setVisibility(GONE);
            this.setVisibility(GONE);
        }
    }

    protected boolean isShowing() {
        return mShowing;
    }

    public int setProgress() {
        if(mPlayer == null) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if(mSeekBar != null) {
            if(duration > 0) {
                int pos = MAX_PROGRESS * position / duration;
                mSeekBar.setProgress(pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * (MAX_PROGRESS / 100));
        }

        if(mTotalTimeView != null) {
            mTotalTimeView.setText(stringForTime(duration));
        }

        if(mTimeView != null) {
            mTimeView.setText(stringForTime(position));
        }

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(mPlayer == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mShowing) {
                    hide();
                } else {
                    show(0); // show until hide is called
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mShowing) { //if already execute hide, don't show;
                    show(sDefaultTimeout); // start timeout
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    public void showWaitBar() {
        if (mWaitBar != null) {
            mWaitBar.setVisibility(VISIBLE);
            mPlayButton.setVisibility(GONE);

            mViewCenter.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }

    public void hideWaitBar() {
        if(mWaitBar != null) {
            mWaitBar.setVisibility(GONE);
            mPlayButton.setVisibility(VISIBLE);

            mViewCenter.setOnTouchListener(null);
        }
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

    public interface OnBackClickListener {
        void onClick(View view);
    }


}
