package com.stream.client.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoSourceInfo implements Parcelable{

    public String name;
    public String url;
    public String videoUrl;

    public VideoSourceInfo() {
    }

    protected VideoSourceInfo(Parcel in) {
        name = in.readString();
        url = in.readString();
        videoUrl = in.readString();
    }

    public Map parseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("url", url);
        map.put("videoUrl", videoUrl);
        return map;
    }

    public static final Creator<VideoSourceInfo> CREATOR = new Creator<VideoSourceInfo>() {
        @Override
        public VideoSourceInfo createFromParcel(Parcel in) {
            return new VideoSourceInfo(in);
        }

        @Override
        public VideoSourceInfo[] newArray(int size) {
            return new VideoSourceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(videoUrl);
    }
}
