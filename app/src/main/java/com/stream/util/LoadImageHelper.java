package com.stream.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.Unikery;
import com.hippo.image.ImageBitmap;
import com.hippo.image.ImageDrawable;
import com.stream.hstream.HStreamApplication;

/**
 * Created by Fuzm on 2017/4/14 0014.
 */

public class LoadImageHelper {

    private static final String TAG = LoadImageHelper.class.getSimpleName();

    public static void loadImage(Context context, String key, String url, final View view)  {
        Unikery<ImageBitmap> unikery = new Unikery<ImageBitmap>() {

            private int mTaskId = Unikery.INVALID_ID;

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
                    view.setBackground(drawable);
                }
                return true;
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onCancel() {

            }
        };
        Conaco<ImageBitmap> mConaco = HStreamApplication.getConaco(context);
        ConacoTask.Builder<ImageBitmap> builder = new ConacoTask.Builder<ImageBitmap>()
                .setUnikery(unikery)
                .setKey(key)
                .setUrl(url)
                .setDataContainer(null)
                .setUseNetwork(true);
        mConaco.load(builder);
    }
}