package com.stream.client.parser;

import android.os.Parcelable;

import com.stream.client.data.VideoDetailInfo;
import com.stream.client.data.VideoInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoDetailParser {

    private static final String FILE_TAG = "file:\"";

    public static class Result {
        public List<VideoDetailInfo> mVideoDetailInfoList;
    }

    public static Result parse(String body) {
        Result result = new Result();
        Document doc = Jsoup.parse(body);

        try {
            List<VideoDetailInfo> list = new ArrayList<>();
            Element root = doc.getElementById("tabs-1");
            Elements sources = root.child(0).children();
            for(Element e: sources) {
                list.add(parseDetail(e, root));
            }

            result.mVideoDetailInfoList = list;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }

    private static VideoDetailInfo parseDetail(Element element, Element root) {
        String name = element.child(0).html();
        String id = element.child(0).attr("href");
        if(id.startsWith("#")) {
            id = id.replace("#", "");
        }

        Element iframe = root.getElementById(id).child(0).child(0);
        String url = iframe.attr("src");

        VideoDetailInfo info = new VideoDetailInfo();
        info.name = name;
        info.url = url;
        return info;
    }

}
