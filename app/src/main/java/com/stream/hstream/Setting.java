package com.stream.hstream;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hippo.unifile.UniFile;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.NumberUtils;

import java.io.File;

/**
 * Created by Fuzm on 2017/3/27 0027.
 */

public class Setting {

    private static final String TAG = Setting.class.getSimpleName();

    private static Context sContext;
    private static SharedPreferences sSettingsPre;

    public static void initialize(Context context) {
        sContext = context.getApplicationContext();
        sSettingsPre = PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    public static final String KEY_TYPE_REQUEST = "request_type";
    public static final String KEY_TYPE_WEB = "web_type";

    public static final String TYPE_MOBILE_REQUEST = "mobile";
    public static final String TYPE_NORMAL_REQUEST = "normal";

    public static final String WEB_STREAM = "STREAM";
    public static final String WEB_MUCHO = "MUCHO";

    public static final int MAX_LIST_VIEO_PLAY_NUM = 1;

    public static final String KEY_READ_CACHE_SIZE = "read_cache_size";
    public static final int DEFAULT_READ_CACHE_SIZE = 160;

    public static String getString(String key) {

        if(key.equals(KEY_TYPE_REQUEST)) {
            return TYPE_NORMAL_REQUEST;
        } else if(key.equals(KEY_TYPE_WEB)) {
            return WEB_MUCHO;
        }

        return null;
    }

    public static String getString(String key, String defValue) {
        try {
            return sSettingsPre.getString(key, defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static int getReadCacheSize() {
        return getIntFromStr(KEY_READ_CACHE_SIZE, DEFAULT_READ_CACHE_SIZE);
    }

    public static int getIntFromStr(String key, int defValue) {
        try {
            return NumberUtils.parseIntSafely(sSettingsPre.getString(key, Integer.toString(defValue)), defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    private static final String APP_DIRNAME = "HStream";
    private static final String DOWNLOAD = "download";

    @Nullable
    public static String getDownloadDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    APP_DIRNAME + File.separator + DOWNLOAD);
            if(!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath() + File.separator;
        }

        return null;
    }






}
