package com.stream.client.parser;

import android.util.Log;

import com.stream.client.data.VideoInfo;
import com.stream.hstream.Setting;
import com.stream.util.StringUtils;

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

    private static final String TAG = "VideoListParser";

    public static class Result {
        public int pages;
        public List<VideoInfo> mVideoInfoList;
    }

    public static Result parse(String body) {
        long start = System.currentTimeMillis();
        Document d = Jsoup.parse(body);
        Log.d(TAG, "Jsoup parse use time: " + (System.currentTimeMillis()-start)/1000);

        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
                if(Setting.TYPE_MOBILE_REQUEST.equals(Setting.getString(Setting.KEY_TYPE_REQUEST))) {
                    return StreamMobileParser.parse(d);
                } else {
                    return StreamNormalParser.parse(d);
                }
            case Setting.WEB_MUCHO:
                return MuchoNormalParser.parse(d);
            default:
                return null;
        }
    }

    private static class StreamNormalParser {

        public static Result parse(Document d) {
            Result result = new Result();

            result.mVideoInfoList = new ArrayList<>();
            try {
                List<VideoInfo> infos = new ArrayList<>();
                Elements elements = d.getElementsByClass("content").get(0).children();
                for(Element e : elements) {
                    String id = e.attr("id");
                    if(null != id && id.startsWith("post-")) {
                        infos.add(parseVideoInfo(e));
                    }
                }

                if(infos != null && infos.size() > 0) {
                    result.mVideoInfoList.addAll(infos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Element element = d.getElementById("wp_page_numbers");
                if(element != null) {
                    result.pages = parsePages(element);
                } else {
                    result.pages = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();

                if(result.mVideoInfoList != null && result.mVideoInfoList.size() > 0) {
                    result.pages = 1;
                } else {
                    result.pages = 0;
                }
            }

            return result;
        }

        private static VideoInfo parseVideoInfo(Element e) {
            VideoInfo info = new VideoInfo();
            String url = e.child(0).child(0).attr("href");
            String thumb = e.child(0).child(0).child(0).attr("src");
            String title = e.child(0).child(0).child(0).attr("title");

            VideoSourceUrlParser.Result result = VideoSourceUrlParser.parse(url);
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
            String[] pageInfo = e.getElementsByClass("page_info").last().html().split(" ");
            //String page = e.getElementsByClass("first_last_page").last().child(0).html();
            String page = pageInfo[pageInfo.length-1];
            if(null != page && !"".equals(page)) {
                return Integer.parseInt(page);
            } else {
                return 0;
            }
        }
    }

    private static class StreamMobileParser {

        public static Result parse(Document d) {
            Result result = new Result();
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

            try {
                Element element = d.getElementsByClass("pagenav").get(0);
                int pages = parsePages(element);
                result.pages = pages;
            } catch (Exception e) {
                e.printStackTrace();

                if(result.mVideoInfoList != null && result.mVideoInfoList.size() > 0) {
                    result.pages = 1;
                } else {
                    result.pages = 0;
                }
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

            VideoSourceUrlParser.Result result = VideoSourceUrlParser.parse(url);
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

    /**
     * mucho normal parser
     */
    private static class MuchoNormalParser {

        public static Result parse(Document d) {
            Result result = new Result();

            result.mVideoInfoList = new ArrayList<>();
            try {
                List<VideoInfo> infos = new ArrayList<>();
                Elements elements = d.getElementsByClass("loop-content").get(0).child(0).children();
                for(Element e : elements) {
                    String id = e.attr("id");
                    if(null != id && id.startsWith("post-")) {
                        infos.add(parseVideoInfo(e));
                    }
                }

                if(null != infos && infos.size() > 0) {
                    result.mVideoInfoList.addAll(infos);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Element element = d.getElementsByClass("wp-pagenavi").get(0);
                int pages = parsePages(element);
                result.pages = pages;
            } catch (Exception e) {
                e.printStackTrace();

                if(result.mVideoInfoList != null && result.mVideoInfoList.size() > 0) {
                    result.pages = 1;
                } else {
                    result.pages = 0;
                }
            }

            return result;
        }

        private static int parsePages(Element e) {
            String[] pageInfo = e.getElementsByClass("pages").last().html().split(" ");
            //String page = e.getElementsByClass("first_last_page").last().child(0).html();
            String page = pageInfo[pageInfo.length-1];
            if(null != page && !"".equals(page)) {
                return Integer.parseInt(page);
            } else {
                return 0;
            }
        }

        private static VideoInfo parseVideoInfo(Element e) {
            VideoInfo info = new VideoInfo();
            String thumb = e.getElementsByClass("clip").get(0).child(0).attr("src");

            Element titleElement = e.getElementsByClass("entry-title").get(0).child(0);
            String title = titleElement.attr("title");
            String url = titleElement.attr("href");

            VideoSourceUrlParser.Result result = VideoSourceUrlParser.parse(url);
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
