package com.stream.util;

/**
 * Created by Seven-one on 2017/9/25.
 */

public class JwpUtil {

    private static final String FILE_TAG = "file:\"";
    private static final String FILE_TAG_BLANK = "file: \"";
    private static final String FILE_TAG_QUOTE = "\"file\":\"";

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

}
