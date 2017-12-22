package com.stream.videoplayerlibrary.common;

/**
 * Created by Seven-one on 2017/12/22 0022.
 */

public class StringUtils {

    public static String trim(String str) {
        if(str == null) {
            return str;
        } else {
            return str.trim();
        }
    }

    public static String splitIndex(String str, String regex, int index) {
        if(str == null) {
            return str;
        } else {
            String[] strArr = str.split(regex);
            if(strArr.length <= index) {
                return null;
            } else {
                return strArr[index].trim();
            }
        }
    }
}
