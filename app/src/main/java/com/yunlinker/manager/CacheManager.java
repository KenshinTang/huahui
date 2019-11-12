package com.yunlinker.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lemon on 2017/8/29.
 */

public class CacheManager {

    private static CacheManager instance = null;
    private SQLiteOpenHelper sqlHelper = null;

    public static CacheManager getInstance(Context c) {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                    instance.sqlHelper = new SQLiteOpenHelper(c, "webviewCache.db", null, 1) {
                        @Override
                        public void onCreate(SQLiteDatabase db) {

                        }

                        @Override
                        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                        }
                    };
                }
            }
        }
        return instance;
    }

    public void getData() {
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        
    }
}
