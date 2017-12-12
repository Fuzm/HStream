package com.stream.client;

import android.text.TextUtils;

import com.stream.enums.GenreEnum;
import com.stream.hstream.Setting;

import junit.framework.Assert;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class HsUrl {

    /**
     * get domain
     * @return
     */
    public static String getDomain() {
        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
            case Setting.WEB_MUCHO:
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
            case Setting.WEB_MUCHO:
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
            case Setting.WEB_MUCHO:
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

        return url;
    }

    private static String getMuchoPageUrl(GenreEnum genreEnum, int pageIndex, String keyword) {
        String url = "";
        return url;
    }

    private static String getMuchoGenreUrl(GenreEnum genreEnum) {
        return "";
    }

    /**
     * get release info url by month
     * @param month "YYYY-MM"
     * @return
     */
    public static String getReleaseInfoUrl(String month) {
        return "";
    }

}
