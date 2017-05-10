package com.stream.download;

import android.util.Log;

import com.hippo.yorozuya.IntIdGenerator;
import com.stream.client.EhRequestBuilder;
import com.stream.hstream.Setting;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Fuzm on 2017/5/4 0004.
 */

public final class DownLoader implements Runnable {

    private static final String TAG = DownLoader.class.getSimpleName();

    private static final Pattern File_PATTERN = Pattern.compile("([^/?=]*\\.mp4)");
    private static final String EXTENSIONG = "mp4";

    private static final IntIdGenerator sIdGenerator = new IntIdGenerator();

    private int mLoaderId;
    private OkHttpClient mHttpClient;
    private String mName;
    private String mUrl;
    private OnDownLoaderListener mOnDownLoaderListener;
    private Thread mThread;

    public static DownLoader createLoader(OkHttpClient client, String name) {
        return createLoader(client, name, null);
    }

    public static DownLoader createLoader(OkHttpClient client, String name, String url) {
        DownLoader loader = new DownLoader(client, name, url);
        return loader;
    }

    private DownLoader(OkHttpClient client, String name, String url) {
        mHttpClient = client;
        mName = name;
        mUrl = url;
        mLoaderId = sIdGenerator.nextId();
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setOnDownLoaderListener(OnDownLoaderListener listener) {
        mOnDownLoaderListener = listener;
    }

    public int getLoaderId() {
        return mLoaderId;
    }

    @Override
    public void run() {
        mThread = Thread.currentThread();
        boolean finish = download();

        if(finish) {
            Log.d(TAG, "finish download by thread-" + this.hashCode());
            notifyDownloadFinish(mLoaderId);
        }
    }

    public void notifyDownloading(int loaderId, long contentLength, long remainSize, int bytesRead) {
        if(mOnDownLoaderListener != null) {
            mOnDownLoaderListener.onDownload(loaderId, contentLength, remainSize, bytesRead);
        }
    }

    public void notifyDownloadFail(int loaderId, String msg) {
        if(mOnDownLoaderListener != null) {
            mOnDownLoaderListener.onFailure(loaderId, msg);
        }
    }

    public void notifyDownload410(int loaderId) {
        if(mOnDownLoaderListener != null) {
            mOnDownLoaderListener.onGet410(loaderId);
        }
    }

    public void notifyDownloadFinish(int loaderId) {
        if(mOnDownLoaderListener != null) {
            mOnDownLoaderListener.onFinish(loaderId);
        }
    }

    private boolean checkExtension(String extension){
        if(extension.toLowerCase().equals(EXTENSIONG)) {
            return true;
        } else {
            return false;
        }
    }

    private String getFileNameFromUrl(String url) {
        Matcher m = File_PATTERN.matcher(url);
        String name = null;
        if(m.find()) {
            name = m.group(0);
        }
        if(name == null || name.length() == 0){
            name = getFileNameFromUrl2(url);
        }
        Log.d(TAG, "Get name from url is: " + name);
        return name;
    }

    private String getFileNameFromUrl2(String url) {
        String name = null;
        String extension = "." + EXTENSIONG;
        if(url == null || url.length() == 0) {
            int index = url.indexOf(extension);
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
            name = url.substring(sIndex, index+extension.length());
        }
        return name;
    }

    private String getFileName() {
        if(mName == null || mName.length() == 0) {
            String name = getFileNameFromUrl(mUrl);
            return Setting.getDownloadDir() + name;
        } else {
            if(mName.endsWith(EXTENSIONG)) {
                return Setting.getDownloadDir() + mName;
            } else {
                return Setting.getDownloadDir() + mName + "." + EXTENSIONG;
            }
        }
    }

    public boolean download() {
        Log.d(TAG, "start download by thread-" + this.hashCode());
        InputStream inputStream = null;
        RandomAccessFile accessFile = null;
        try {
            long finishSize = 0;
            Log.d(TAG, "connect url: " + mUrl);
            Request.Builder builder = new EhRequestBuilder(mUrl).addHeader("Connection", "close");
            accessFile = new RandomAccessFile(getFileName(), "rw");
            if(accessFile != null && accessFile.length() > 0) {
                Log.d(TAG,"already download size: " + accessFile.length());
                finishSize = accessFile.length();
                accessFile.seek(finishSize);
                builder.addHeader("Range", "bytes=" + finishSize + "-");
            }

            Call call = mHttpClient.newCall(builder.build());
            Response response = call.execute();

            String extension = response.body().contentType().subtype();
            long contentLength = response.body().contentLength();
            contentLength += finishSize;
            Log.d(TAG,"response code :" + response.code());
            Log.d(TAG,"content lenght :" + contentLength);
            Log.d(TAG,"media type :" + response.body().contentType().type());
            Log.d(TAG,"extension type :" + extension);

            if(response.code()/100 != 2) {
                Log.d(TAG, "cannot get the source by response code: " + response.code());
                if (response.code() == 410) {
                    notifyDownload410(mLoaderId);
                } else {
                    String msg = "connect url error";
                    notifyDownloadFail(mLoaderId, msg);
                }
                return false;
            }

            if(!checkExtension(extension)) {
                response.body().close();
                String errorMsg = "don't support this video type, error type: " + extension;
                notifyDownloadFail(mLoaderId, errorMsg);
                return false;
            }

            if(Thread.currentThread().isInterrupted()) {
                Log.d(TAG, "downloader task stop");
                return false;
            }

            inputStream = response.body().byteStream();
            int byteRead = 0;
            long receiveSize = 0;
            byte[] data = new byte[1024 * 4];
            while((byteRead = inputStream.read(data)) != -1) {
                //Log.d(TAG, "read by one size: " + byteRead);
                accessFile.write(data, 0, byteRead);
                receiveSize += byteRead;
                finishSize += byteRead;

                notifyDownloading(mLoaderId, contentLength, finishSize, byteRead);

                if(Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "downloader task stop");
                    return false;
                }
            }

        } catch (Exception e) {
            //e.printStackTrace();
            Log.d(TAG, "download failure, maybe interrupte: " + e.getMessage());

            String msg = "download failure: +" + e.getMessage();
            notifyDownloadFail(mLoaderId, msg);
            return false;
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mThread = null;
        }

        return true;
    }

    public void stop() {
        if(mThread != null) {
            Log.d(TAG, "stop current thread: " + mThread.getName());
            mThread.interrupt();
        }
    }

    public interface OnDownLoaderListener {

        void onDownload(int loaderId, long contentLength, long finished, int bytesRead);

        void onFinish(int loaderId);

        void onFailure(int loaderId, String msg);

        void onGet410(int loaderId);
    }

}
