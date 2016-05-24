package com.lynn9388.datamonitor.dao;

import java.util.List;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "APP".
 */
public class App {

    private Long uid;
    /**
     * Not-null value.
     */
    private String packageName;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient AppDao myDao;

    private List<AppLog> logs;

    public App() {
    }

    public App(Long uid) {
        this.uid = uid;
    }

    public App(Long uid, String packageName) {
        this.uid = uid;
        this.packageName = packageName;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAppDao() : null;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    /** Not-null value. */
    public String getPackageName() {
        return packageName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<AppLog> getLogs() {
        if (logs == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AppLogDao targetDao = daoSession.getAppLogDao();
            List<AppLog> logsNew = targetDao._queryApp_Logs(uid);
            synchronized (this) {
                if (logs == null) {
                    logs = logsNew;
                }
            }
        }
        return logs;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetLogs() {
        logs = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
