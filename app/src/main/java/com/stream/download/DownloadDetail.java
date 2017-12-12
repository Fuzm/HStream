package com.stream.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.stream.dao.DownloadInfo;

/**
 * Created by Seven-one on 2017/10/16.
 */

public class DownloadDetail implements Parcelable {

    private String token;
    private String title;
    private String thumb;
    private String downloadUrl;
    private String alternativeName;
    private int state;
    private long speed;
    private long total;
    private long finished;

    public DownloadDetail() {}

    public DownloadDetail(DownloadDetail detail) {
        token = detail.getToken();
        title = detail.getTitle();
        alternativeName = detail.getAlternativeName();
        thumb = detail.getThumb();
        downloadUrl = detail.getDownloadUrl();
        state = detail.getState();
        speed = detail.getSpeed();
        total = detail.getTotal();
        finished = detail.getFinished();
    }

    public DownloadDetail(DownloadInfo downloadInfo) {
        token = downloadInfo.getToken();
        title = downloadInfo.getTitle();
        alternativeName = downloadInfo.getAlternative_name();
        thumb = downloadInfo.getThumb();
        downloadUrl = downloadInfo.getUrl();
        state = downloadInfo.getState();
    }

    protected DownloadDetail(Parcel in) {
        token = in.readString();
        title = in.readString();
        thumb = in.readString();
        downloadUrl = in.readString();
        alternativeName = in.readString();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
        dest.writeString(title);
        dest.writeString(thumb);
        dest.writeString(downloadUrl);
        dest.writeString(alternativeName);
    }

    public static final Creator<DownloadDetail> CREATOR = new Creator<DownloadDetail>() {
        @Override
        public DownloadDetail createFromParcel(Parcel in) {
            return new DownloadDetail(in);
        }

        @Override
        public DownloadDetail[] newArray(int size) {
            return new DownloadDetail[size];
        }
    };
}
