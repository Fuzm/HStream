package com.stream.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/4/13 0013.
 */

public class FloatingPercentView extends FrameLayout {

    public static final int TYPE_VOLUMN = 0x0001;
    public static final int TYPE_LIGHT = 0x0002;

    private static final int MAX = 100;
    private static final int DEFAULT_TIME_OUT = 3000;

    private ImageView mTitleView;
    private TextView mPercentText;
    private ProgressBar mPercentBar;

    private Runnable mFadeout = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public FloatingPercentView(Context context) {
        super(context);
        init(context);
    }

    public FloatingPercentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatingPercentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_volumn_view, this);

        mTitleView = (ImageView) view.findViewById(R.id.title_image_view);
        mPercentText = (TextView) view.findViewById(R.id.percent_text);
        mPercentBar = (ProgressBar) view.findViewById(R.id.percent_bar);

        mPercentBar.setMax(MAX);
    }

    public void setPercent(float percent) {
        mPercentBar.setProgress(Math.round(percent * MAX));
        mPercentText.setText(Math.round(percent * MAX) + "%");
    }

    private void setTitleDrawable(Drawable drawable) {
        mTitleView.setImageDrawable(drawable);
    }

    public void showByType(int type) {
        show(getDefaultRes(type), DEFAULT_TIME_OUT);
    }

    public void show(int resId) {
        show(resId, DEFAULT_TIME_OUT);
    }

    public void show(int resId, int timeout) {
        Drawable drawable = mTitleView.getDrawable();
        Drawable currentDrawable = getDrawable(resId);
        if(drawable == null || drawable != currentDrawable) {
            setTitleDrawable(getDrawable(resId));
        }

        try {
            removeCallbacks(mFadeout);
        } catch (Exception e) {}
        setVisibility(VISIBLE);

        if(timeout != 0) {
            postDelayed(mFadeout, timeout);
        }
    }

    private void hide() {
        setVisibility(GONE);
    }

    public int getDefaultRes(int type) {
        int resId = 0;
        if(type == TYPE_LIGHT) {
            resId = R.drawable.ic_view_light;
        } else if(type == TYPE_VOLUMN){
            resId = R.drawable.ic_view_volumn;
        } else {
            //throw new Exception("not type can get");
        }

        return resId;
    }

    private Drawable getDrawable(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resId, null);
        }

        return getResources().getDrawable(resId);
    }


}
