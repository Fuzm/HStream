package com.stream.download;

import android.util.Log;

import com.hippo.yorozuya.IntIdGenerator;
import com.stream.client.EhRequestBuilder;
import com.stream.hstream.Setting;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Fuzm on 2017/5/10 0010.
 */

public class DownloadQueen {

    private static final String TAG = DownloadQueen.class.getSimpleName();

    private static final Pattern File_PATTERN = Pattern.compile("([^/?=]*\\.mp4)");
    private static final String EXTENSIONG = "mp4";
    private static final int MAX_WORKER_SIZE = 5;

    private static IntIdGenerator sIntIdGenerator = new IntIdGenerator();
    private static DownloadQueen sQueen ;

    private ThreadPoolExecutor mWorkerPoolExecutor;
    private OkHttpClient mHttpClient;

    private Queue<WorkInfo> mWorkInfoLinkedList;
    private HashMap<Integer, WorkInfo> mExecuteWorkMap;
    private DownloadWorkListener mWorkListener;
    private Object mWorkerLock = new Object();
    private int mWorkerCount;

    private DownloadQueen(OkHttpClient client) {
        mWorkInfoLinkedList = new LinkedList<>();
        mExecuteWorkMap = new HashMap<>();
        mHttpClient = client;
        mWorkerPoolExecutor = new ThreadPoolExecutor(MAX_WORKER_SIZE, MAX_WORKER_SIZE,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public static DownloadQueen obtainQueen(OkHttpClient client) {
        if(sQueen != null) {
            sQueen = new DownloadQueen(client);
        }

        return sQueen;
    }

    public static WorkInfo buildWork(String filename, String fileurl) {
        return new WorkInfo(filename, fileurl);
    }

    public void regiesteWork(WorkInfo info) {
        if(null != info && info.getFileUrl() != null) {
            synchronized (mWorkInfoLinkedList) {
                mWorkInfoLinkedList.add(info);
            }
        }
    }

    public void unregiesteWork(WorkInfo info) {
        synchronized (mWorkInfoLinkedList) {
            mWorkInfoLinkedList.remove(info);
        }
    }

    private void start() {
        synchronized (mWorkerLock) {
            if (null == mWorkerPoolExecutor) {
                Log.e(TAG, "Try to start worker after stopped");
                return;
            }

            for(; mWorkerCount < MAX_WORKER_SIZE; mWorkerCount++) {
                mWorkerPoolExecutor.execute(new Worker());
            }
        }
    }

    private void stopWork(WorkInfo info) {
        unregiesteWork(info);
        synchronized (mExecuteWorkMap) {
            if(mExecuteWorkMap.containsKey(info.getWId())) {
                info.isInterrupted = true;
            }
        }
    }

    private void stopAll() {
        synchronized (mWorkInfoLinkedList) {
            mWorkInfoLinkedList.clear();
        }
        mWorkerPoolExecutor.shutdownNow();
    }

    public void setDownloadWorkListener(DownloadWorkListener listener) {
        mWorkListener = listener;
    }

    public void notifyDownloading(WorkInfo workInfo, long contentLength, long remainSize, int bytesRead) {
        if(mWorkListener != null) {
            mWorkListener.onDownload(workInfo, contentLength, remainSize, bytesRead);
        }
    }

    public void notifyDownloadFail(WorkInfo workInfo, String msg) {
        if(mWorkListener != null) {
            mWorkListener.onFailure(workInfo, msg);
        }
    }

    public void notifyDownload410(WorkInfo workInfo) {
        if(mWorkListener != null) {
            mWorkListener.onGet410(workInfo);
        }
    }

    public void notifyDownloadFinish(WorkInfo workInfo) {
        if(mWorkListener != null) {
            mWorkListener.onFinish(workInfo);
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

    private String getFileName(WorkInfo info) {
        if(info.getFileName() == null || info.getFileName().length() == 0) {
            String name = getFileNameFromUrl(info.getFileUrl());
            return Setting.getDownloadDir() + name;
        } else {
            if(info.getFileName().endsWith(EXTENSIONG)) {
                return Setting.getDownloadDir() + info.getFileName();
            } else {
                return Setting.getDownloadDir() + info.getFileName() + "." + EXTENSIONG;
            }
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            //loop execute, util the condition
            while (!Thread.currentThread().isInterrupted() && runInternal());

            synchronized (mWorkerLock) {
                mWorkerCount--;
            }
        }

        public boolean runInternal(){
            WorkInfo workInfo = null;
            synchronized (mWorkInfoLinkedList) {
                workInfo = mWorkInfoLinkedList.poll();
            }

            if(workInfo == null) {
                return false;
            }

            synchronized (mExecuteWorkMap) {
                mExecuteWorkMap.put(workInfo.getWId(), workInfo);
            }

            //thread interrupte, stop thread
            if(Thread.currentThread().isInterrupted()) {
                Log.d(TAG, "downloader task stop");
                return false;
            }

            //stop current work
            if(workInfo.isInterrupted) {
                return true;
            }

            Log.d(TAG, "start download by thread-" + this.hashCode());
            InputStream inputStream = null;
            RandomAccessFile accessFile = null;
            try {
                long finishSize = 0;
                Log.d(TAG, "connect url: " + workInfo.getFileUrl());
                Request.Builder builder = new EhRequestBuilder(workInfo.getFileUrl()).addHeader("Connection", "close");
                accessFile = new RandomAccessFile(getFileName(workInfo), "rw");
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
                        //notifyDownload410(mLoaderId);
                    } else {
                        String msg = "connect url error";
                        notifyDownloadFail(workInfo, msg);
                    }
                    return true;
                }

                if(!checkExtension(extension)) {
                    response.body().close();
                    String errorMsg = "don't support this video type, error type: " + extension;
                    notifyDownloadFail(workInfo, errorMsg);
                    return true;
                }

                //thread interrupte, stop thread
                if(Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "downloader task stop");
                    return false;
                }

                //stop current work
                if(workInfo.isInterrupted) {
                    return true;
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

                    notifyDownloading(workInfo, contentLength, finishSize, byteRead);

                    if(Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "downloader task stop");
                        return false;
                    }

                    //stop current work
                    if(workInfo.isInterrupted) {
                        return true;
                    }
                }

            } catch (Exception e) {
                //e.printStackTrace();
                Log.d(TAG, "download failure, maybe interrupte: " + e.getMessage());
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
                synchronized (mExecuteWorkMap) {
                    mExecuteWorkMap.remove(workInfo);
                }
            }

            return false;
        }
    }

    public static final class WorkInfo {

        private int wId;
        private String fileName;
        private String fileUrl;
        private boolean isInterrupted;

        private WorkInfo(String fileName, String fileUrl) {
            wId = sIntIdGenerator.nextId();
            isInterrupted = false;

            this.fileName = fileName;
            this.fileUrl = fileUrl;
        }

        public int getWId() {
            return wId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }
    }

    public interface DownloadWorkListener {

        void onDownload(WorkInfo workInfo, long contentLength, long finished, int bytesRead);

        void onFinish(WorkInfo workInfo);

        void onFailure(WorkInfo workInfo, String msg);

        void onGet410(WorkInfo workInfo);
    }


}
