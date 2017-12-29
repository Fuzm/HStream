package com.stream.client.parser;

import android.util.Log;

import com.stream.client.HsUrl;
import com.stream.client.data.GenreInfoVO;
import com.stream.util.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Seven-one on 2017/11/7 0007.
 */

public class GenreInfoParser {

    private static final String TAG = GenreInfoParser.class.getSimpleName();

    private static final Pattern GENRE_ID_PATTERN = Pattern.compile("https?://" + HsUrl.getDomain() + "/genre/(\\w+)/");

    public static class Result {
        public List<GenreInfoVO> genreInfoList;
    }

    public static Result parse(String body) throws Exception {
        long start = System.currentTimeMillis();
        Document d = Jsoup.parse(body);
        Log.d(TAG, "Jsoup parse use time: " + (System.currentTimeMillis()-start)/1000);

        Result result = new Result();
        result.genreInfoList = new ArrayList<>();
        try {
            List<GenreInfoVO> infoList = new ArrayList<>();
            Elements elements = d.getElementById("mcTagMap").getElementsByClass("links");
            for(Element e : elements) {
                Elements list = e.children();
                for (Element li: list) {
                    GenreInfoVO info = getGenreInfo(li);
                    if(info != null) {
                        infoList.add(info);
                    }
                }
            }

            if(infoList != null && infoList.size() > 0) {
                result.genreInfoList.addAll(infoList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static GenreInfoVO getGenreInfo(Element li) {
        GenreInfoVO info = null;
        try {

            Element link = li.child(0);
            String id = getGenreId(link.attr("href"));

            if(!StringUtils.isEmpty(id)) {
                info = new GenreInfoVO();
                info.setGenreId(id);
                info.setGenreName(link.html());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static String getGenreId(String url) {
        Matcher matcher = GENRE_ID_PATTERN.matcher(url);
        if(matcher.find()) {
            //System.out.println(matcher.group(0));
            //System.out.println(matcher.group(1));
            if(matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }

        return null;
    }

    public static void main(String[] args) {
        String url = "";
        Matcher matcher = GENRE_ID_PATTERN.matcher(url);
        if(matcher.find()) {
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
        }
    }
}
