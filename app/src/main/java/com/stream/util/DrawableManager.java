package com.stream.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatDrawableManager;

/**
 * Created by Fuzm on 2017/4/9 0009.
 */

public final class DrawableManager {

    public static final AppCompatDrawableManager sManager = new AppCompatDrawableManager();

    public static Drawable getDrawable(Context context, int resId) {
        return sManager.getDrawable(context, resId);
    }
}
