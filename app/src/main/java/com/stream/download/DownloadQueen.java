package com.stream.download;

import android.icu.text.IDNA;
import android.text.TextUtils;
import android.util.Log;

import com.hippo.yorozuya.IntIdGenerator;
import com.stream.client.EhRequestBuilder;
import com.stream.hstream.Setting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
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

    private static final int MAX_WORKER_SIZE = 3;

    private static IntIdGenerator sIntIdGenerator = new IntIdGenerator();
    private static DownloadQueen sQueen ;

    private ThreadPoolExecutor mWorkerPoolExecutor;
    private OkHttpClient mHttpClient;

    private Queue<WorkInfo> mWorkInfoLinkedList;
    private ConcurrentHashMap<Integer, WorkInfo> mExecuteWorkMap;
    private Set<Integer> mInterruptedList;
    private DownloadWorkListener mWorkListener;
    private Object mWorkerLock = new Object();
    private int mWorkerCount;

    private DownloadQueen(OkHttpClient client) {
        mWorkInfoLinkedList = new LinkedList<>();
        mExecuteWorkMap = new ConcurrentHashMap<>();
        mInterruptedList = new HashSet<>();
        mHttpClient = client;
        mWorkerPoolExecutor = new ThreadPoolExecutor(MAX_WORKER_SIZE, MAX_WORKER_SIZE,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public static DownloadQueen obtainQueen(OkHttpClient client) {
        if(sQueen == null) {
            sQueen = new DownloadQueen(client);
        }

        return sQueen;
    }

    public static WorkInfo buildWork(String filename, String fileurl) {
        if(TextUtils.isEmpty(filename)) {
            throw new NullPointerException("filename is null");
        }
        return new WorkInfo(filename, fileurl);
    }

    public boolean isIdle() {
        if(mWorkInfoLinkedList.isEmpty() && mExecuteWorkMap.isEmpty()) {
            return true;
        } else {
            return false;
        }
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

    public void start() {
        synchronized (mWorkerLock) {
            if (null == mWorkerPoolExecutor) {
                Log.e(TAG, "Try to start worker after stopped");
                return;
            }

//            for(; mWorkerCount < MAX_WORKER_SIZE; mWorkerCount++) {
//                mWorkerPoolExecutor.execute(new Worker());
//            }
            if (mWorkerCount < MAX_WORKER_SIZE) {
                mWorkerCount++;
                mWorkerPoolExecutor.execute(new Worker());
            }
        }
    }

    public void stopWork(WorkInfo info) {
        unregiesteWork(info);

        //synchronized (mExecuteWorkMap) {
            if(mExecuteWorkMap.containsKey(info.getWId())) {
                synchronized (mInterruptedList) {
                    mInterruptedList.add(info.getWId());
                }
            }
        //}
    }

    public void stopAll() {
        synchronized (mWorkInfoLinkedList) {
            mWorkInfoLinkedList.clear();
        }

        //synchronized (mExecuteWorkMap) {
            for(Integer workId : mExecuteWorkMap.keySet()){
                synchronized (mInterruptedList) {
                    mInterruptedList.add(workId);
                }
            }
        //}
        //mWorkerPoolExecutor.shutdownNow();
    }

    public void setDownloadWorkListener(DownloadWorkListener listener) {
        mWorkListener = listener;
    }

    public void notifyStart(WorkInfo workInfo) {
        if(mWorkListener != null) {
            mWorkListener.onStart(workInfo);
        }
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
            Log.d(TAG, "start download by thread-" + this.hashCode());
            InputStream inputStream = null;
            RandomAccessFile accessFile = null;
            WorkInfo workInfo = null;
            try {
                synchronized (mWorkInfoLinkedList) {
                    workInfo = mWorkInfoLinkedList.poll();
                }

                if(workInfo == null) {
                    return false;
                }

                notifyStart(workInfo);

                //synchronized (mExecuteWorkMap) {
                    mExecuteWorkMap.put(workInfo.getWId(), workInfo);
                //}

                //thread interrupte, stop thread
                if(Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "downloader task stop");
                    return false;
                }

                //stop current work
                synchronized (mInterruptedList) {
                    if(mInterruptedList.contains(workInfo.getWId())) {
                        return true;
                    }
                }

                Request.Builder builder = new EhRequestBuilder(workInfo.getFileUrl()).addHeader("Connection", "close");

                long finishSize = 0;
                String filePath = DownloadUtil.getFilePath(workInfo.getFileName());
                accessFile = new RandomAccessFile(filePath, "rw");
                if(accessFile.length() > 0) {
                    finishSize = accessFile.length();
                    accessFile.seek(finishSize);
                    builder.addHeader("Range", "bytes=" + finishSize + "-");
                }

                Call call = mHttpClient.newCall(builder.build());
                Response response = call.execute();

                if(response.code() == 410) {
                    builder.removeHeader("Range");
                }

                if(response.code()/100 != 2) {
                    Log.d(TAG, "cannot get the source by response code: " + response.code());
                    if (response.code() == 410) {
                        notifyDownload410(workInfo);
                    } else {
                        String msg = "connect url error";
                        notifyDownloadFail(workInfo, msg);
                    }
                    return true;
                }

                String extension = response.body().contentType().subtype();
                long contentLength = response.body().contentLength();
                Log.d(TAG,"already download size: " + finishSize);
                Log.d(TAG,"content lenght :" + contentLength);
                if(contentLength == finishSize) {
                    //file already download complete; but the condition is imprecisely;
                    notifyDownloadFinish(workInfo);
                    return true;
                } else {
                    contentLength += finishSize;
                }

                //check extension
                if(!DownloadUtil.checkExtension(extension)) {
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
                synchronized (mInterruptedList) {
                    if(mInterruptedList.contains(workInfo.getWId())) {
                        return true;
                    }
                }

                inputStream = response.body().byteStream();
                int byteRead = 0;
                byte[] data = new byte[1024 * 4];
                while((byteRead = inputStream.read(data)) != -1) {
                    //Log.d(TAG, "read by one size: " + byteRead);
                    accessFile.write(data, 0, byteRead);
                    finishSize += byteRead;

                    notifyDownloading(workInfo, contentLength, finishSize, byteRead);

                    if(Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "worker stop");
                        return false;
                    }

                    //stop current work
                    synchronized (mInterruptedList) {
                        if(mInterruptedList.contains(workInfo.getWId())) {
                            return true;
                        }
                    }
                }

                //finish work
                notifyDownloadFinish(workInfo);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, workInfo.getFileName() + " download failure, maybe interrupte: " + e.getMessage());
                notifyDownloadFail(workInfo, "error");
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

                if(workInfo != null) {
                    //synchronized (mExecuteWorkMap) {
                        mExecuteWorkMap.remove(workInfo.getWId());
                    //}

                    synchronized (mInterruptedList) {
                        mInterruptedList.remove(workInfo.getWId());
                    }
                }
            }

            return false;
        }
    }

    private static class TempInfo {

        private String token;
        private long contentLength;
    }

    public static final class WorkInfo {

        private int wId;
        private String fileName;
        private String fileUrl;

        private WorkInfo(String fileName, String fileUrl) {
            wId = sIntIdGenerator.nextId();

            this.fileName = fileName;
            this.fileUrl = fileUrl;
        }

        public int getWId() {
            return wId;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String url) {
            fileUrl = url;
        }
    }

    public interface DownloadWorkListener {

        void onStart(WorkInfo workInfo);

        void onDownload(WorkInfo workInfo, long contentLength, long finished, int bytesRead);

        void onFinish(WorkInfo workInfo);

        void onFailure(WorkInfo workInfo, String msg);

        void onGet410(WorkInfo workInfo);
    }


}
