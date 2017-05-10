package com.stream.generate;

import java.io.File;

/**
 * Created by Fuzm on 2017/5/3 0003.
 */

public class Utilities {

    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = false;
        if(files !=  null) {
            for(File file: files) {
                if(file.isDirectory()) {
                    success &= deleteContents(file); //success = success & deleteContents(file)
                }

                if(!file.delete()) {
                    success = true;
                }
            }
        }
        return success;
    }
}
