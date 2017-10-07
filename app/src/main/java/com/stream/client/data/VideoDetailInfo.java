package com.stream.client.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Seven-one on 2017/10/6.
 */

public class VideoDetailInfo implements Parcelable {

    private String alternativeName;

    public VideoDetailInfo() {}

    protected VideoDetailInfo(Parcel in) {
        alternativeName = in.readString();
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alternativeName);
    }

    public static final Creator<VideoDetailInfo> CREATOR = new Creator<VideoDetailInfo>() {
        @Override
        public VideoDetailInfo createFromParcel(Parcel in) {
            return new VideoDetailInfo(in);
        }

        @Override
        public VideoDetailInfo[] newArray(int size) {
            return new VideoDetailInfo[size];
        }
    };

}
