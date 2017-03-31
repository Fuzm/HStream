package com.stream.client;

import com.stream.client.parser.VideoDetailParser;
import com.stream.client.parser.VideoListParser;
import com.stream.client.parser.VideoUrlParser;

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
            e.printStackTrace();
            throw e;
        }

        return result;
    }

    public static VideoDetailParser.Result getVideoDetail(HsClient.Task task, OkHttpClient okHttpClient, String url) throws Exception {
        Request request = new EhRequestBuilder(url).addHeader("Connection", "close").build();
        Call call = okHttpClient.newCall(request);

        if (task != null) {
            task.setCall(call);
        }

        String body = null;
        Headers headers = null;
        int code = -1;
        VideoDetailParser.Result result = null;

        try {
            Response response = call.execute();
            code = response.code();
            headers = response.headers();
            body = response.body().string();

            result = VideoDetailParser.parse(body);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }

    public static VideoUrlParser.Result getVideoUrl(HsClient.Task task, OkHttpClient okHttpClient, String url) throws Exception {
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
