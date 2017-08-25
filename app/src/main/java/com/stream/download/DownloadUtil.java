package com.stream.download;

import android.util.Log;

import com.stream.hstream.Setting;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fuzm on 2017/5/12 0012.
 */

public class DownloadUtil {

    private static final String TAG = DownloadUtil.class.getSimpleName();

    private static final Pattern File_PATTERN = Pattern.compile("([^/?=]*\\.mp4)");
    private static final String EXTENSIONG = "mp4";

    public static String getFilePath(String fileName) {
        if(fileName != null && fileName.length() > 0) {
            return Setting.getDownloadDir() + fileName + "." + EXTENSIONG;
        } else {
            return null;
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if(file.exists()) {
            file.delete();
        }
    }

    public static void deleteFile(DownloadQueen.WorkInfo info) {
        String filePath = getFileName(info);
        File file = new File(filePath);
        if(file.exists()) {
            file.delete();
        }
    }

    public static String getFileName(DownloadQueen.WorkInfo info) {
        if(info.getFileName() == null || info.getFileName().length() == 0) {
            String name = DownloadUtil.getFileNameFromUrl(info.getFileUrl());
            if(name == null || name.length() == 0) {
                name = DownloadUtil.getFileNameFromUrl2(info.getFileUrl(), "." + EXTENSIONG);
            }

            return Setting.getDownloadDir() + name;
        } else {
            return Setting.getDownloadDir() + info.getFileName();
        }
    }

    public static String getFileNameFromUrl(String url) {
        Matcher m = File_PATTERN.matcher(url);
        String name = null;
        if(m.find()) {
            name = m.group(0);
        }
        Log.d(TAG, "Get name from url is: " + name);
        return name;
    }

    public static String getFileNameFromUrl2(String url, String matcher) {
        String name = null;
        //String extension = "." + EXTENSIONG;
        if(url == null || url.length() == 0) {
            int index = url.indexOf(matcher);
            int sIndex = -1;
            if(index != -1) {
                sIndex = name.indexOf("=", -index);
            }
            if(sIndex == -1) {
                sIndex = name.indexOf("/", -index);
            }
            if(sIndex == -1) {
                if(index > 100){
                    sIndex = index - 100;
                } else {
                    sIndex = 0;
                }
            }
            name = url.substring(sIndex, index + matcher.length());
        }
        return name;
    }

    public static boolean checkExtension(String extension){
        if(extension.toLowerCase().equals(EXTENSIONG)) {
            return true;
        } else {
            return false;
        }
    }

}
