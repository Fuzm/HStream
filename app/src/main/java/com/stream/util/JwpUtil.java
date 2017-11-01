package com.stream.util;

/**
 * Created by Seven-one on 2017/9/25.
 */

public class JwpUtil {

    private static final String FILE_TAG = "file:\"";
    private static final String FILE_TAG_BLANK = "file: \"";

    public static String catchFileUrl(String html) {
        String tag = "";
        if(html.contains(FILE_TAG)) {
            tag = FILE_TAG;
            return doCatchFileUrl(html, tag);
        } else if(html.contains(FILE_TAG_BLANK)) {
            tag = FILE_TAG_BLANK;
            return doCatchFileUrl(html, tag);
        }

        return null;
    }

    private static String doCatchFileUrl(String html, String tag) {
        int fileStartIndex = html.indexOf(tag);
        int fileEndIndex = html.indexOf("\"", fileStartIndex + tag.length());
        return html.substring(fileStartIndex + tag.length(), fileEndIndex);
    }

    public static void main(String[] args) {
        String file = "jwplayer(\"pro-player-customid-7491\")[\"setup\"]({file:\"http://cache12.stormap.sapo.pt/dld/43512dcc7fcdd8070c0a349a6dd45d31/58d74752/vidstore19/videos/52/b8/e7/8211614_g0ziu.mp4\",image:\"http://hentaistream.com/wp-includes/images/preview3.png\",width:\"600\",height:\"510\",startparam:\"start\",stretching:\"exactfit\",primary:\"flash\",abouttext:\"Visit HentaiStream.COM\",aboutlink:\"http://hentaistream.com\",sharing: {link:\"http://hentaistream.com/sleazy-daughter-episode-02\",code:encodeURI('<iframe src=\"http://hentaistream.com/frames/s11_Sleazy_Daughter_Episode_02.html\" width=\"601\"  height=\"511\"  frameborder=\"0\"  scrolling=\"auto\" />'),heading:\"Share this Hentai episode!\"} });";
        //System.out.println(catchFileUrl(file));
    }

}