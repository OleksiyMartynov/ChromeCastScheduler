package com.beastpotato.cast.chromcastscheduler.managers;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 8/28/2016.
 */

public class DatabaseManager extends OrmLiteSqliteOpenHelper {

    private static DatabaseManager instance;
    private static final String DATABASE_NAME = "ormlite.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<ScheduledItem, Integer> mScheduledItemDao = null;

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseManager getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseManager(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.clearTable(connectionSource, ScheduledItem.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, ScheduledItem.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dao<ScheduledItem, Integer> getScheduledItemDao() throws SQLException {
        if (mScheduledItemDao == null) {
            mScheduledItemDao = getDao(ScheduledItem.class);
        }

        return mScheduledItemDao;
    }

    @Override
    public void close() {
        mScheduledItemDao = null;

        super.close();
    }

    public List<ScheduledItem> getScheduledItems() {
        try {
            Dao<ScheduledItem, Integer> dao = getScheduledItemDao();
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void setScheduledItem(ScheduledItem item) {
        try {
            Dao<ScheduledItem, Integer> dao = getScheduledItemDao();
            dao.createOrUpdate(item);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteScheduledItem(ScheduledItem item) {
        try {
            Dao<ScheduledItem, Integer> dao = getScheduledItemDao();
            dao.delete(item);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
