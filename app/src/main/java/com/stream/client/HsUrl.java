package com.stream.client;

import android.text.TextUtils;

import com.stream.enums.GenreEnum;
import com.stream.hstream.Setting;

import junit.framework.Assert;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class HsUrl {

    //stream
    private static final String STREAM_DOMAIN = "hentaistream.com";
    private static final String STREAM_HOST = "http://hentaistream.com/";
    private static final String STREAM_HOME = "http://hentaistream.com/";
    private static final String STREAM_PAGE = "page/";
    private static final String STREAM_SEARCH = "?s=";

    //mucho
    private static final String MUCHO_DOMAIN = "muchohentai.com";
    private static final String MUCHO_HOST = "https://muchohentai.com/";
    private static final String MUCHO_HOME = "https://muchohentai.com/genre/japanese/";
    private static final String MUCHO_GENRE = "https://muchohentai.com/genre/";
    private static final String MUCHO_PAGE = "page/";
    private static final String MUCHO_SEARCH = "?s=";

    /**
     * get domain
     * @return
     */
    public static String getDomain() {
        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
                return STREAM_DOMAIN;
            case Setting.WEB_MUCHO:
                return MUCHO_DOMAIN;
            default:
                return null;
        }
    }

    /**
     * get host
     * @return
     */
    private static String getHost() {
        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
                return STREAM_HOST;
            case Setting.WEB_MUCHO:
                return MUCHO_HOST;
            default:
                return null;
        }
    }
    
    /**
     * get home
     * @return
     */
    private static String getHome() {
        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
                return STREAM_HOME;
            case Setting.WEB_MUCHO:
                return MUCHO_HOME;
            default:
                return null;
        }
    }

    /**
     * get page url
     * @return
     */
    public static String getPageUrl(GenreEnum genreEnum, int pageIndex, String keyword) {
        if(genreEnum == GenreEnum.MochuSearch) {
            Assert.assertNotNull("keyword can not bu null", keyword);
            return getMuchoPageUrl(genreEnum, pageIndex, keyword);
        } else if(genreEnum == GenreEnum.StreamSearch){
            Assert.assertNotNull("keyword can not bu null", keyword);
            return getStreamPageUrl(pageIndex, keyword);
        } else {
            switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
                case Setting.WEB_STREAM:
                    return getStreamPageUrl(pageIndex, keyword);
                case Setting.WEB_MUCHO:
                    return getMuchoPageUrl(genreEnum, pageIndex, keyword);
                default:
                    return null;
            }
        }
    }

    private static String getStreamPageUrl(int pageIndex, String keyword) {
        String url = "";
        if(pageIndex > 0) {
            url = STREAM_PAGE + pageIndex;
        }

        if (!TextUtils.isEmpty(keyword)) {
            return STREAM_HOME + url + STREAM_SEARCH + keyword;
        } else {
            return STREAM_HOME + url;
        }
    }

    private static String getMuchoPageUrl(GenreEnum genreEnum, int pageIndex, String keyword) {
        String url = "";
        if(pageIndex > 0) {
            url = MUCHO_PAGE + pageIndex;
        }

        if (!TextUtils.isEmpty(keyword)) {
            return MUCHO_HOST + url + MUCHO_SEARCH + keyword;
        } else {
            return getMuchoGenreUrl(genreEnum) + url;
        }
    }

    private static String getMuchoGenreUrl(GenreEnum genreEnum) {
        return MUCHO_GENRE + genreEnum.getValue() + "/";
    }

}
