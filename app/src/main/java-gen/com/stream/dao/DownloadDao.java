package com.stream.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.stream.dao.DownloadInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "hs_dowanload_info".
*/
public class DownloadDao extends AbstractDao<DownloadInfo, String> {

    public static final String TABLENAME = "hs_dowanload_info";

    /**
     * Properties of entity DownloadInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Token = new Property(0, String.class, "token", true, "TOKEN");
        public final static Property Title = new Property(1, String.class, "title", false, "TITLE");
        public final static Property Thumb = new Property(2, String.class, "thumb", false, "THUMB");
        public final static Property Source_url = new Property(3, String.class, "source_url", false, "SOURCE_URL");
        public final static Property Url = new Property(4, String.class, "url", false, "URL");
        public final static Property State = new Property(5, int.class, "state", false, "STATE");
        public final static Property Time = new Property(6, long.class, "time", false, "TIME");
    };


    public DownloadDao(DaoConfig config) {
        super(config);
    }
    
    public DownloadDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"hs_dowanload_info\" (" + //
                "\"TOKEN\" TEXT PRIMARY KEY NOT NULL ," + // 0: token
                "\"TITLE\" TEXT," + // 1: title
                "\"THUMB\" TEXT," + // 2: thumb
                "\"SOURCE_URL\" TEXT," + // 3: source_url
                "\"URL\" TEXT," + // 4: url
                "\"STATE\" INTEGER NOT NULL ," + // 5: state
                "\"TIME\" INTEGER NOT NULL );"); // 6: time
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"hs_dowanload_info\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DownloadInfo entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getToken());
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
 
        String thumb = entity.getThumb();
        if (thumb != null) {
            stmt.bindString(3, thumb);
        }
 
        String source_url = entity.getSourceUrl();
        if (source_url != null) {
            stmt.bindString(4, source_url);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }
        stmt.bindLong(6, entity.getState());
        stmt.bindLong(7, entity.getTime());
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public DownloadInfo readEntity(Cursor cursor, int offset) {
        DownloadInfo entity = new DownloadInfo( //
            cursor.getString(offset + 0), // token
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // title
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // thumb
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // source_url
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // url
            cursor.getInt(offset + 5), // state
            cursor.getLong(offset + 6) // time
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DownloadInfo entity, int offset) {
        entity.setToken(cursor.getString(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setThumb(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSourceUrl(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setState(cursor.getInt(offset + 5));
        entity.setTime(cursor.getLong(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DownloadInfo entity, long rowId) {
        return entity.getToken();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DownloadInfo entity) {
        if(entity != null) {
            return entity.getToken();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
