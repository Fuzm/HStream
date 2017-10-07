package com.stream.client;

import android.support.v4.app.Fragment;

import com.stream.client.HsClient;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public abstract class HsCallback <E extends Fragment, T> implements HsClient.Callback<T> {

    private E mFragment;

    public HsCallback(E fragment) {
        mFragment = fragment;
    }

    public E getFragment() {
        return mFragment;
    }

}
