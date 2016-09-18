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
    public static final String BLOGER_TYPE = "blog_type";
    public static final String URL_HOME = "url_home";
    public static final String URL_HEAD = "url_head";
    public static final String NICK_NAME = "nick_name";
    public static final String BIO = "bio";
    public static final String DATESTAMP_FOLLOW = "time_follow";
    public static final String COUNT_BLOG = "blog_count";
    public static final String FOLLOW = "isFollowed";


    // 创建表的sql语言
    protected static final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME
            + " ( "
            + ID + " integer primary key autoincrement, "
            + BLOGER_ID + " text UNIQUE NOT NULL, "
            + URL_HOME + " text, "
            + URL_HEAD + " text, "
            + NICK_NAME + " text, "
            + BIO + " text, "
            + DATESTAMP_FOLLOW + " text, "
            + FOLLOW + " integer, "
            + BLOGER_TYPE + " integer, "
            + COUNT_BLOG + " integer "
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
        return insertOrUpdate(TABLE_NAME, selection, selectionArgs, values, false);
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
            values.put(BLOGER_TYPE, info.blogerType);
            values.put(URL_HEAD, info.headUrl);
            values.put(URL_HOME, info.homePageUrl);
            values.put(BIO, info.bio);
            values.put(DATESTAMP_FOLLOW, info.dateStampFollow);
            values.put(NICK_NAME, info.nickName);
            values.put(COUNT_BLOG, info.blogCount);
            values.put(FOLLOW, info.isFollowed?1:0);
            return values;
        }
        return null;
    }

    @Override
    protected void readCursor(ArrayList<Bloger> list, Cursor cursor) throws JSONException {
        if (cursor != null && cursor.moveToFirst()) {
            Bloger blogerInfo;
            do {
                blogerInfo = new Bloger();
                blogerInfo.blogerID = cursor.getString(cursor.getColumnIndex(BLOGER_ID));
                blogerInfo.headUrl = cursor.getString(cursor.getColumnIndex(URL_HEAD));
                blogerInfo.homePageUrl = cursor.getString(cursor.getColumnIndex(URL_HOME));
                blogerInfo.bio = cursor.getString(cursor.getColumnIndex(BIO));
                blogerInfo.nickName = cursor.getString(cursor.getColumnIndex(NICK_NAME));
                blogerInfo.dateStampFollow = cursor.getInt(cursor.getColumnIndex(DATESTAMP_FOLLOW));
                blogerInfo.blogCount = cursor.getInt(cursor.getColumnIndex(COUNT_BLOG));
                blogerInfo.isFollowed = cursor.getInt(cursor.getColumnIndex(FOLLOW)) == 1;
                blogerInfo.blogerType = cursor.getInt(cursor.getColumnIndex(BLOGER_TYPE));
                list.add(blogerInfo);
            } while (cursor.moveToNext());
        }
    }

}
