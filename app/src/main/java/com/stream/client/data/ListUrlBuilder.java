package com.stream.client.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.stream.client.HsUrl;
import com.stream.enums.GenreEnum;
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
    private GenreEnum mGenreEnum;

    public ListUrlBuilder(GenreEnum genreEnum) {
        mGenreEnum = genreEnum;
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
        return new UrlBuilder(HsUrl.getPageUrl(mGenreEnum, mPageIndex, mKeyword)).build();
    }

    public String build(String url) {
        return new UrlBuilder(url).build();
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
