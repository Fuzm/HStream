package com.stream.videoplayerlibrary.common;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DrawableUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TimeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Fuzm on 2017/4/22 0022.
 */

public class VideoUtils {

    private static final String FAVORITE_DIR = "/sdcard/HStream/favorite/";
    private static final String FILE_PROTOCOL = "file:";

    private static final String TU_PROGRESS = "TU_PROGRESS";

    /**
     * Get activity from context object
     *
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    /**
     * Get AppCompatActivity from context
     *
     * @param context
     * @return AppCompatActivity if it's not null
     */
    public static AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void saveProgress(Context context, String url, long progress) {
        SharedPreferences preferences =
                context.getSharedPreferences(TU_PROGRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(url, progress);
        editor.apply();
    }

    public static long getSavedProgress(Context context, String url){
        SharedPreferences preferences =
                context.getSharedPreferences(TU_PROGRESS, Context.MODE_PRIVATE);
        return preferences.getLong(url, 0);
    }

    public static String saveDrawable(Drawable drawable) {
        Bitmap bitmap = drawableToBitmap(drawable);

        File dir = new File(FAVORITE_DIR);
        if(!dir.exists()) {
            dir.mkdirs();
        }

        String imagePath = FAVORITE_DIR + new Date().getTime() + ".PNG";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            imagePath = null;
        } finally {
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = null;
        }

        if(imagePath != null) {
            imagePath = FILE_PROTOCOL + imagePath;
        }
        return imagePath;
    }

    public static Drawable getDrawableFromPath(Context context, String path) {
        if(path == null || TextUtils.isEmpty(path)) {
            return null;
        } else if(path.startsWith(FILE_PROTOCOL)) {
            path = path.replace(FILE_PROTOCOL, "");
            return getDrawableFromLocalFile(context, path);
        } else {
            //暂不支持该协议
            return null;
        }
    }

    private static Drawable getDrawableFromLocalFile(Context context, String fileName) {
        Drawable drawable = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Resources resources = context.getResources();
            drawable = new BitmapDrawable(resources, bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return drawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
