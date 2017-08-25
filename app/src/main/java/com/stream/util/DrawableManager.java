package com.stream.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatDrawableManager;

/**
 * Created by Fuzm on 2017/4/9 0009.
 */

public final class DrawableManager {

    public static final AppCompatDrawableManager sManager = AppCompatDrawableManager.get();

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        return sManager.getDrawable(context, resId);
    }
}
