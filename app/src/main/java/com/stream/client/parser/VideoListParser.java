package com.stream.client.parser;

import com.stream.client.data.VideoInfo;
import com.stream.hstream.Setting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoListParser {

    public static class Result {
        public int pages;
        public List<VideoInfo> mVideoInfoList;
    }

    public static Result parse(String body) {
        Document d = Jsoup.parse(body);
        if(Setting.TYPE_MOBILE_REQUEST.equals(Setting.getString(Setting.KEY_TYPE_REQUEST))) {
            return MobileParser.parse(d);
        } else {
            return NormalParser.parse(d);
        }
    }

    private static class NormalParser {

        public static Result parse(Document d) {
            Result result = new Result();
            try {
                Element element = d.getElementById("wp_page_numbers");
                int pages = parsePages(element);
                result.pages = pages;
            } catch (Exception e) {
                e.printStackTrace();
                result.pages = 0;
            }

            try {
                List<VideoInfo> infos = new ArrayList<>();
                Elements elements = d.getElementById("lastesteps_id").children();
                for(Element e : elements) {
                    String id = e.attr("id");
                    if(null != id && id.startsWith("post-")) {
                        infos.add(parseVideoInfo(e));
                    }
                }
                result.mVideoInfoList = infos;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        private static VideoInfo parseVideoInfo(Element e) {
            VideoInfo info = new VideoInfo();
            String url = e.child(0).child(0).attr("href");
            String thumb = e.child(0).child(0).child(0).attr("src");
            String title = e.child(0).child(0).child(0).attr("title");

            VideoDetailUrlParser.Result result = VideoDetailUrlParser.parse(url);
            if (null == result) {
                return null;
            }

            info.token = result.token;
            info.title = title;
            info.thumb = thumb;
            info.url = url;

            return info;
        }

        private static int parsePages(Element e) {
            String page = e.getElementsByClass("first_last_page").get(0).child(0).html();
            if(null != page && !"".equals(page)) {
                return Integer.parseInt(page);
            } else {
                return 0;
            }
        }
    }

    private static class MobileParser {

        public static Result parse(Document d) {
            Result result = new Result();

            try {
                Element element = d.getElementsByClass("pagenav").get(0);
                int pages = parsePages(element);
                result.pages = pages;
            } catch (Exception e) {
                e.printStackTrace();
                result.pages = 0;
            }

            try {
                List<VideoInfo> infos = new ArrayList<>();
                Elements elements = d.getElementsByClass("eppostimg");
                for(Element e : elements) {
                    infos.add(parseVideoInfo(e));
                }
                result.mVideoInfoList = infos;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        private static int parsePages(Element e) {
            int page = 9999;
            return page;
        }

        private static VideoInfo parseVideoInfo(Element e) {
            VideoInfo info = new VideoInfo();
            String url = e.child(0).attr("href");
            String thumb = e.child(0).child(0).attr("src");
            String title = e.child(0).child(0).attr("title");

            VideoDetailUrlParser.Result result = VideoDetailUrlParser.parse(url);
            if (null == result) {
                return null;
            }

            info.token = result.token;
            info.title = title;
            info.thumb = thumb;
            info.url = url;

            return info;
        }
    }
}
