package com.stream.client.parser;

import android.support.annotation.Nullable;

import com.stream.client.HsUrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoSourceUrlParser {

    public static final Pattern URL_PATTERN = Pattern.compile("https?://(?:"
            + HsUrl.DOMAIN_STREAM + ")/([\\w+\\-+]*)");

    @Nullable
    public static Result parse(String url) {
        if (url == null) {
            return null;
        }

        Matcher m = URL_PATTERN.matcher(url);
        if (m.find()) {
            Result result = new Result();
            result.token = m.group(1);
            return result;
        } else {
            return null;
        }
    }

    public static class Result {
        public String token;
    }

    public static void main(String[] args) {
        String url = "http://hentaistream.com/ajdfs-sdfs-sdf-01";
        Matcher m = URL_PATTERN.matcher(url);

        if(m.find()) {
            System.out.println("group 0: " + m.group(0));
            System.out.println("group 1: " + m.group(1));
            System.out.println("group 2: " + m.group(2));
        } else {
            System.out.println("not found");
        }
    }
}
