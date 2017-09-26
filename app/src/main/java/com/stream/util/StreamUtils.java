package com.stream.util;

import android.text.TextUtils;

import com.stream.enums.VideoFormat;

/**
 * Created by Seven-one on 2017/9/26.
 */

public class StreamUtils {

    /**
     * check video format
     * @param videoUrl
     * @param format
     * @return
     */
    public static boolean checkVideoFormat(String videoUrl, VideoFormat format) {
        if(TextUtils.isEmpty(videoUrl)) {
            return false;
        }

        if(videoUrl.endsWith(format.getValue())) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        String playUrl = "sdfsdfsdfddddddddfdf.m3u8";
        System.out.print(checkVideoFormat(playUrl, VideoFormat.m3u8));
    }
}
