package com.stream.util;

import android.os.AsyncTask;
import android.util.Log;

import com.stream.hstream.Setting;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Seven-one on 2017/12/12 0012.
 */

public class SubtitleFileMananger {

    private static final String TAG = SubtitleFileMananger.class.getSimpleName();

    private static List<SubtitleFileInfo> sSubtitleArr;
    private volatile static boolean isCompress = false;

    private static Executor executor = Executors.newCachedThreadPool();

    public static void initialize() {
        //listSubtitle();
        autoCompress();
    }

    /**
     * find subtitle
     * @return
     */
    public static List<SubtitleFileInfo> listSubtitle() {
        if(sSubtitleArr == null || isCompress) {
            sSubtitleArr = listFile2(Setting.getSubtitleDir() + File.separator + ".tmp" + File.separator);
            isCompress = false;
        }

        return sSubtitleArr;
    }

    /**
     * query subtitle
     * @param query
     * @return
     */
    public static List<SubtitleFileInfo> querySubtitle(String query) {
        long cm = System.currentTimeMillis();
        List<SubtitleFileInfo> queryList = new ArrayList<>();
        List<SubtitleFileInfo> subtitleArr = listSubtitle();
        if(subtitleArr != null && query != null) {
            query = StringUtils.clearWhiteSpace(query);
            for(SubtitleFileInfo subtitle: subtitleArr) {
                if(StringUtils.clearWhiteSpace(subtitle.getName()).contains(query)) {
                    queryList.add(subtitle);
                }
            }
        }
        //Log.d(TAG, " subtitle count: " + subtitleArr. length + " query subtitle cost time: " + (System.currentTimeMillis() - cm));
        return queryList;
    }

    /**
     * auto compress zip file for subtitle
     */
    private static void autoCompress() {
        //Log.d(TAG, "auto compress start.........");
        String[] zipFileArr = listFile(new String[]{".zip"});
        if(zipFileArr != null) {
            for (String zipFileName: zipFileArr) {
                System.out.print(zipFileName);
                //Log.d(TAG, "start compress file: " + zipFileName);
                new CompressTask(zipFileName).executeOnExecutor(executor);
            }
            //Log.d(TAG, "auto compress end.........");
        }
    }

    /**
     * check file format is ass, ssa or srt
     * @param fileName
     * @return
     */
    private static boolean checkFormat(String fileName) {
        String[] formatArr = new String[]{".ass", ".ssa", ".srt"};
        return checkFormat(fileName, formatArr);
    }

    /**
     * check file format is ass, ssa or srt
     * @param fileName
     * @return
     */
    private static boolean checkFormat(String fileName, String[] formatArr) {
        for(String format: formatArr) {
            if(fileName.toLowerCase().endsWith(format.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

//    private static void writeEntryToFile(ZipFile zipFile, ZipEntry entry) {
//        //Log.d(TAG, "start write entry to file: " + entry.getName());
//        if(!entry.isDirectory()) {
//            File fileDir = new File(Setting.getSubtitleDir() + File.separator + ".tmp" + File.separator);
//
//            if(!fileDir.exists()) {
//                fileDir.mkdirs();
//            }
//
//            File file = new File(fileDir.getAbsolutePath() + File.separator + entry.getName());
//            if(!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            FileOutputStream outputStream = null;
//            InputStream inputStream = null;
//            try {
//                inputStream = zipFile.getInputStream(entry);
//                outputStream = new FileOutputStream(file);
//
//                int byteRead;
//                byte[] buffer = new byte[1 * 1024];
//                while((byteRead = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, byteRead);
//                    outputStream.flush();
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if(outputStream != null) {
//                    try {
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if(inputStream != null) {
//                    try {
//                        inputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    private static List<SubtitleFileInfo> listFile2(String dir) {
        String[] subtitleFileNameList = new File(dir).list();
        List<SubtitleFileInfo> subtilteList = new ArrayList<>();
        if(subtitleFileNameList != null) {
            for(String fileName: subtitleFileNameList) {
                File file = new File(dir + File.separator + fileName);
                //System.out.println(file.getAbsoluteFile());

                if (file.isDirectory()) {
                    subtilteList.addAll(listFile2(file.getAbsolutePath()));
                } else {
                    //System.out.println("File: " + file.getAbsoluteFile());
                    SubtitleFileInfo info = new SubtitleFileInfo();
                    info.setName(file.getName());
                    info.setFilePath(file.getAbsolutePath());
                    subtilteList.add(info);
                }
            }

        }
        return subtilteList;
    }

    /**
     * find file by format
     * @return
     */
    private static String[] listFile(final String[] formatArr) {
        return new File(Setting.getSubtitleDir()).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name != null) {
                    if(formatArr == null) {
                        return true;
                    } else {
                        return checkFormat(name, formatArr);
                    }
                }

                return false;
            }
        });
    }

    private static class CompressTask extends AsyncTask<Void, Void, Void> {

        private String mFileName;

        public CompressTask(String fileName) {
            mFileName = fileName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long cm = System.currentTimeMillis();
            compress(mFileName);
            isCompress = true;
            Log.d(TAG, "compress cost time: " + ((System.currentTimeMillis() - cm) / 1000) + "/s");
            return null;
        }

        /**
         * compress zip file
         * @param fileName
         */
        private static void compress(String fileName) {
            File file = new File(Setting.getSubtitleDir() + fileName);
            if(!file.exists()) {
                return;
            }

            ZipFile zipFile = null;
            boolean complete = false;
            try {
                zipFile = new ZipFile(Setting.getSubtitleDir() + fileName);
                zipFile.setFileNameCharset("UTF-8");
                if (!zipFile.isValidZipFile()) {
                    throw new ZipException("error zip file");
                }

                if(zipFile.isEncrypted()) {
                    zipFile.setPassword("fuzzmy");
                }

                File destDir = new File(Setting.getSubtitleDir() + File.separator + ".tmp" + File.separator);
                if(!destDir.exists()) {
                    destDir.mkdirs();
                }

                UnzipParameters parameters = new UnzipParameters();
                parameters.setIgnoreAllFileAttributes(true);

                List fileHeaderList = zipFile.getFileHeaders();
                for (int i = 0; i < fileHeaderList.size(); i++) {
                    FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                    if(checkFormat(fileHeader.getFileName())) {
                        System.out.println("****File Details for: " + fileHeader.getFileName() + "*****");
                        System.out.println("Name: " + fileHeader.getFileName());
                        System.out.println("Compressed Size: " + fileHeader.getCompressedSize());
                        System.out.println("Uncompressed Size: " + fileHeader.getUncompressedSize());
                        System.out.println("CRC: " + fileHeader.getCrc32());
                        System.out.println("************************************************************");

                        if(fileHeader.isEncrypted()) {
                            fileHeader.setPassword("fuzmmy".toCharArray());
                        }
                        zipFile.extractFile(fileHeader, destDir.getAbsolutePath(), parameters);
                    }
                }

                complete = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                /**
                 * delete file after complete
                 */
                if(complete && file.exists()) {
                    file.delete();
                }
            }
        }

    }

    public static class SubtitleFileInfo {

        private String name;
        private String filePath;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String toString() {
           return name;
        }
    }

    public static void main(String[] args) {
        //SubtitleFileMananger.initialize();
        List<SubtitleFileInfo> list = listFile2(Setting.getSubtitleDir() + File.separator + ".tmp" + File.separator);
        for(SubtitleFileInfo file: list) {
            System.out.println(file);
        }
    }
}
