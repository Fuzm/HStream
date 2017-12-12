package com.stream.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.hippo.yorozuya.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/10/9.
 */

public class HSAssetManager {

    private static final String SUBTITLE_DIR_PATH = "subtitle";

    private static AssetManager assetManager;
    private static String[] sSubtitleArr;

    /**
     * initialize
     * @param context
     */
    public static void initialize(Context context) {
        assetManager = context.getAssets();
        listSubtitle();
    }

    /**
     * list subtitle
     * @return
     */
    public static String[] listSubtitle() {
        try {
            sSubtitleArr = assetManager.list(SUBTITLE_DIR_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(sSubtitleArr != null) {
            return sSubtitleArr;
        } else {
            return null;
        }
    }

    /**
     * query subtitle
     * @param query
     * @return
     */
    public static List querySubtitle(String query) {
        long cm = System.currentTimeMillis();
        List<String> queryList = new ArrayList<>();
        String[] subtitleArr = listSubtitle();
        if(subtitleArr != null) {
            query = StringUtils.clearWhiteSpace(query);
            for(String subtitle: subtitleArr) {
                if(StringUtils.clearWhiteSpace(subtitle).contains(query)) {
                    queryList.add(subtitle);
                }
            }
        }
        Log.d("HSAssetMannager", " subtitle count: " + subtitleArr. length + " query subtitle cost time: " + (System.currentTimeMillis() - cm));
        return queryList;
    }

    /**
     * get subtitle dir
     * @return
     */
    public static String getSubtitleDir() {
        return SUBTITLE_DIR_PATH + "/";
    }

    /**
     * get subtitle asset inputstream
     * @param subtitleFileName
     * @return
     * @throws IOException
     */
    public static InputStream getSubtitleInputStream(String subtitleFileName) throws IOException {
        if(assetManager != null) {
            return assetManager.open(subtitleFileName);
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
    }
}
