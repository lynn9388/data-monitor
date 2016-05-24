package com.lynn9388.datamonitor.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "APP".
 */
public class AppDao extends AbstractDao<App, Long> {

    public static final String TABLENAME = "APP";
    private DaoSession daoSession;
    ;

    public AppDao(DaoConfig config) {
        super(config);
    }


    public AppDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"APP\" (" + //
                "\"UID\" INTEGER PRIMARY KEY ," + // 0: uid
                "\"PACKAGE_NAME\" TEXT NOT NULL );"); // 1: packageName
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"APP\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, App entity) {
        stmt.clearBindings();

        Long uid = entity.getUid();
        if (uid != null) {
            stmt.bindLong(1, uid);
        }
        stmt.bindString(2, entity.getPackageName());
    }

    @Override
    protected void attachEntity(App entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    /** @inheritdoc */
    @Override
    public App readEntity(Cursor cursor, int offset) {
        App entity = new App( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // uid
                cursor.getString(offset + 1) // packageName
        );
        return entity;
    }

    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, App entity, int offset) {
        entity.setUid(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPackageName(cursor.getString(offset + 1));
    }

    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(App entity, long rowId) {
        entity.setUid(rowId);
        return rowId;
    }

    /** @inheritdoc */
    @Override
    public Long getKey(App entity) {
        if (entity != null) {
            return entity.getUid();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }

/**
     * Properties of entity App.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
    public final static Property Uid = new Property(0, Long.class, "uid", true, "UID");
        public final static Property PackageName = new Property(1, String.class, "packageName", false, "PACKAGE_NAME");
    }
    
}
