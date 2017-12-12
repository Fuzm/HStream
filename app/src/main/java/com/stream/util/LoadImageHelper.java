package com.stream.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.Unikery;
import com.hippo.image.Image;
import com.hippo.image.ImageBitmap;
import com.hippo.image.ImageDrawable;
import com.stream.drawable.PreciselyClipDrawable;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/4/14 0014.
 */

public class LoadImageHelper {

    private static final String TAG = LoadImageHelper.class.getSimpleName();

    private Context mContext;
    private Drawable mDrawable;
    private ImageView mImageView;
    private HelperUnikery mUnikery;
    private Conaco<ImageBitmap> mConaco;

    private LoadImageHelper(Context context) {
        mContext = context;
        mUnikery = new HelperUnikery();
        mConaco = HStreamApplication.getConaco(mContext);
    }

    public static LoadImageHelper with(Context context) {
        return new LoadImageHelper(context);
    }

    public LoadImageHelper load(String key, final String url)  {
        ConacoTask.Builder<ImageBitmap> builder = new ConacoTask.Builder<ImageBitmap>()
                .setUnikery(mUnikery)
                .setKey(key)
                .setUrl(url)
                .setDataContainer(null)
                .setUseNetwork(true)
                .setUseMemoryCache(false);
        mConaco.load(builder);
        return this;
    }

    public LoadImageHelper into(ImageView view) {
        if(mDrawable != null) {
            view.setImageDrawable(mDrawable);
        }
        mImageView = view;
        return this;
    }

    public void cancel() {
        mConaco.cancel(mUnikery);
        mDrawable = null;
        mImageView = null;
    }

    private ImageDrawable getImageDrawable() {
        Drawable drawable = mImageView.getDrawable();
        if (drawable instanceof TransitionDrawable) {
            TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
            if (transitionDrawable.getNumberOfLayers() == 2) {
                drawable = transitionDrawable.getDrawable(1);
            }
        }
        if (drawable instanceof PreciselyClipDrawable) {
            drawable = ((PreciselyClipDrawable) drawable).getWrappedDrawable();
        }
        if (drawable instanceof ImageDrawable) {
            return (ImageDrawable) drawable;
        } else {
            return null;
        }
    }

    private void clearDrawable() {
        // Recycle ImageDrawable
        ImageDrawable imageDrawable = getImageDrawable();
        if (imageDrawable != null) {
            imageDrawable.recycle();
        }

        // Set drawable null
        mImageView.setImageDrawable(null);
    }


    private class HelperUnikery implements Unikery<ImageBitmap> {

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
            Log.d(TAG, "Task id: " + mTaskId + " miss in " + source);
        }

        @Override
        public void onRequest() {
            Log.d(TAG, "Task id: " + mTaskId + " request");
        }

        @Override
        public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
            Log.d(TAG, "Task id: " + mTaskId + ", received Size: " + receivedSize + ", total size:" + totalSize);
        }

        @Override
        public void onWait() {
            Log.d(TAG, "Task id: " + mTaskId + " wait");
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

            clearDrawable();

            if ((source == Conaco.SOURCE_DISK || source == Conaco.SOURCE_NETWORK) && mImageView.isShown()) {
                Drawable[] layers = new Drawable[2];
                layers[0] = new ColorDrawable(Color.TRANSPARENT);
                layers[1] = drawable;
                TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                mImageView.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(300);
            } else {
                mImageView.setImageDrawable(drawable);
            }

            mDrawable = drawable;
            return true;
        }

        @Override
        public void onFailure() {
            Log.d(TAG, "Task id: " + mTaskId + " failure");
            clearDrawable();
            mDrawable = null;
            mImageView = null;
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "Task id: " + mTaskId + " cancel");
            mDrawable = null;
            mImageView = null;
        }
    }
}
