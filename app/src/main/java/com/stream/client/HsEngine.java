package com.stream.client;

import com.stream.client.data.VideoInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.client.parser.VideoListParser;
import com.stream.client.parser.VideoUrlParser;

import java.util.Collections;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class HsEngine {

    private static final String TAG = HsEngine.class.getSimpleName();

    public static VideoListParser.Result getVideoList(HsClient.Task task, OkHttpClient okHttpClient, String url) throws Exception {
        Request request = new EhRequestBuilder(url).addHeader("Connection", "close").build();
        Call call = okHttpClient.newCall(request);

        if (task != null) {
            task.setCall(call);
        }

        String body = null;
        Headers headers = null;
        int code = -1;
        VideoListParser.Result result = null;
        try {
            Response response = call.execute();
            code = response.code();
            headers = response.headers();
            body = response.body().string();

            result = VideoListParser.parse(body);
        } catch (Exception e) {
            if(null !=body && body.contains("No episodes found")) {
                result.pages = 0;
                result.mVideoInfoList = Collections.emptyList();
                return result;
            } else {
                e.printStackTrace();
                throw e;
            }
        }

//        for(VideoInfo info: result.mVideoInfoList) {
//            VideoSourceParser.Result sourceResult = getVideoDetail(task, okHttpClient, info.url);
//            info.mSourceInfoList = sourceResult.mVideoSourceInfoList;
//        }

        return result;
    }

    public static VideoSourceParser.Result getVideoDetail(HsClient.Task task, OkHttpClient okHttpClient, String url) throws Exception {
        Request request = new EhRequestBuilder(url).addHeader("Connection", "close").build();
        Call call = okHttpClient.newCall(request);

        if (task != null) {
            task.setCall(call);
        }

        String body = null;
        Headers headers = null;
        int code = -1;
        VideoSourceParser.Result result = null;

        try {
            Response response = call.execute();
            code = response.code();
            headers = response.headers();
            body = response.body().string();

            result = VideoSourceParser.parse(body);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        for(VideoSourceInfo info: result.mVideoSourceInfoList) {
            VideoUrlParser.Result urlResult = getVideoUrl(task, okHttpClient, info.url);
            info.videoUrl = urlResult.url;
        }

        return result;
    }

    private static VideoUrlParser.Result getVideoUrl(HsClient.Task task, OkHttpClient okHttpClient, String url) throws Exception {
        Request request = new EhRequestBuilder(url).addHeader("Connection", "close").build();
        Call call = okHttpClient.newCall(request);

        if (task != null) {
            task.setCall(call);
        }

        String body = null;
        Headers headers = null;
        int code = -1;
        VideoUrlParser.Result result = null;

        try {
            Response response = call.execute();
            code = response.code();
            headers = response.headers();
            body = response.body().string();

            result = VideoUrlParser.parse(body);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }
}
