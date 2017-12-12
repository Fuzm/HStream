package com.stream.client.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Seven-one on 2017/10/6.
 */

public class VideoDetailInfo implements Parcelable {

    private String detailUrl;
    private String alternativeName;
    private String offeringDate;

    public VideoDetailInfo() {}

    protected VideoDetailInfo(Parcel in) {
        alternativeName = in.readString();
        detailUrl = in.readString();
        offeringDate = in.readString();
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getOfferingDate() {
        return offeringDate;
    }

    public void setOfferingDate(String offeringDate) {
        this.offeringDate = offeringDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alternativeName);
        dest.writeString(detailUrl);
        dest.writeString(offeringDate);
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
