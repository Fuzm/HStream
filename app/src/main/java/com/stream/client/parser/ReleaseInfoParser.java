package com.stream.client.parser;

import com.stream.client.data.ReleaseInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/11/7 0007.
 */

public class ReleaseInfoParser {

    private static final String TAG = ReleaseInfoParser.class.getSimpleName();

    public static class Result {
        public List<ReleaseInfo> releaseInfoList;
    }

    public static Result parse(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        List<ReleaseInfo> list = new ArrayList<>();
        ReleaseInfo info = null;
        while ((line = reader.readLine()) != null) {

            //event start
            if(line.startsWith("BEGIN:VEVENT")) {
                info = new ReleaseInfo();
            }

            //event for start time
            else if(line.startsWith("DTSTART")) {
                info.setReleaseDate(line.split(":")[1].trim());
            }

            //event for summary
            else if(line.startsWith("SUMMARY")) {
                info.setSummary(line.split(":")[1].trim());
            }

            //event for description
            else if(line.startsWith("DESCRIPTION")) {
                int index = line.indexOf(":");
                if(index != -1) {
                    info.setDescription(line.substring(index + 1));
                }
            }

            //event for URL
            else if(line.startsWith("URL")) {
                int index = line.indexOf(":");
                if(index != -1) {
                    info.setUrl(line.substring(index + 1));
                }
            }

            //event for CATEGORIES
            else if(line.startsWith("CATEGORIES")) {
                info.setCategories(line.split(":")[1].trim());
            }

            //event for ATTACH
            else if(line.startsWith("ATTACH;")) {
                int index = line.indexOf(":");
                if(index != -1) {
                    info.setAttach(line.substring(index + 1));
                }
            }

            //event for END
            else if(line.startsWith("END:VEVENT")) {
                list.add(info);
                info = null;
            }
        }

        Result result = new Result();
        result.releaseInfoList = list;
        return result;
    }
}
