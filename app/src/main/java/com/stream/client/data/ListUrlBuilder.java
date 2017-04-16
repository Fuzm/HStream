package com.stream.client.data;

import android.nfc.Tag;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.stream.client.HsUrl;
import com.stream.network.UrlBuilder;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class ListUrlBuilder implements Cloneable, Parcelable{

    private static final String TAG = ListUrlBuilder.class.getSimpleName();
    private static final String KEY_PAGE_INDEX = "page_index";
    private static final String KEY_KEYWORD = "keyword";

    private int mPageIndex = 0;
    private String mKeyword = null;

    public ListUrlBuilder() {
    }

    protected ListUrlBuilder(Parcel in) {
        mPageIndex = in.readInt();
        mKeyword = in.readString();
    }

    public void reset() {
        mPageIndex = 0;
        mKeyword = null;
    }

    public int getPageIndex() {
        return mPageIndex;
    }

    public void setPageIndex(int pageIndex) {
        mPageIndex = pageIndex;
    }

    public void setKeyword(String query) {
        mKeyword = query;
    }

    public String getKeyword(){
        return mKeyword;
    }

    public String build() {
        UrlBuilder builder = new UrlBuilder(HsUrl.getHost());

        String buildUrl = builder.build();
        if(mPageIndex > 0) {
            buildUrl = buildUrl + HsUrl.PAGE_STREAM + mPageIndex;
        }

        if(!TextUtils.isEmpty(mKeyword)) {
            buildUrl = buildUrl + HsUrl.SEARCH_STREAM + mKeyword;
        }

        Log.d(TAG, "build url :" + buildUrl);
        return buildUrl;
    }

    public String build(String url) {
        UrlBuilder builder = new UrlBuilder(url);
        return builder.build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPageIndex);
        dest.writeString(mKeyword);
    }

    public static final Creator<ListUrlBuilder> CREATOR = new Creator<ListUrlBuilder>() {
        @Override
        public ListUrlBuilder createFromParcel(Parcel in) {
            return new ListUrlBuilder(in);
        }

        @Override
        public ListUrlBuilder[] newArray(int size) {
            return new ListUrlBuilder[size];
        }
    };
}
