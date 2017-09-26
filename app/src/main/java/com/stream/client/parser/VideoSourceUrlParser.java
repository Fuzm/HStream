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
            + HsUrl.getDomain() + ")([\\w+\\-+]*)");

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

    public static void main(String[] args) {
        String url = "https://muchohentai.com/125468/TIQnb0/2017/09/21/implicity-episode-1-raw/";
        Matcher m = MUCHO_URL_PATTERN.matcher(url);

        while (m.find()) {
            System.out.println("group 0: " + m.group(0));
            System.out.println("group 1: " + m.group(1));
            System.out.println("group 2: " + m.group(2));
            System.out.println("group 3: " + m.group(3));
            System.out.println("group 4: " + m.group(4));
            System.out.println("group 5: " + m.group(5));
            System.out.println("group 6: " + m.group(6));
            //System.out.println("group 2: " + m.group(2));
        }
//        else {
//            System.out.println("not found");
//        }
    }
}
