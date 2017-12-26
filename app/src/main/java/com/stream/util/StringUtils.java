package com.stream.util;

/**
 * Created by Fuzm on 2017/5/3 0003.
 */

public class StringUtils {

    public static String stringToHex(String str) {
        byte[] bytes = str.getBytes();
        return byteToHex(bytes);
    }


    public static String byteToHex(byte[] bytes) {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0; n<bytes.length; n++)
        {
            stmp = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
        }
        return sb.toString().toUpperCase().trim();
    }

    public static long createToken(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder("");
        for(char c: chars) {
            sb.append((int)c);
        }
        return Long.parseLong(sb.toString());
    }

    /**
     * clear all white space
     * @param str
     */
    public static String clearWhiteSpace(String str) {
        if(str != null) {
            str = str.replaceAll(" ", "");
        }

        return str;
    }

    public static boolean isEmpty(String str) {
        if(str == null || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get first string from param
     * @param str
     * @return
     */
    public static String getFirstStr(String str) {
        if(str != null && str.length() > 0) {
            return str.substring(0, 1);
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(StringUtils.createToken("测试"));
    }

}
