package com.stream.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class SafeCoordinatorLayout extends CoordinatorLayout {

    public SafeCoordinatorLayout(Context context) {
        super(context);
    }

    public SafeCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
