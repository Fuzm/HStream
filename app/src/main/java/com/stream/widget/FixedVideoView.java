package com.stream.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Fuzm on 2017/4/12 0012.
 */

public class FixedVideoView extends VideoView {

    public FixedVideoView(Context context) {
        super(context);
    }

    public FixedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getWidth(), widthMeasureSpec);
        int height = getDefaultSize(getHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
