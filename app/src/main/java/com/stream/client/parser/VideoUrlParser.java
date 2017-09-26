package com.stream.client.parser;

import com.stream.util.JwpUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Fuzm on 2017/3/26 0026.
 */

public class VideoUrlParser {

    public static class Result {
        public String url;
    }

    public static VideoUrlParser.Result parse(String body) {
        Result result = new Result();
        Document doc = Jsoup.parse(body);

        try {
            String url = null;
            Elements scripts = doc.getElementsByTag("body").get(0).getElementsByTag("script");
            for(Element script: scripts) {
                url = JwpUtil.catchFileUrl(script.html());
                if(url != null && !"".equals(url.trim())) {
                    result.url = url;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }


}
