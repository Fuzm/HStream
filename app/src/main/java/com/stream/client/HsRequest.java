package com.stream.client;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class HsRequest {

    private int mMethod;
    private Object[] mArgs;
    private HsClient.Callback mCallback;
    private HsConfig mHsConfig;

    HsClient.Task task;

    private boolean mCancel = false;

    public HsRequest setMethod(int method) {
        mMethod = method;
        return this;
    }

    public HsRequest setArgs(Object... args) {
        mArgs = args;
        return this;
    }

    public HsRequest setCallback(HsClient.Callback callback) {
        mCallback = callback;
        return this;
    }

    public int getMethod() {
        return mMethod;
    }

    public Object[] getArgs() {
        return mArgs;
    }

    public HsClient.Callback getCallback() {
        return mCallback;
    }

    public HsConfig getHsConfig() {
        return mHsConfig;
    }

    public void cancel() {
        if (!mCancel) {
            mCancel = true;
            if (task != null) {
                task.stop();
                task = null;
            }
        }
    }

    public boolean isCancelled() {
        return mCancel;
    }
}
