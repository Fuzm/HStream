package com.stream.hstream;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.hippo.conaco.Conaco;
import com.hippo.image.ImageBitmap;
import com.hippo.yorozuya.OSUtils;
import com.stream.client.HsClient;
import com.stream.okhttp.MobileRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class HStreamApplication extends Application {

    private static final String TAG = HStreamApplication.class.getSimpleName();
    private static final boolean DEBUG_CONACO = false;

    private OkHttpClient mOkHttpClient;
    private HsClient mHsClient;
    private Conaco<ImageBitmap> mConaco;
    private ImageBitmapHelper mImageBitmapHelper;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static OkHttpClient getOkHttpClient(Context context) {
        HStreamApplication application = (HStreamApplication) context.getApplicationContext();
        if(application.mOkHttpClient == null) {
            application.mOkHttpClient = new OkHttpClient.Builder()
                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.101", 9666)))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    //.cookieJar()
                    .build();
        }

        return application.mOkHttpClient;
    }

    public static HsClient getHsClient(Context context) {
        HStreamApplication application = (HStreamApplication) context.getApplicationContext();
        if(application.mHsClient == null) {
            application.mHsClient = new HsClient(application);
        }

        return application.mHsClient;
    }

    private static int getMemoryCacheMaxSize() {
        return Math.min(20 * 1024 * 1024, (int) OSUtils.getAppMaxMemory());
    }

    @NonNull
    public static Conaco<ImageBitmap> getConaco(@NonNull Context context) {
        HStreamApplication application = ((HStreamApplication) context.getApplicationContext());
        if (application.mConaco == null) {
            Conaco.Builder<ImageBitmap> builder = new Conaco.Builder<>();
            builder.hasMemoryCache = true;
            builder.memoryCacheMaxSize = getMemoryCacheMaxSize();
            builder.hasDiskCache = true;
            builder.diskCacheDir = new File(context.getCacheDir(), "thumb");
            builder.diskCacheMaxSize = 80 * 1024 * 1024; // 80MB
            builder.okHttpClient = getOkHttpClient(context);
            builder.objectHelper = getImageBitmapHelper(context);
            builder.debug = DEBUG_CONACO;
            application.mConaco = builder.build();
        }
        return application.mConaco;
    }

    public static ImageBitmapHelper getImageBitmapHelper(Context context) {
        HStreamApplication application = ((HStreamApplication) context.getApplicationContext());
        if (application.mImageBitmapHelper == null) {
            application.mImageBitmapHelper = new ImageBitmapHelper();
        }
        return application.mImageBitmapHelper;
    }

    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.101", 9666)))
                //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("58.216.61.243", 808)))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request re = chain.request();
                        System.out.println(re.headers());
                        System.out.println("---------------------------------");

                        return chain.proceed(re);
                    }
                })
                //.cookieJar()
                .build();

        String url = "http://hentaistream.com/";
        Request request = new MobileRequestBuilder(url).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println(response.code());
//                System.out.println(response.headers());
//                System.out.println(response.body().string());
            }
        });

//        Response response = call.execute();
//        System.out.println(response.code());
//        System.out.println(response.headers());
//        System.out.println(response.body().string());
    }
}
