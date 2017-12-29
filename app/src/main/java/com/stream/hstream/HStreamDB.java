package com.stream.hstream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.danikula.videocache.Source;
import com.stream.dao.DaoMaster;
import com.stream.dao.DaoSession;
import com.stream.dao.DetailDao;
import com.stream.dao.DetailInfo;
import com.stream.dao.DownloadDao;
import com.stream.dao.DownloadInfo;
import com.stream.dao.Favorite;
import com.stream.dao.FavoriteDao;
import com.stream.dao.GenreDao;
import com.stream.dao.GenreInfo;
import com.stream.dao.SourceDao;
import com.stream.dao.SourceInfo;
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

    //default out time in 15 day
    private static final long SOURCE_OUT_TIME = 7 * 24 * 24 * 60 * 1000;

    private static DaoSession sDaoSession;

    private static class DBOpenHelper extends DaoMaster.OpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Updrade version: " + oldVersion + "->" + newVersion);
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

    /**
     * update download info state
     * @param token
     * @param state
     */
    public synchronized static void updateDownloadInfoState(String token, int state) {
        DownloadDao dao = sDaoSession.getDownloadDao();
        DownloadInfo info = null;
        if (null != (info = dao.load(token))) {
            info.setState(state);
            dao.update(info);
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

    /**
     * find the soruce by token
     * @param token
     * @return
     */
    public synchronized static List<SourceInfo> querySoruceInfoByToken(String token, boolean checkOutTime) {
        if(TextUtils.isEmpty(token)) {
            return null;
        }

        long out_time = System.currentTimeMillis() - SOURCE_OUT_TIME;
        Log.d(TAG, "source out time: " + out_time);
        SourceDao dao = sDaoSession.getSourceDao();

        List<SourceInfo> list = null;
        if(checkOutTime) {
            list = dao.queryBuilder().where(SourceDao.Properties.Token.eq(token),
                    SourceDao.Properties.Upd_time.ge(out_time)).orderAsc(SourceDao.Properties.Upd_time).list();
        } else {
            list = dao.queryBuilder().where(SourceDao.Properties.Token.eq(token))
                    .orderAsc(SourceDao.Properties.Upd_time).list();
        }

        return list;
    }

    /**
     * save the source
     * @param sourceInfo
     */
    public synchronized static void putSourceInfo(SourceInfo sourceInfo) {
        SourceDao dao = sDaoSession.getSourceDao();
        if(null != sourceInfo.getId() && null != dao.load(sourceInfo.getId())) {
            dao.update(sourceInfo);
        } else {
            dao.insert(sourceInfo);
        }
    }

    /**
     * delete by token
     * @param token
     */
    public synchronized static void deleteByToken(String token) {
        try {
            SourceDao sourceDao = sDaoSession.getSourceDao();
            String selection = SourceDao.Properties.Token.columnName + " = ?";

            String[] args = new String[]{token};
            sourceDao.getDatabase().delete(sourceDao.getTablename(), selection, args);

        } catch (RuntimeException e) {
            Log.e(TAG, "deleteByToken", e);
        }
    }

    /**
     * check source by token, check exist and out time
     * @param token
     * @return
     */
    public synchronized static boolean existSourceByToken(String token) {
        try {
            long out_time = System.currentTimeMillis() - SOURCE_OUT_TIME;
            SourceDao sourceDao = sDaoSession.getSourceDao();
            long count = sourceDao.queryBuilder().where(SourceDao.Properties.Upd_time.le(out_time)).count();

            if(count > 0) {
                return true;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "existSourceByToken", e);
        }

        return false;
    }

    /**
     * save detail info add or update;
     * @param detailInfo
     */
    public synchronized static void putDetailInfo(DetailInfo detailInfo) {
        DetailDao dao = sDaoSession.getDetailDao();
        if(null != detailInfo.getToken() && null != dao.load(detailInfo.getToken())) {
            dao.update(detailInfo);
        } else {
            dao.insert(detailInfo);
        }
    }

    /**
     * query detail info by token
     * @param token
     * @return
     */
    public synchronized static DetailInfo queryDetailInfo(String token) {
        return sDaoSession.getDetailDao().load(token);
    }

    /**
     * save genre info to add or update;
     * @param genreInfo
     */
    public synchronized static void putGenreInfo(GenreInfo genreInfo) {
        GenreDao dao = sDaoSession.getGenreDao();
        if(null != genreInfo.getGenre_id() && null != dao.load(genreInfo.getGenre_id())) {
            dao.update(genreInfo);
        } else {
            dao.insert(genreInfo);
        }
    }

    /**
     * Query all genre info
     * @return
     */
    public synchronized static List<GenreInfo> queryAllGenreInfo() {
        GenreDao dao = sDaoSession.getGenreDao();
        return dao.loadAll();
    }

    /**
     * Query genre info by status
     * @param status
     * @return
     */
    public synchronized static List<GenreInfo> queryGenreInfoByStatus(int status) {
        GenreDao dao = sDaoSession.getGenreDao();
        return dao.queryBuilder().where(GenreDao.Properties.Status.eq(status)).list();
    }
}
