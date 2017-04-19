package com.stream.client.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoInfo implements Parcelable{

    public String token;
    public String title;
    public String thumb;
    public String url;
    public List<VideoSourceInfo>  mSourceInfoList;

    public VideoInfo() {}

    protected VideoInfo(Parcel in) {
        this.token = in.readString();
        this.title = in.readString();
        this.thumb = in.readString();
        this.url = in.readString();
        this.mSourceInfoList = in.readArrayList(VideoSourceInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.token);
        dest.writeString(this.title);
        dest.writeString(this.thumb);
        dest.writeString(this.url);
        dest.writeList(mSourceInfoList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
}
