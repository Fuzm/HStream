package com.stream.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "hs_dowanload_info".
 */
public class DownloadInfo {

    /** Not-null value. */
    private String token;
    private String title;
    private String thumb;
    private String source_url;
    private String url;
    private String alternative_name;
    private int state;
    private long time;

    public DownloadInfo() {
    }

    public DownloadInfo(String token) {
        this.token = token;
    }

    public DownloadInfo(String token, String title, String thumb, String source_url, String url, String alternative_name, int state, long time) {
        this.token = token;
        this.title = title;
        this.thumb = thumb;
        this.source_url = source_url;
        this.url = url;
        this.alternative_name = alternative_name;
        this.state = state;
        this.time = time;
    }

    /** Not-null value. */
    public String getToken() {
        return token;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
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

    public String getSource_url() {
        return source_url;
    }

    public void setSource_url(String source_url) {
        this.source_url = source_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlternative_name() {
        return alternative_name;
    }

    public void setAlternative_name(String alternative_name) {
        this.alternative_name = alternative_name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
