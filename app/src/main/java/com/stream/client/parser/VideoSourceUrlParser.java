package com.stream.client.parser;

import android.support.annotation.Nullable;

import com.stream.client.HsUrl;
import com.stream.hstream.Setting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoSourceUrlParser {

    public static final Pattern STREAM_URL_PATTERN = Pattern.compile("https?://(?:"
            + HsUrl.getDomain() + ")/([\\w+\\-+]*)");

    public static final Pattern MUCHO_URL_PATTERN = Pattern.compile("https?://(?:" + HsUrl.getDomain()
            + ")/(\\d+)/(\\w+)/(\\d+)/(\\d+)/(\\d+)/([\\w+\\-+]*)");

    @Nullable
    public static Result parse(String url) {
        if (url == null) {
            return null;
        }

        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
                Matcher m1 = STREAM_URL_PATTERN.matcher(url);
                if (m1.find()) {
                    Result result = new Result();
                    result.token = m1.group(1);
                    return result;
                } else {
                    return null;
                }
            case Setting.WEB_MUCHO:
                Matcher m2 = MUCHO_URL_PATTERN.matcher(url);
                if (m2.find()) {
                    Result result = new Result();
                    result.token = m2.group(6);
                    return result;
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    public static class Result {
        public String token;
    }
}
