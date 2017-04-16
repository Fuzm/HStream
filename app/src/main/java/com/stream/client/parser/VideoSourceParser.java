package com.stream.client.parser;

import com.stream.client.data.VideoSourceInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoSourceParser {

    private static final String FILE_TAG = "file:\"";

    public static class Result {
        public List<VideoSourceInfo> mVideoSourceInfoList;
    }

    public static Result parse(String body) {
        Result result = new Result();
        Document doc = Jsoup.parse(body);

        try {
            List<VideoSourceInfo> list = new ArrayList<>();
            Element root = doc.getElementById("tabs-1");
            Elements sources = root.child(0).children();
            for(Element e: sources) {
                list.add(parseDetail(e, root));
            }

            result.mVideoSourceInfoList = list;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }

    private static VideoSourceInfo parseDetail(Element element, Element root) {
        String name = element.child(0).html();
        String id = element.child(0).attr("href");
        if(id.startsWith("#")) {
            id = id.replace("#", "");
        }

        Element iframe = root.getElementById(id).child(0).child(0);
        String url = iframe.attr("src");

        VideoSourceInfo info = new VideoSourceInfo();
        info.name = name;
        info.url = url;
        return info;
    }

}
