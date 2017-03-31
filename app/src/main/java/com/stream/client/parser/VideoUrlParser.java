package com.stream.client.parser;

import android.util.Log;

import com.stream.client.data.VideoDetailInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/26 0026.
 */

public class VideoUrlParser {

    private static final String FILE_TAG = "file:\"";

    public static class Result {
        public String url;
    }

    public static VideoUrlParser.Result parse(String body) {
        Result result = new Result();
        Document doc = Jsoup.parse(body);

        String url = null;
        try {
            Elements scripts = doc.getElementsByTag("body").get(0).getElementsByTag("script");
            for(Element script: scripts) {
                String html = script.html();
                if(html.contains(FILE_TAG)) {
                    url = catchFileUrl(html);
                    break;
                }
            }

            result.url = url;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }

    private static String catchFileUrl(String html) {
        int fileStartIndex = html.indexOf(FILE_TAG);
        int fileEndIndex = html.indexOf("\"", fileStartIndex + FILE_TAG.length());
        return html.substring(fileStartIndex + FILE_TAG.length(), fileEndIndex);
    }

    public static void main(String[] args) {
        String file = "jwplayer(\"pro-player-customid-7491\")[\"setup\"]({file:\"http://cache12.stormap.sapo.pt/dld/43512dcc7fcdd8070c0a349a6dd45d31/58d74752/vidstore19/videos/52/b8/e7/8211614_g0ziu.mp4\",image:\"http://hentaistream.com/wp-includes/images/preview3.png\",width:\"600\",height:\"510\",startparam:\"start\",stretching:\"exactfit\",primary:\"flash\",abouttext:\"Visit HentaiStream.COM\",aboutlink:\"http://hentaistream.com\",sharing: {link:\"http://hentaistream.com/sleazy-daughter-episode-02\",code:encodeURI('<iframe src=\"http://hentaistream.com/frames/s11_Sleazy_Daughter_Episode_02.html\" width=\"601\"  height=\"511\"  frameborder=\"0\"  scrolling=\"auto\" />'),heading:\"Share this Hentai episode!\"} });";
        System.out.println(catchFileUrl(file));
    }
}
