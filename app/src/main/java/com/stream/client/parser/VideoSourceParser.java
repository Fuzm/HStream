package com.stream.client.parser;

import android.text.TextUtils;
import android.util.Log;

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
                List<VideoSourceInfo> list = new ArrayList<>();
                VideoSourceInfo info = parseDetail(doc);
                if(info != null) {
                    list.add(info);
                }

                List<VideoSourceInfo> downloadList = parseDownload(doc);
                if(downloadList != null && downloadList.size() > 0) {
                    list.addAll(downloadList);
                }

                result.mVideoSourceInfoList = list;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

            return result;
        }

        public static VideoSourceInfo parseDetail(Element root) {
            VideoSourceInfo info = null;
            try {
                String videoUrl = null;
                Elements scripts = root.getElementById("page").getElementsByTag("script");
                for(Element script: scripts) {
                    videoUrl = JwpUtil.catchFileUrl(script.html());
                    if(videoUrl != null && !"".equals(videoUrl.trim())) {
                        info = new VideoSourceInfo();
                        info.name = "Default";
                        info.videoUrl = videoUrl;
                        break;
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "parse detail error, " + e);
            }

            return info;
        }

        public static List<VideoSourceInfo> parseDownload(Element root) {
            List<VideoSourceInfo> list = new ArrayList<>();
            try {
                Elements hrefs = root.getElementById("details").getElementsByTag("a");
                for(Element href: hrefs) {
                    if(href.html().contains("Download")) {
                        VideoSourceInfo source = new VideoSourceInfo();
                        source.name = href.html();
                        source.videoUrl = href.attr("href");

                        if(!TextUtils.isEmpty(source.videoUrl)) {
                            list.add(source);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "parse download error, " + e);
            }

            return list;
        }
    }

}
