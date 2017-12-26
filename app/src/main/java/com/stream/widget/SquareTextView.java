package com.stream.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Seven-one on 2017/12/26 0026.
 */

public class SquareTextView extends TextView {

    public SquareTextView(Context context) {
        super(context);
        init(context);
    }

    public SquareTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.post(new Runnable() {
            @Override
            public void run() {
                int width = getWidth();
                int height = getHeight();

                int size = Math.max(width, height);
                SquareTextView.this.setLayoutParams(new FrameLayout.LayoutParams(size , size));
            }
        });
    }
}
