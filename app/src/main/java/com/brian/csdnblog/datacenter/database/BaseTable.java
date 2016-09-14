package com.brian.csdnblog.datacenter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.util.LogUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTable<T> {

    abstract protected ContentValues toContentValues(T obj);

    abstract protected void readCursor(ArrayList<T> list, Cursor cursor) throws JSONException;

    private static final boolean DEBUG = Config.isDebug;

    /**
     * 插入一行数据前，判断数据库中是否已经存在，如果存在就更新它，不存在就插入
     */
    protected boolean insertOrUpdate(String tableName, String selection, String[] selectionArgs, ContentValues values) {
        boolean flag = false;
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = DataBaseHelper.getInstance().getWritableDatabase();
            Cursor cursor = null;
            try {
                cursor = database.query(tableName, null, selection, selectionArgs, null, null, null);

                // 不存在则写入数据库
                if (cursor == null || !cursor.moveToFirst()) {
                    flag = database.insert(tableName, null, values) >= 0;
                } else {
                    flag = database.update(tableName, values, selection, selectionArgs) > 0;
                }
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                flag = false;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

                if (database.isOpen()) {
                    database.close();
                }
            }
        }
        LogUtil.log("result : " + flag);
        return flag;
    }

    /**
     * 批量插入数据库，不考虑已经存在相同主键的情况
     */
    protected boolean insert(String tableName, ContentValues[] cvArray) {
        boolean flag = true;
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = DataBaseHelper.getInstance().getWritableDatabase();
            database.beginTransaction();
            try {
                for (ContentValues values : cvArray) {
                    database.insert(tableName, null, values);
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                flag = false;
            } finally {
                database.endTransaction();
                if (database.isOpen()) {
                    database.close();
                }
            }
        }
        LogUtil.log("result : " + flag);
        return flag;
    }

    /**
     * 删除数据
     *
     * @param tableName 需要删除数据的表名
     */
    protected boolean delete(String tableName, String selection, String[] selectionArgs) {
        boolean flag = false;
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = null;
            try {
                database = DataBaseHelper.getInstance().getWritableDatabase();
                flag = database.delete(tableName, selection, selectionArgs) > 0;
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                flag = false;
            } finally {
                if (database != null && database.isOpen()) {
                    database.close();
                }
            }
        }
        LogUtil.log("result : " + flag);
        return flag;

    }

    /**
     * 查询，PS：提供给ContentProvider 使用
     */
    public Cursor query(String tableName, String[] columns, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = DataBaseHelper.getInstance().getReadableDatabase();
            try {
                cursor = database.query(tableName, columns, selection, selectionArgs, null, null, null);
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                cursor = null;
            }
        }
        return cursor;
    }

    /**
     * 查询
     */
    protected T query(String tableName, String selection, String[] selectionArgs) {
        T object = null;
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = DataBaseHelper.getInstance().getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = database.query(tableName, null, selection, selectionArgs, null, null, null, " 0, 1 ");

                ArrayList<T> list = new ArrayList<T>();
                readCursor(list, cursor);

                if (!list.isEmpty()) {
                    object = list.get(0);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                object = null;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (database.isOpen()) {
                    database.close();
                }
            }
        }
        LogUtil.log("result : " + object);
        return object;
    }

    /**
     * 按照时间倒叙批量拉取
     */
    protected List<T> queryList(String tableName, String selection, String[] selectionArgs, String orderBy, String limit) {
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = DataBaseHelper.getInstance().getWritableDatabase();
            Cursor cursor = null;
            try {
                cursor = database.query(tableName, null, selection, selectionArgs, null, null, orderBy, limit);
                ArrayList<T> list = new ArrayList<>();
                readCursor(list, cursor);
                LogUtil.log("query result size= " + list.size());
                return list;
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

                if (database.isOpen()) {
                    database.close();
                }
            }
        }
        LogUtil.log("result : error ");
        return null;
    }

    /**
     * 判断该记录是否存在
     */
    protected boolean isExist(String tableName, String selection, String[] selectionArgs) {
        boolean flag = false;
        synchronized (DataBaseHelper.getInstance().getLock()) {
            SQLiteDatabase database = DataBaseHelper.getInstance().getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = database.query(tableName, null, selection, selectionArgs, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    flag = true;
                }
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                flag = false;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

                if (database.isOpen()) {
                    database.close();
                }
            }
        }
        LogUtil.log("result : " + flag);
        return flag;
    }
}
