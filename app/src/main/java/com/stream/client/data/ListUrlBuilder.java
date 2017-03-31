package com.stream.client.data;

import android.nfc.Tag;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.stream.client.HsUrl;
import com.stream.network.UrlBuilder;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class ListUrlBuilder {

    private static final String TAG = ListUrlBuilder.class.getSimpleName();

    private int mPageIndex = 0;

    public int getPageIndex() {
        return mPageIndex;
    }

    public void setPageIndex(int pageIndex) {
        mPageIndex = pageIndex;
    }


    public String build() {
        UrlBuilder builder = new UrlBuilder(HsUrl.getHost());

        String buildUrl = builder.build();
        if(mPageIndex > 0) {
            buildUrl = buildUrl + HsUrl.PAGE_STREAM + mPageIndex;
        }
        Log.d(TAG, "build url :" + buildUrl);
        return buildUrl;
    }

    public String build(String url) {
        UrlBuilder builder = new UrlBuilder(url);
        return builder.build();
    }

}
