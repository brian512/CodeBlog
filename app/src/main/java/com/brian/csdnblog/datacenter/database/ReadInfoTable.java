package com.brian.csdnblog.datacenter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.brian.csdnblog.model.ReadInfo;
import com.brian.csdnblog.util.LogUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by huamm on 2016/9/26 0026.
 */

public class ReadInfoTable extends BaseTable<ReadInfo>  {
    private static final String TAG = ReadInfoTable.class.getSimpleName();

    // 文章表名称
    public static final String TABLE_NAME = "ReadInfoTable";

    // 文章表字段ID
    private static final String ID = "id";
    public static final String READ_DATE= "read_data";
    public static final String READ_COUNT = "read_count";
    public static final String READ_TIME = "read_time";

    // 创建表的sql语言
    protected static final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME
            + " ( "
            + ID + " integer primary key autoincrement, "
            + READ_DATE + " integer UNIQUE NOT NULL, "
            + READ_COUNT + " integer, "
            + READ_TIME + " integer "
            + " ) ";

    private static ReadInfoTable mInstance;

    private ReadInfoTable() {
    }

    public static ReadInfoTable getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                if (mInstance == null) {
                    mInstance = new ReadInfoTable();
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
    public boolean insertOrUpdate(ReadInfo info) {
        if (info == null) {
            return false;
        }

        String selection = READ_DATE + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(info.date)};

        ContentValues values = toContentValues(info);
        return insertOrUpdate(TABLE_NAME, selection, selectionArgs, values, true);
    }

    /**
     * 批量查询消息（按照接收时间倒序）
     */
    public List<ReadInfo> queryList(int startIndex, int num) {
        if (startIndex < 0 || num < 0) {
            return null;
        }

        String orderBy = READ_DATE + " desc ";
        String limit = String.format(Locale.ENGLISH, " %d, %d ", startIndex, num);

        return queryList(TABLE_NAME, null, null, orderBy, limit);
    }


    @Override
    protected ContentValues toContentValues(ReadInfo info) {
        if (info != null) {
            ContentValues values = new ContentValues();
            values.put(READ_DATE, info.date);
            values.put(READ_COUNT, info.count);
            values.put(READ_TIME, info.timeSecond);
            return values;
        }
        return null;
    }

    @Override
    protected void readCursor(ArrayList<ReadInfo> list, Cursor cursor) throws JSONException {
        if (cursor != null && cursor.moveToFirst()) {
            ReadInfo readInfo;
            do {
                readInfo = new ReadInfo();
                readInfo.date = cursor.getInt(cursor.getColumnIndex(READ_DATE));
                readInfo.timeSecond = cursor.getInt(cursor.getColumnIndex(READ_TIME));
                readInfo.count = cursor.getInt(cursor.getColumnIndex(READ_COUNT));
                list.add(readInfo);
            } while (cursor.moveToNext());
        }
    }

}
