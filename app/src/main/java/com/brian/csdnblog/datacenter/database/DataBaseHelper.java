package com.brian.csdnblog.datacenter.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.util.LogUtil;

/**
 * 文章信息存储数据库
 * <p/>
 * PS：除了主进程，其他进程严禁直接访问数据库
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DataBaseHelper.class.getSimpleName();
    /**
     * 数据库版本,有修改的时候注意修改本字段和onUpgrade函数
     * 特别注意：新增数据表，也需要修改这个字段，且在升级过程中，创建新的表
     */
    private static final int DATABASE_VERSION = 1;

    // 数据库名称
    private static final String DATABASE_NAME = "DataBaseHelper";
    private static DataBaseHelper mInstance = null;

    public static DataBaseHelper getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                if (mInstance == null) {
                    mInstance = new DataBaseHelper();
                }
            }
        }
        return mInstance;
    }

    private DataBaseHelper() {
        super(Env.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        LogUtil.log("DATABASE_NAME=" + DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        BlogInfoTable.onCreate(db);
        BlogerTable.onCreate(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.log("oldVersion=" + oldVersion + "; newVersion=" + newVersion);
        BlogInfoTable.onUpgrade(db, oldVersion, newVersion);
        BlogerTable.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public synchronized Object getLock() {
        return TAG;
    }
}

