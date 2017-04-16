package com.stream.hstream;

/**
 * Created by Fuzm on 2017/3/27 0027.
 */

public class Setting {

    public static final String KEY_TYPE_REQUEST = "request_type";

    public static final String TYPE_MOBILE_REQUEST = "mobile";
    public static final String TYPE_NORMAL_REQUEST = "normal";

    public static final int MAX_LIST_VIEO_PLAY_NUM = 1;

    public static String getString(String key) {

        if(key.equals(KEY_TYPE_REQUEST)) {
            return TYPE_NORMAL_REQUEST;
        }

        return null;
    }
}
