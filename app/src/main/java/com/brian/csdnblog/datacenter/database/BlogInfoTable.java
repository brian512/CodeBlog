package com.brian.csdnblog.datacenter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.LogUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlogInfoTable extends BaseTable<BlogInfo> {
    private static final String TAG = BlogInfoTable.class.getSimpleName();

    // 文章表名称
    public static final String TABLE_NAME = "BlogInfoTable";

    // 文章表字段ID
    private static final String ID = "id";
    public static final String BLOG_ID = "blog_id";
    public static final String TITLE = "title";
    public static final String LINK = "link";
    public static final String BLOGER_ID = "bloger_id";
    public static final String BLOGER_JSON = "bloger_json";
    public static final String SUMMARY = "summary";
    public static final String LOACAL_PATH = "local_path";
    public static final String DATESTAMP = "datestamp";
    public static final String VISITTIME = "visit_time";
    public static final String EXTRA_MSG = "extra_msg";
    public static final String TYPE = "type";
    public static final String FAVO = "isFavo";


    // 创建帖子表的sql语言
    protected static final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME
            + " ( "
            + ID + " integer primary key autoincrement, "
            + BLOG_ID + " text UNIQUE NOT NULL, "
            + TITLE + " text, "
            + LINK + " text, "
            + BLOGER_ID + " text, "
            + BLOGER_JSON + " text, "
            + SUMMARY + " text, "
            + LOACAL_PATH + " text, "
            + VISITTIME + " integer, "
            + DATESTAMP + " text, "
            + EXTRA_MSG + " text, "
            + FAVO + " integer, "
            + TYPE + " integer "
            + " ) ";

    private static BlogInfoTable mInstance;

    private BlogInfoTable() {
    }

    public static BlogInfoTable getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                if (mInstance == null) {
                    mInstance = new BlogInfoTable();
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

    public void saveBlogAsyc(final BlogInfo info) {
        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                saveBlog(info);
            }
        });
    }

    /**
     * 插入新的消息
     */
    public boolean saveBlog(BlogInfo info) {
        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{info.blogId};

        ContentValues values = toContentValues(info);
        return insertOrUpdate(TABLE_NAME, selection, selectionArgs, values, false);
    }

    public boolean saveOrUpdateBlog(BlogInfo info) {
        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{info.blogId};

        ContentValues values = toContentValues(info);
        return insertOrUpdate(TABLE_NAME, selection, selectionArgs, values, true);
    }

    /**
     * 收藏或取消收藏博文，更新数据库中的info.isFavo字段
     */
    public boolean doFavo(BlogInfo info) {
        ContentValues values = new ContentValues();
        values.put(FAVO, info.isFavo?1:0);
        return update(info.blogId, values);
    }

    /**
     * 收藏或取消收藏博文，更新数据库中的info.isFavo字段
     */
    public boolean updateBlogCachePath(BlogInfo info) {
        ContentValues values = new ContentValues();
        values.put(LOACAL_PATH, info.localPath);
        return update(info.blogId, values);
    }

    /**
     * 更新blog信息，使用封装好的ContentValues
     */
    private boolean update(String blogID, ContentValues values) {
        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{blogID};
        return update(TABLE_NAME, selection, selectionArgs, values);
    }


    public boolean delete(BlogInfo info) {
        return delete(info.blogId);
    }

    /**
     * 根据id删除
     */
    public boolean delete(String blogId) {
        if (TextUtils.isEmpty(blogId)) {
            return false;
        }

        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{blogId};

        return delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * 删除指定类型的文章
     */
    public boolean deleteBlogsByType(int type) {
        String selection = TYPE + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(type)};
        return delete(TABLE_NAME, selection, selectionArgs);
    }

    public boolean deleteHistoryBlogs() {
        String selection = FAVO + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(0)};
        return delete(TABLE_NAME, selection, selectionArgs);
    }

    public boolean deleteFavoBlogs() {
        String selection = FAVO + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(1)};
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
    public BlogInfo query(String blogId) {
        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{blogId};

        return query(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * 查询指定id博主的信息
     */
    public BlogInfo queryByBlogerId(String blogerId) {
        String selection = BLOGER_ID + " = ? ";
        String[] selectionArgs = new String[]{blogerId};

        return query(TABLE_NAME, selection, selectionArgs);
    }

    public List<BlogInfo> getFavoList(int start, int num) {
        String orderBy = VISITTIME + " desc ";
        String selection = FAVO + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(1)};

        String limit = String.format(Locale.ENGLISH, " %d, %d ", start, num);

        return queryList(TABLE_NAME, selection, selectionArgs, orderBy, limit);
    }

    public List<BlogInfo> getHistoryList(int start, int num) {
        String orderBy = VISITTIME + " desc ";
        String selection = FAVO + " != ? ";
        String[] selectionArgs = new String[]{String.valueOf(1)};

        String limit = String.format(Locale.ENGLISH, " %d, %d ", start, num);

        return queryList(TABLE_NAME, selection, selectionArgs, orderBy, limit);
    }

    public List<BlogInfo> queryList(int type) {
        return queryList(type, 0, 0);
    }

    /**
     * 批量查询消息（按照接收时间倒序）
     */
    public List<BlogInfo> queryList(int type, int startIndex, int num) {
        if (type <= 0 || startIndex < 0) {
            return null;
        }

        String orderBy = VISITTIME + " desc ";
        String selection = TYPE + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(type)};

        String limit = null;
        if (num > 0) {
            limit = String.format(Locale.ENGLISH, " %d, %d ", startIndex, num);
        }

        return queryList(TABLE_NAME, selection, selectionArgs, orderBy, limit);
    }

    @Override
    protected ContentValues toContentValues(BlogInfo info) {
        if (info != null) {
            ContentValues values = new ContentValues();
            values.put(BLOG_ID, info.blogId);
            values.put(TITLE, info.title);
            values.put(LINK, info.link);
            values.put(BLOGER_ID, info.blogerID);
            values.put(BLOGER_JSON, info.blogerJson);
            values.put(SUMMARY, info.summary);
            values.put(LOACAL_PATH, info.localPath);
            values.put(DATESTAMP, info.dateStamp);
            values.put(VISITTIME, info.visitTime);
            values.put(EXTRA_MSG, info.extraMsg);
            values.put(TYPE, info.type);
            values.put(FAVO, info.isFavo?1:0);
            return values;
        }
        return null;
    }

    @Override
    protected void readCursor(ArrayList<BlogInfo> list, Cursor cursor) throws JSONException {
        if (cursor != null && cursor.moveToFirst()) {
            BlogInfo blogInfo;
            do {
                blogInfo = new BlogInfo();
                blogInfo.blogId = cursor.getString(cursor.getColumnIndex(BLOG_ID));
                blogInfo.title = cursor.getString(cursor.getColumnIndex(TITLE));
                blogInfo.link = cursor.getString(cursor.getColumnIndex(LINK));
                blogInfo.blogerID = cursor.getString(cursor.getColumnIndex(BLOGER_ID));
                blogInfo.blogerJson = cursor.getString(cursor.getColumnIndex(BLOGER_JSON));
                blogInfo.summary = cursor.getString(cursor.getColumnIndex(SUMMARY));
                blogInfo.localPath = cursor.getString(cursor.getColumnIndex(LOACAL_PATH));
                blogInfo.dateStamp = cursor.getString(cursor.getColumnIndex(DATESTAMP));
                blogInfo.visitTime = cursor.getInt(cursor.getColumnIndex(VISITTIME));
                blogInfo.extraMsg = cursor.getString(cursor.getColumnIndex(EXTRA_MSG));
                blogInfo.type = cursor.getInt(cursor.getColumnIndex(TYPE));
                blogInfo.isFavo = cursor.getInt(cursor.getColumnIndex(FAVO)) == 1;
                list.add(blogInfo);
            } while (cursor.moveToNext());
        }
    }

}
