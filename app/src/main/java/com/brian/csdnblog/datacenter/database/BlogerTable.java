package com.brian.csdnblog.datacenter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.util.LogUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlogerTable extends BaseTable<Bloger> {
    private static final String TAG = BlogerTable.class.getSimpleName();

    // 文章表名称
    public static final String TABLE_NAME = "BlogerTable";

    // 文章表字段ID
    private static final String ID = "id";
    public static final String BLOGER_ID = "bloger_id";
    public static final String DATESTAMP_FOLLOW = "time_follow";


    // 创建表的sql语言
    protected static final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME
            + " ( "
            + ID + " integer primary key autoincrement, "
            + BLOGER_ID + " text"
            + " ) ";

    private static BlogerTable mInstance;

    private BlogerTable() {
    }

    public static BlogerTable getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                if (mInstance == null) {
                    mInstance = new BlogerTable();
                }
            }
        }
        return mInstance;
    }

    public static void onCreate(SQLiteDatabase db) {
        LogUtil.log("SQL_CREATE_TABLE=" + SQL_CREATE_TABLE);
        db.execSQL(SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 实现升级逻辑
    }


    /**
     * 插入新的消息
     */
    public boolean insert(Bloger info) {
        if (info == null) {
            return false;
        }

        String selection = BLOGER_ID + " = ? ";
        String[] selectionArgs = new String[]{info.blogerID};

        ContentValues values = toContentValues(info);
        return insertOrUpdate(TABLE_NAME, selection, selectionArgs, values);
    }

    /**
     * 根据id删除
     */
    public boolean delete(String blogId) {
        if (TextUtils.isEmpty(blogId)) {
            return false;
        }

        String selection = BLOGER_ID + " = ? ";
        String[] selectionArgs = new String[]{blogId};

        return delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * 清空当前记录
     */
    public boolean clearTable() {
        return delete(TABLE_NAME, null, null);
    }

    /**
     * 查询指定id的信息
     */
    public Bloger query(String blogerId) {
        if (TextUtils.isEmpty(blogerId)) {
            return null;
        }

        String selection = BLOGER_ID + " = ? ";
        String[] selectionArgs = new String[]{blogerId};

        return query(TABLE_NAME, selection, selectionArgs);
    }


    /**
     * 批量查询消息（按照接收时间倒序）
     */
    public List<Bloger> queryList(int type, int startIndex, int num) {
        if (type <= 0 || num < 0) {
            return null;
        }

        String orderBy = DATESTAMP_FOLLOW + " desc ";
        String limit = String.format(Locale.ENGLISH, " %d, %d ", startIndex, num);

        return queryList(TABLE_NAME, null, null, orderBy, limit);
    }

    @Override
    protected ContentValues toContentValues(Bloger info) {
        if (info != null) {
            ContentValues values = new ContentValues();
            values.put(BLOGER_ID, info.blogerID);
            return values;
        }
        return null;
    }

    @Override
    protected void readCursor(ArrayList<Bloger> list, Cursor cursor) throws JSONException {
        if (cursor != null && cursor.moveToFirst()) {
            Bloger blogInfo;
            do {
                blogInfo = new Bloger();
                blogInfo.blogerID = cursor.getString(cursor.getColumnIndex(BLOGER_ID));
                list.add(blogInfo);
            } while (cursor.moveToNext());
        }
    }

}
