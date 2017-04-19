package com.stream.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.Unikery;
import com.hippo.image.ImageBitmap;
import com.hippo.image.ImageDrawable;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/4/18 0018.
 */

public class ListVideoView extends VideoTextureView {

    private static final String TAG = ListVideoView.class.getSimpleName();

    private Conaco<ImageBitmap> mConaco;
    private StreamUnikery mStreamUnikery;

    public ListVideoView(Context context) {
        this(context, null);
    }

    public ListVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) {
            mConaco = HStreamApplication.getConaco(context);
        }
        mStreamUnikery = new StreamUnikery(this);
    }

    public void setBackground(String key, String url) {
        setBackground(key, url, true);
    }

    public void setBackground(String key, String url, boolean useNetwork) {
        if (url == null || key == null) {
            return;
        }

        //unload();
        ConacoTask.Builder<ImageBitmap> builder = new ConacoTask.Builder<ImageBitmap>()
                .setUnikery(mStreamUnikery)
                .setKey(key)
                .setUrl(url)
                //.setDataContainer(container)
                .setUseNetwork(useNetwork);
        mConaco.load(builder);
    }

    private void unload() {
        mConaco.cancel(mStreamUnikery);
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
                if(!isInEditMode()) {
                    //mView.setBackground(drawable);
                    mView.setBackground(getResources().getDrawable(R.drawable.ic_media_play));
                }

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
}
