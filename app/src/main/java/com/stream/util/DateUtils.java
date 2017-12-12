package com.stream.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Seven-one on 2017/11/8 0008.
 */

public class DateUtils {

    /**
     * get date from str
     * @param dateStr yyyymmdd
     * @return
     */
    public static Date fromDateStr(String dateStr) {
        if(dateStr != null && dateStr.length() == 8) {
            try {
                int year = Integer.parseInt(dateStr.substring(0,4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int date = Integer.parseInt(dateStr.substring(6, 8));
                System.out.println("year: " + year);
                System.out.println("month: " + month);
                System.out.println("date: " + date);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, (month - 1), date);

                return calendar.getTime();
            } catch (Exception e) {
                return null;
            }

        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        Calendar calendar  = Calendar.getInstance();
        calendar.set(2017, 11, 20);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        System.out.println(dateFormat.format(calendar.getTime()));
    }
}
