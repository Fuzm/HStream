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

    public static void main(String[] args) {
        System.out.println(StringUtils.createToken("测试"));
    }

}
