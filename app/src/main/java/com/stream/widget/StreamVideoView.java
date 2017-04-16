package com.stream.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.Unikery;
import com.hippo.image.ImageBitmap;
import com.hippo.image.ImageDrawable;
import com.stream.drawable.PreciselyClipDrawable;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Fuzm on 2017/4/10 0010.
 */

public class StreamVideoView extends FrameLayout {

    private static final String TAG = StreamVideoView.class.getSimpleName();
    private static final int sDefaultTimeout = 3000;
    private static final int MAX_PROGRESS = 1000;

    public static final int VIEW_MODE_SMALL = 0X4000;
    public static final int VIEW_MODE_LARGE = 0x5000;

    private VideoView mVideoView;
    private LinearLayout mViewHeader;
    private ImageView mBackButton;
    private TextView mTitleView;
    private LinearLayout mViewBottom;
    private ImageView mPlayButton;
    private ImageView mCenterPlayButton;
    private AppCompatSeekBar mSeekBar;
    private ProgressBar mWaitBar;
    private TextView mTimeView;
    private TextView mTotalTimeView;

    private boolean isPrepared = false;
    private boolean mShowing = false;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private int mCurrentPosition = -1;
    private OnBackClickListener mOnBackClickListener;
    private StreamUnikery<VideoView> mUnikery;

    private boolean mTitleBarShow = true;
    private int mCurrentMode = VIEW_MODE_LARGE;

    private OnClickListener mPlayButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            updatePlayButton();
            show(sDefaultTimeout);
        }
    };

    private OnClickListener mCenterPlayButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            updateCenterPlayButton();
            if(mVideoView.isPlaying()) {
                show(sDefaultTimeout);
            } else {
                removeCallbacks(mFadeOut);
                show(0);
            }
        }
    };

    private OnClickListener mBackButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mOnBackClickListener.onClick(v);
        }
    };

    private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) {
                return;
            }

            int duration = mVideoView.getDuration();
            int newPosition = (duration * progress) / MAX_PROGRESS;
            mVideoView.seekTo(newPosition);
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
            if (mVideoView != null && mShowing && mVideoView.isPlaying()) {
                //Log.d(TAG, "progress delayed time: " + (MAX_PROGRESS - (pos % MAX_PROGRESS)));
                postDelayed(mShowProgress, MAX_PROGRESS - (pos % MAX_PROGRESS));
            }
        }
    };

    private final Runnable mBufferWait = new Runnable() {

        @Override
        public void run() {
            if(mVideoView != null) {
                int oldPosition = mCurrentPosition;
                mCurrentPosition = mVideoView.getCurrentPosition();
                if(oldPosition == mCurrentPosition && mVideoView.isPlaying()) {
                    showWaitBar();
                } else {
                    hideWaitBar();
                }

                postDelayed(this, MAX_PROGRESS - (mCurrentPosition % MAX_PROGRESS));
            }
        }
    };

    public StreamVideoView(Context context) {
        super(context);
        init(context);
    }

    public StreamVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StreamVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_video_view, this);

        mVideoView = (VideoView) view.findViewById(R.id.view_video);
        mWaitBar = (ProgressBar) view.findViewById(R.id.wait_bar);
//        if(mWaitBar != null) {
//            showWaitBar();
//        }

        mViewHeader = (LinearLayout) view.findViewById(R.id.view_header);
        mBackButton = (ImageView) mViewHeader.findViewById(R.id.back_button);
        mTitleView = (TextView) mViewHeader.findViewById(R.id.view_title);

        mViewBottom = (LinearLayout) view.findViewById(R.id.view_bottom);
        mPlayButton = (ImageView) mViewBottom.findViewById(R.id.play_button);
        mCenterPlayButton = (ImageView) view.findViewById(R.id.center_play_button);
        mTimeView = (TextView) mViewBottom.findViewById(R.id.time_text);
        mTotalTimeView = (TextView) mViewBottom.findViewById(R.id.total_time_text);

        mSeekBar = (AppCompatSeekBar) mViewBottom.findViewById(R.id.seek_bar);
        mSeekBar.setMax(MAX_PROGRESS);

        mPlayButton.setOnClickListener(mPlayButtonClick);
        mCenterPlayButton.setOnClickListener(mCenterPlayButtonClick);
        mBackButton.setOnClickListener(mBackButtonClick);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mUnikery = new StreamUnikery<>(mVideoView);
        mVideoView.setDrawingCacheEnabled(true);
    }

    public void setMode(int mode) {
        if(mode == VIEW_MODE_SMALL) {
            mPlayButton.setVisibility(GONE);
        } else if(mode == VIEW_MODE_LARGE) {
            mPlayButton.setVisibility(VISIBLE);
        } else {
            Log.d(TAG, "not support mode");
            return;
        }

        mCurrentMode = mode;
    }

    public void setVideoPath(String path) {
        mVideoView.setVideoPath(path);
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setZOrderOnTop(boolean onTop) {
        mVideoView.setZOrderOnTop(onTop);
    }

    public void start() {
        if(mWaitBar != null) {
            showWaitBar();
        }

        if(!mVideoView.isPlaying()) {
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isPrepared = true;
                    mVideoView.setBackground(null);
                    setBackground(null);
                    hideWaitBar();
                    //mVideoView.start();
                    show(sDefaultTimeout);

                    if(mCurrentMode == VIEW_MODE_SMALL) {
                        updateCenterPlayButton();
                    } else {
                        updatePlayButton();
                    }

                    post(mBufferWait);
                }
            });
        }
    }

    public void pause() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    public void resume(){
        mVideoView.resume();
    }

    public void reset(){
        resume();
        isPrepared = false;
        setTitle(null);
    }

    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    public void seekTo(int msec) {
        mVideoView.seekTo(msec);
        show();
    }

    public void setTitleBarShow(boolean visibility) {
        mTitleBarShow = visibility;
    }

    public void release() {
        mVideoView = null;
    }

    public void addBackButtonListener(OnBackClickListener listener) {
        mBackButton.setVisibility(VISIBLE);
        mOnBackClickListener = listener;
    }

    private void updatePlayButton() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mPlayButton.setImageResource(R.drawable.ic_media_play);
        } else {
            mVideoView.start();
            mVideoView.setFocusable(true);
            mPlayButton.setImageResource(R.drawable.ic_media_pause);
        }
    }

    private void updateCenterPlayButton() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mCenterPlayButton.setImageResource(R.drawable.ic_media_play);
        } else {
            mVideoView.start();
            mVideoView.setFocusable(true);
            mCenterPlayButton.setImageResource(R.drawable.ic_media_pause);
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
            if(mTitleBarShow) {
                mViewHeader.setVisibility(VISIBLE);
            }
            if(mCurrentMode == VIEW_MODE_SMALL) {
                mCenterPlayButton.setVisibility(VISIBLE);
            }
            mViewBottom.setVisibility(VISIBLE);
        }

        post(mShowProgress);

        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    private void hide() {
        if (mViewHeader == null || mViewBottom == null) {
            return;
        }

        if (mShowing) {
            removeCallbacks(mShowProgress);

            mShowing = false;
            mViewHeader.setVisibility(GONE);
            mCenterPlayButton.setVisibility(GONE);
            mViewBottom.setVisibility(GONE);
        }
    }

    public int setProgress() {
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

    public void loadBackground(Drawable drawable) {
        mVideoView.setBackground(drawable);
    }

    public void loadBackground(String key, String url) {
        Conaco<ImageBitmap> mConaco = HStreamApplication.getConaco(getContext());
        ConacoTask.Builder<ImageBitmap> builder = new ConacoTask.Builder<ImageBitmap>()
                .setUnikery(mUnikery)
                .setKey(key)
                .setUrl(url)
                .setDataContainer(null)
                .setUseNetwork(true);
        mConaco.load(builder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //not prepare;
        if(!isPrepared){
            return true;
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
        }
    }

    public void hideWaitBar() {
        if(mWaitBar != null) {
            mWaitBar.setVisibility(GONE);
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

    public class StreamUnikery<E extends View> implements Unikery<ImageBitmap> {

        private int mTaskId = Unikery.INVALID_ID;
        private E mView;

        public StreamUnikery(E view) {
            mView = view;
        }

        @Override
        public void setTaskId(int id) {
            mTaskId = id;
        }

        @Override
        public int getTaskId() {
            return mTaskId;
        }

        @Override
        public void onMiss(int source) {

        }

        @Override
        public void onRequest() {

        }

        @Override
        public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {

        }

        @Override
        public void onWait() {

        }

        @Override
        public boolean onGetValue(@NonNull ImageBitmap value, int source) {
            Drawable drawable;
            try {
                drawable = new ImageDrawable(value);
            } catch (Exception e) {
                Log.d(TAG, "cannot get drawable");
                return false;
            }

            if(null != drawable) {
                mView.setBackground(drawable);
            }
            return true;
        }

        @Override
        public void onFailure() {

        }

        @Override
        public void onCancel() {

        }
    }

    public interface OnBackClickListener {
        void onClick(View view);
    }

}
