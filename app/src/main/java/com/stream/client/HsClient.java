package com.stream.client;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;

import com.hippo.yorozuya.thread.PriorityThreadFactory;
import com.stream.client.exception.CancelledException;
import com.stream.hstream.HStreamApplication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class HsClient {

    public static final String TAG = HsClient.class.getSimpleName();

    public static final int METHOD_GET_VIDEO_LIST = 1;
    public static final int METHOD_GET_VIDEO_DETAIL = 2;
    public static final int METHOD_GET_VIDEO_URL = 3;
    public static final int METHOD_GET_VIDEO_ALL = 4;
    public static final int METHOD_GET_RELEASE_INFO = 5;
    public static final int METHOD_GET_GENRE_INFO = 6;

    private ThreadPoolExecutor mRequestThreadPool;
    private OkHttpClient mOkHttpClient;

    public HsClient(Context context) {
        int poolSize = 3;

        BlockingQueue<Runnable> requestWorkQueue = new LinkedBlockingQueue<>();
        ThreadFactory factory = new PriorityThreadFactory(TAG, Process.THREAD_PRIORITY_BACKGROUND);

        mRequestThreadPool = new ThreadPoolExecutor(poolSize, poolSize,
                1L, TimeUnit.SECONDS, requestWorkQueue, factory);
        mOkHttpClient = HStreamApplication.getOkHttpClient(context);
    }

    public void execute(HsRequest request) {
        if(!request.isCancelled()) {
            Task task = new Task(request.getMethod(), request.getCallback(), request.getHsConfig());
            task.executeOnExecutor(mRequestThreadPool, request.getArgs());
            request.task = task;
        } else {
            request.getCallback().onCancel();
        }
    }

    public class Task extends AsyncTask<Object, Void, Object> {

        private int mMethod;
        private Callback mCallback;
        private HsConfig mHsConfig;

        private final AtomicReference<Call> mCall = new AtomicReference<>();

        public Task(int method, Callback callback, HsConfig hsConfig) {
            mMethod = method;
            mCallback = callback;
            mHsConfig = hsConfig;
        }

        public void setCall(Call call) {
            mCall.lazySet(call);
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                switch (mMethod) {
                    case METHOD_GET_VIDEO_LIST:
                        return HsEngine.getVideoList(this, mOkHttpClient, (String) params[0]);
                    case METHOD_GET_VIDEO_DETAIL:
                        return HsEngine.getVideoDetail(this, mOkHttpClient, (String) params[0]);
                    case METHOD_GET_VIDEO_URL:
                        //return null;
                        return HsEngine.getVideoUrl(this, mOkHttpClient, (String) params[0]);
                    case METHOD_GET_VIDEO_ALL:
                        return null;
                    case METHOD_GET_RELEASE_INFO:
                        return HsEngine.getReleaseInfo(this, mOkHttpClient, (String) params[0]);
                    case METHOD_GET_GENRE_INFO:
                        return HsEngine.getGenreInfo(this, mOkHttpClient, (String) params[0]);
                    default:
                        return new IllegalStateException("Can't detect method " + mMethod);
                }
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if (mCallback != null) {
                if (!(result instanceof CancelledException)) {
                    if (result instanceof Exception) {
                        mCallback.onFailure((Exception) result);
                    } else {
                        mCallback.onSuccess(result);
                    }
                } else {
                    // onCancel is called in stop
                }
            }

            // Clear
            mCallback = null;
            mCall.lazySet(null);
        }

        public void stop() {

        }
    }

    public interface Callback<E> {

        void onSuccess(E result);

        void onFailure(Exception e);

        void onCancel();
    }
}
