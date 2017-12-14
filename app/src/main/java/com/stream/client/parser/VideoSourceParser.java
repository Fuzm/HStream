package com.stream.client.parser;

import android.text.TextUtils;
import android.util.Log;

import com.stream.client.data.VideoDetailInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.hstream.Setting;
import com.stream.util.JwpUtil;

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

    private static final String TAG = "VideoSourceParser";

    public static class Result {
        public VideoDetailInfo mDetailInfo;
        public List<VideoSourceInfo> mVideoSourceInfoList;
    }

    public static Result parse(String body) {
        long start = System.currentTimeMillis();
        Document d = Jsoup.parse(body);
        Log.d(TAG, "Jsoup parse use time: " + ((System.currentTimeMillis()-start)/1000) + "s");

        switch (Setting.getString(Setting.KEY_TYPE_WEB)) {
            case Setting.WEB_STREAM:
                return StreamNormalParser.parse(d);
            case Setting.WEB_MUCHO:
                return MuchoNormalParser.parse(d);
            default:
                return null;
        }
    }

    /**
     * stream detail parser
     */
    private static class StreamNormalParser {

        public static Result parse(Document doc) {
            Result result = new Result();

            try {
                List<VideoSourceInfo> list = new ArrayList<>();
                Element root = doc.getElementById("tabs-1");
                Elements sources = root.child(0).children();
                for(Element e: sources) {
                    VideoSourceInfo info = parseDetail(e, root);
                    if(info != null) {
                        list.add(parseDetail(e, root));
                    }
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

            VideoSourceInfo info = null;
            try {
                Element iframe = root.getElementById(id).child(0).child(0);
                String url = iframe.attr("src");

                info = new VideoSourceInfo();
                info.name = name;
                info.url = url;
                return info;
            } catch (Exception e) {
                Log.e(TAG, "This Source is not available");
            }

            return info;
        }
    }

    /**
     * mucho detail parser
     */
    private static class MuchoNormalParser {

        public static Result parse(Document doc) {
            Result result = new Result();

            try {
                VideoDetailInfo detailInfo = parseDetail(doc);
                if(detailInfo != null) {
                    result.mDetailInfo = detailInfo;
                }

                List<VideoSourceInfo> list = new ArrayList<>();
                list.addAll(parseSource(doc));
                list.addAll(parseDownload(doc));

                result.mVideoSourceInfoList = list;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

            return result;
        }

        public static VideoDetailInfo parseDetail(Element root) {
            VideoDetailInfo info = null;
            //japanese name
            try {
                info = new VideoDetailInfo();
                Elements h4s = root.getElementById("extras").getElementsByTag("h4");
                for(Element element: h4s) {
                    if(element.html().contains("Alternative")) {
                        info.setAlternativeName(element.nextSibling().toString());
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            //offering date
            try {
                String offeringDate = "";
                Elements contents = root.getElementById("info").getElementsByClass("entry-content").get(0).children();
                for(Element element : contents) {
                    if(element.html().matches(".*\\d+.*")) {
                        offeringDate = element.html();
                        break;
                    }
                }

                info.setOfferingDate(offeringDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return info;
        }

        public static List<VideoSourceInfo> parseSource(Element root) {
            List<VideoSourceInfo> list = new ArrayList<>();
            try {

                VideoSourceInfo info = null;
                Elements videos = root.getElementById("video").children();
                Elements scripts = root.getElementById("page").getElementsByTag("script");

                for(Element element: videos) {
                    String videoId = element.child(0).id();

                    for(Element script: scripts) {
                        if(script.html().contains("jwplayer(\"" + videoId + "\")")) {
                            String videoUrl = JwpUtil.catchFileUrl(script.html());
                            if(videoUrl != null && !"".equals(videoUrl.trim())) {
                                info = new VideoSourceInfo();
                                info.name = videoId.toUpperCase();
                                info.videoUrl = videoUrl.replace("\\", "");
                                list.add(info);
                                break;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "parse detail error, " + e);
            }

            return list;
        }

        public static List<VideoSourceInfo> parseDownload(Element root) {
            List<VideoSourceInfo> list = new ArrayList<>();

            Elements videos = null;
            try {
                videos = root.getElementById("video").children();
            } catch (Exception e) {
                Log.e(TAG, "can not find video node from html", e);
            }

            if(videos != null && videos.size() > 1) {
                try {
                    for (Element element : videos) {
                        Elements links = element.getElementsByTag("a");

                        for (Element href : links) {
                            if (href.html().contains("Download")) {
                                VideoSourceInfo source = new VideoSourceInfo();
                                source.name = element.id().toUpperCase() + "-" + href.html();
                                source.videoUrl = href.attr("href");

                                if (!TextUtils.isEmpty(source.videoUrl)) {
                                    list.add(source);
                                    break;
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "parse download error for tab, ", e);
                }
            } else {
                try {
                    Elements hrefs = root.getElementById("details").getElementsByTag("a");
                    for(Element href: hrefs) {
                        if(href.html().contains("Download")) {
                            VideoSourceInfo source = new VideoSourceInfo();
                            source.name = href.html();
                            source.videoUrl = href.attr("href");

                            if(!TextUtils.isEmpty(source.videoUrl)) {
                                list.add(source);
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "parse download error for detail, " + e);
                }
            }

            return list;
        }
    }

    public static void main(String[] args) {
        System.out.println("sdfsdfdsdfssdfsdf".matches(".*\\d+.*"));
    }
}
