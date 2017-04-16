package com.stream.data.table;

import android.content.ContentValues;

/**
 * Created by Fuzm on 2017/4/9 0009.
 */

public final class SuggestionsTable {

    public static final String TABLE_NAME = "hs_suggestions";

    public static class Cols {
        public static final String UUID = "id";
        public static final String QUERY = "query";
        public static final String DATE = "date";
    }

    public static String buildCreate() {
        StringBuilder builder = new StringBuilder(100);
        builder.append("create table " + TABLE_NAME + "(");
        builder.append(Cols.UUID + " integer primary key autoincrement, ");
        builder.append(Cols.QUERY + "," );
        builder.append(Cols.DATE + ")" );
        return builder.toString();
    }

    public static String buildDrop() {
        StringBuilder builder = new StringBuilder(100);
        builder.append("DROP TABLE IF EXISTS " + TABLE_NAME);
        return builder.toString();
    }
}
