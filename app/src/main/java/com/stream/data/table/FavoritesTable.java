package com.stream.data.table;

/**
 * Created by Fuzm on 2017/4/10 0010.
 */

public final class FavoritesTable {

    public static final String TABLE_NAME = "hs_favorites";

    public static class Cols {
        public static final String UUID = "id";
        public static final String TITLE = "title";
        public static final String IMAGE_PTAH = "image_path";
        public static final String SOURCE_PATH = "source_path";
        public static final String VIDEO_PATH = "video_path";
    }

    public static String buildCreate() {
        StringBuilder builder = new StringBuilder(100);
        builder.append("CREATE TABLE " + TABLE_NAME + "(");
        builder.append(Cols.UUID + " integer primary key autoincrement, ");
        builder.append(Cols.TITLE + ",");
        builder.append(Cols.IMAGE_PTAH + ",");
        builder.append(Cols.VIDEO_PATH + ")");
        return builder.toString();
    }

    public static String buildDrop() {
        StringBuilder builder = new StringBuilder(50);
        builder.append("DROP TABLE IF EXISTS " + TABLE_NAME);
        return builder.toString();
    }

}
