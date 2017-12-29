package com.stream.util;

import android.content.Context;
import android.util.Log;

import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.HsUrl;
import com.stream.client.data.GenreInfoVO;
import com.stream.client.parser.GenreInfoParser;
import com.stream.common.PubConstant;
import com.stream.dao.GenreInfo;
import com.stream.enums.GenreEnum;
import com.stream.hstream.HStreamApplication;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.fragments.tab.ListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/12/26 0026.
 */

public class GenreManager {

    private static final String TAG = GenreManager.class.getSimpleName();

    /**
     * initialize
     * @param context
     */
    public static void initialize(Context context) {
        List<GenreInfo> list = HStreamDB.queryAllGenreInfo();
        if(list == null || list.size() == 0) {
            requestGenreInfo(context);
        }
    }

    /**
     * Get used genre info
     * @return
     */
    public static List<GenreInfo> getUsedGenreInfo() {
        List<GenreInfo> genreInfoList = new ArrayList<>();
        //add default genre
        for(GenreEnum genreEnum: GenreEnum.listForGener()) {
            GenreInfo info = new GenreInfo();
            info.setGenre_id(genreEnum.getValue());
            info.setGenre_name(genreEnum.getTitle());
            genreInfoList.add(info);
        }

        genreInfoList.addAll(HStreamDB.queryGenreInfoByStatus(PubConstant.GENRE_STUTAS_USED));
        Log.d(TAG, "get used genre size: " + genreInfoList.size());
        return genreInfoList;
    }

    /**
     * Get unused genre info
     * @return
     */
    public static List<GenreInfo> getUnusedGenreInfo() {
        return HStreamDB.queryGenreInfoByStatus(PubConstant.GENRE_STUTAS_UNUSED);
    }

    /**
     * Set genre info for used
     * @param info
     */
    public static void useGenreInfo(GenreInfo info) {
        info.setStatus(PubConstant.GENRE_STUTAS_USED);
        HStreamDB.putGenreInfo(info);
    }

    /**
     * Set genre info for unused
     * @param info
     */
    public static void unusedGenreInfo(GenreInfo info) {
        info.setStatus(PubConstant.GENRE_STUTAS_UNUSED);
        HStreamDB.putGenreInfo(info);
    }

    /**
     * Request genre info
     * @param context
     */
    public static void requestGenreInfo(Context context) {
        HsClient client = HStreamApplication.getHsClient(context);

        HsRequest request = new HsRequest();
        request.setMethod(HsClient.METHOD_GET_GENRE_INFO);
        request.setCallback(new GenreListener());
        request.setArgs(HsUrl.getMuchoGenreListUrl());
        client.execute(request);
    }

    /**
     * Deal success
     * @param result
     */
    private static void notifySuccess(GenreInfoParser.Result result) {
        if(result != null && result.genreInfoList != null) {
            for(GenreInfoVO vo: result.genreInfoList) {
                GenreInfo info = new GenreInfo();
                info.setGenre_id(vo.getGenreId());
                info.setGenre_name(vo.getGenreName());
                info.setStatus(PubConstant.GENRE_STUTAS_UNUSED);
                //save to db
                HStreamDB.putGenreInfo(info);
            }
            Log.d(TAG, "find " + result.genreInfoList.size() + " genres");
        }
    }

    /**
     * Genre listener for request callback
     */
    private static class GenreListener implements HsClient.Callback<GenreInfoParser.Result> {

        @Override
        public void onSuccess(GenreInfoParser.Result result) {
            GenreManager.notifySuccess(result);
        }

        @Override
        public void onFailure(Exception e) {
            Log.d(TAG, "request genre info failure by " + e.getMessage());
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "cancel request genre info");
        }
    }
}
