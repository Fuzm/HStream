package com.stream.download;

import android.os.Handler;
import android.text.TextUtils;

import com.stream.hstream.Setting;
import com.stream.util.HSAssetManager;

import junit.framework.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Seven-one on 2017/10/16.
 */

public class SubtitleDownloader {

    private static final int MAX_WORKER_SIZE = 3;

    private static SubtitleDownloader mDownloader;

    private ThreadPoolExecutor mWorkerPoolExecutor;
    private Handler mainThreadHandler;

    private SubtitleDownloader() {
        mWorkerPoolExecutor = new ThreadPoolExecutor(MAX_WORKER_SIZE, MAX_WORKER_SIZE,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        mainThreadHandler = new Handler();
    }

    /**
     * instance downloader
     * @return
     */
    public synchronized static SubtitleDownloader instance() {
        if(mDownloader == null) {
            mDownloader = new SubtitleDownloader();
        }

        return mDownloader;
    }

    /**
     * start download
     * @param subtile
     * @param downloadFileName
     */
    public void start(String subtile, String downloadFileName) {
        start(subtile, downloadFileName, null);
    }

    /**
     * start download
     * @param subtile
     * @param downloadFileName
     */
    public void start(String subtile, String downloadFileName, SubtitleDownloadListener listener) {
        Assert.assertNotNull("subtitle not null", subtile);
        Assert.assertNotNull("download file name not null", downloadFileName);

        Worker worker = new Worker(subtile, downloadFileName, listener);
        mWorkerPoolExecutor.execute(worker);
    }

    /**
     * download unit
     */
    private class Worker implements Runnable {

        private String mSubtitle;
        private String mDownloadFileName;
        private SubtitleDownloadListener mListener;

        public Worker(String subtile, String downloadFileName) {
            this(subtile, downloadFileName, null);
        }

        public Worker(String subtile, String downloadFileName,  SubtitleDownloadListener listener) {
            mSubtitle = subtile;
            mDownloadFileName = downloadFileName;
            mListener = listener;
        }

        @Override
        public void run() {
            downloadSubtitle();
        }

        private String getFilePath() {
            if(!TextUtils.isEmpty(mDownloadFileName)) {
                return Setting.getDownloadDir() + mDownloadFileName + getSubtitleFormat(mSubtitle);
            } else {
                return Setting.getDownloadDir() + mSubtitle;
            }
        }

        private String getSubtitleFormat(String subtitle) {
            String format = null;
            if(!TextUtils.isEmpty(subtitle)) {
                format = subtitle.substring(subtitle.lastIndexOf("."));
            }

            return format;
        }

        /**
         * notify success
         * @param subtitle
         * @param path
         */
        private void notifySuccess(final String subtitle, final String path) {
            if(mListener != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onSuccess(subtitle, path);
                    }
                });
            }
        }

        /**
         * notify fail
         * @param e
         */
        private void notifyFail(final Exception e) {
            if(mListener != null) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onFail(e);
                    }
                });
            }
        }

        private void downloadSubtitle() {
            String path = getFilePath();
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            boolean complete = false;
            try {
                //delete file if already exists
                File oldFile = new File(path);
                if(oldFile.exists()) {
                    oldFile.delete();
                }

                inputStream = HSAssetManager.getSubtitleInputStream(HSAssetManager.getSubtitleDir() + mSubtitle);
                outputStream = new FileOutputStream(path);

                int byteRead = 0;
                byte[] buffer = new byte[1024];
                while ((byteRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }

                complete = true;
                notifySuccess(mSubtitle, path);

            } catch (IOException e) {
                e.printStackTrace();
                notifyFail(e);
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //not complete, delete the file if exist
                if(!complete) {
                    File file = new File(path);
                    if(file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }

    /**
     * download listener interface
     */
    public interface SubtitleDownloadListener {

        void onSuccess(String subtitle, String path);

        void onFail(Exception e);
    }

    public static void main(String[] args) {
        System.out.println("sdfsdf.ass".substring("sdfsdf.ass".lastIndexOf(".") + 1));
    }
}
