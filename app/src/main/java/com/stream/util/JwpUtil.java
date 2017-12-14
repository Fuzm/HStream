package com.stream.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seven-one on 2017/9/25.
 */

public class JwpUtil {

    private static final String FILE_TAG = "file:\"";
    private static final String FILE_TAG_BLANK = "file: \"";
    private static final String FILE_TAG_QUOTE = "\"file\":\"";

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("jwplayer\\(\"(\\w+)\"\\)");

    public static String catchFileUrl(String html) {
        if(html.contains(FILE_TAG)) {
            return doCatchFileUrl(html, FILE_TAG);
        } else if(html.contains(FILE_TAG_BLANK)) {
            return doCatchFileUrl(html, FILE_TAG_BLANK);
        } else if(html.contains(FILE_TAG_QUOTE)) {
            return doCatchFileUrl(html, FILE_TAG_QUOTE);
        }

        return null;
    }

    private static String doCatchFileUrl(String html, String tag) {
        int fileStartIndex = html.indexOf(tag);
        int fileEndIndex = html.indexOf("\"", fileStartIndex + tag.length());
        return html.substring(fileStartIndex + tag.length(), fileEndIndex);
    }

    private static String doCatchVideoId(String html) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(html);
        if(matcher.find()) {
            if(matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }

        return null;
    }

    public static void main(String[] args) {
        doCatchVideoId("var instance = jwplayer(\"fzm\"); function");
    }

}
