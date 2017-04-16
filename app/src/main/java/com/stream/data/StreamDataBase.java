package com.stream.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimeUtils;

import com.stream.data.model.Favorite;
import com.stream.data.table.FavoritesTable;
import com.stream.data.table.SuggestionsTable;
import com.stream.util.SqlUtils;

import org.w3c.dom.Text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Fuzm on 2017/4/9 0009.
 */

public class StreamDataBase {

    private static final String TAG = StreamDataBase.class.getSimpleName();
    private static final String DATABASE_NAME = "stream_database.db";

    private static final int MAX_ENTRIES = 50;

    private final SQLiteDatabase mDatabase;
    private static StreamDataBase sInstance;

    public static StreamDataBase getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new StreamDataBase(context.getApplicationContext());
        }
        return sInstance;
    }

    private StreamDataBase(Context context) {
        DataHelper dataHelper = new DataHelper(context);
        mDatabase = dataHelper.getWritableDatabase();
    }

    public void addSuggestion(String query) {
        if(!TextUtils.isEmpty(query)) {
            ContentValues values = new ContentValues();
            values.put(SuggestionsTable.Cols.QUERY, query);
            values.put(SuggestionsTable.Cols.DATE, System.currentTimeMillis());

            mDatabase.insert(SuggestionsTable.TABLE_NAME, null, values);
            deleteHistory(MAX_ENTRIES);
        }
    }

    public void deleteSuggestion(String query) {
        mDatabase.delete(SuggestionsTable.TABLE_NAME, SuggestionsTable.Cols.QUERY + " = ?", new String[]{query});
    }

    public String[] querySuggestions(String prefix) {
        List<String> queryList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(SuggestionsTable.TABLE_NAME);
        if (!TextUtils.isEmpty(prefix)) {
            sb.append(" WHERE ").append(SuggestionsTable.Cols.QUERY).append(" LIKE '")
                    .append(SqlUtils.sqlEscapeString(prefix)).append("%'");
        }
        sb.append(" ORDER BY ").append(SuggestionsTable.Cols.DATE).append(" DESC")
                .append(" LIMIT 5");

        Cursor cursor = mDatabase.rawQuery(sb.toString(), null);
        int queryIndex = cursor.getColumnIndex(SuggestionsTable.Cols.QUERY);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String suggestion = cursor.getString(queryIndex);
                if (!prefix.equals(suggestion)) {
                    queryList.add(suggestion);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return queryList.toArray(new String[queryList.size()]);
    }

    private void deleteHistory(int maxEntries) {
        if (maxEntries < 0) {
            throw new IllegalArgumentException();
        }

        try {
            String selection = null;
            if (maxEntries > 0) {
                selection = SuggestionsTable.Cols.UUID + " IN " +
                        "(SELECT " + SuggestionsTable.Cols.UUID + " FROM " + SuggestionsTable.TABLE_NAME +
                        " ORDER BY " + SuggestionsTable.Cols.DATE + " DESC" +
                        " LIMIT -1 OFFSET " + String.valueOf(maxEntries) + ")";
            }
            mDatabase.delete(SuggestionsTable.TABLE_NAME, selection, null);
        } catch (RuntimeException e) {
            Log.e(TAG, "deleteHistory", e);
        }
    }

    public void addFavorite(String title, String imagePath, String sourcePath, String videoPath) {
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(imagePath) && !TextUtils.isEmpty(sourcePath)) {
            ContentValues values = new ContentValues();
            values.put(FavoritesTable.Cols.TITLE, title);
            values.put(FavoritesTable.Cols.IMAGE_PTAH, imagePath);
            values.put(FavoritesTable.Cols.SOURCE_PATH, sourcePath);
            values.put(FavoritesTable.Cols.VIDEO_PATH, videoPath);

            mDatabase.insert(FavoritesTable.TABLE_NAME, null, values);
        }
    }

    public List<Favorite> queryFavorites() {
        List<Favorite> queryList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(FavoritesTable.TABLE_NAME);

        Cursor cursor = mDatabase.rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String id = cursor.getString(cursor.getColumnIndex(FavoritesTable.Cols.UUID));
                String title = cursor.getString(cursor.getColumnIndex(FavoritesTable.Cols.TITLE));
                String imagePath = cursor.getString(cursor.getColumnIndex(FavoritesTable.Cols.IMAGE_PTAH));
                String sourcePath = cursor.getString(cursor.getColumnIndex(FavoritesTable.Cols.SOURCE_PATH));
                String videoPath = cursor.getString(cursor.getColumnIndex(FavoritesTable.Cols.VIDEO_PATH));

                Favorite favorite = new Favorite();
                favorite.setId(id);
                favorite.setTitle(title);
                favorite.setImagePath(imagePath);
                favorite.setSourcePath(sourcePath);
                favorite.setVideoPath(videoPath);
                queryList.add(favorite);

                cursor.moveToNext();
            }
        }
        cursor.close();
        return queryList;
    }

    private class DataHelper extends SQLiteOpenHelper {

        public DataHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SuggestionsTable.buildCreate());
            db.execSQL(FavoritesTable.buildCreate());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SuggestionsTable.buildDrop());
            db.execSQL(FavoritesTable.buildDrop());
            onCreate(db);
        }
    }

}
