package com.stream.hstream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.stream.dao.DaoMaster;
import com.stream.dao.DaoSession;
import com.stream.dao.DownloadDao;
import com.stream.dao.DownloadInfo;
import com.stream.dao.Favorite;
import com.stream.dao.FavoriteDao;
import com.stream.dao.Suggestion;
import com.stream.dao.SuggestionDao;
import com.stream.util.SqlUtils;

import java.util.List;

/**
 * Created by Fuzm on 2017/5/3 0003.
 */

public class HStreamDB {

    private static String TAG = HStreamDB.class.getSimpleName();
    private static final String DB_NAME = "stream_database.db";
    private static final int MAX_ENTRIES = 50;

    private static DaoSession sDaoSession;

    private static class DBOpenHelper extends DaoMaster.OpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public static void initialize(Context context) {
        DBOpenHelper helper = new DBOpenHelper(
                context.getApplicationContext(), DB_NAME, null);

        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);

        sDaoSession = daoMaster.newSession();
    }

    public synchronized static List<DownloadInfo> queryAllDownloadInfo() {
        DownloadDao dao = sDaoSession.getDownloadDao();
        List<DownloadInfo> list = dao.queryBuilder().orderDesc(DownloadDao.Properties.Time).list();
        return list;
    }

    public synchronized static void addDownloadInfo(DownloadInfo downloadInfo) {
        // Insert
        sDaoSession.getDownloadDao().insert(downloadInfo);
    }

    public synchronized static void updateDownloadInfo(DownloadInfo downloadInfo) {
        sDaoSession.getDownloadDao().update(downloadInfo);
    }

    public synchronized static void removeDownloadInfo(String token) {
        sDaoSession.getDownloadDao().deleteByKey(token);
    }

    public synchronized static void putDownloadInfo(DownloadInfo downloadInfo) {
        DownloadDao dao = sDaoSession.getDownloadDao();
        if (null != dao.load(downloadInfo.getToken())) {
            dao.update(downloadInfo);
        } else {
            // Insert
            dao.insert(downloadInfo);
        }
    }

    public synchronized static List<Favorite> queryAllFavorite() {
        FavoriteDao dao = sDaoSession.getFavoriteDao();
        List<Favorite> list = dao.queryBuilder().orderDesc(FavoriteDao.Properties.Time).list();
        return list;
    }

    public synchronized static void putFavorite(Favorite favorite) {
        FavoriteDao dao = sDaoSession.getFavoriteDao();
        if(null != dao.load(favorite.getToken())) {
            dao.update(favorite);
        } else {
            dao.insert(favorite);
        }
    }

    public synchronized static void removeFavorite(String token) {
        sDaoSession.getFavoriteDao().deleteByKey(token);
    }

    public synchronized static boolean existeFavorite(String token) {
        return null != sDaoSession.getFavoriteDao().load(token);
    }

    public synchronized static List<Suggestion> searchSuggestionByPrefit(String prefit) {
        prefit = SqlUtils.sqlEscapeString(prefit + "%");
        SuggestionDao dao = sDaoSession.getSuggestionDao();
        List<Suggestion> list = dao.queryBuilder().orderDesc(SuggestionDao.Properties.Date)
                .where(SuggestionDao.Properties.Query.like(prefit)).limit(5).list();
        return list;
    }

    public synchronized static void deleteHistory(int maxEntries) {
        if (maxEntries < 0) {
            throw new IllegalArgumentException();
        }

        try {
            SuggestionDao suggestionDao = sDaoSession.getSuggestionDao();
            String selection = null;
            if (maxEntries > 0) {
                selection = SuggestionDao.Properties.Id.columnName + " IN " +
                        "(SELECT " + SuggestionDao.Properties.Id.columnName + " FROM " + SuggestionDao.TABLENAME +
                        " ORDER BY " + SuggestionDao.Properties.Date.columnName + " DESC" +
                        " LIMIT -1 OFFSET " + String.valueOf(maxEntries) + ")";
            }

            suggestionDao.getDatabase().delete(suggestionDao.TABLENAME, selection, null);
        } catch (RuntimeException e) {
            Log.e(TAG, "deleteHistory", e);
        }
    }

    public synchronized static List<Suggestion> searchSuggestionByQuery(String query) {
        SuggestionDao dao = sDaoSession.getSuggestionDao();
        List<Suggestion> list = dao.queryBuilder().orderDesc(SuggestionDao.Properties.Date)
                .where(SuggestionDao.Properties.Query.eq(query)).list();
        return list;
    }

    public synchronized static void addSuggestion(Suggestion suggestion) {
        SuggestionDao dao = sDaoSession.getSuggestionDao();
        List<Suggestion> list = searchSuggestionByQuery(suggestion.getQuery());
        if(list.size() == 0) {
            dao.insert(suggestion);
            deleteHistory(MAX_ENTRIES);
        }
    }

    public synchronized static void deleteSuggestionByQuery(String query) {
        SuggestionDao dao = sDaoSession.getSuggestionDao();
        List<Suggestion> list = searchSuggestionByQuery(query);
        if(list != null && list.size() > 0) {
            dao.deleteByKey(list.get(0).getId());
        }
    }
}
