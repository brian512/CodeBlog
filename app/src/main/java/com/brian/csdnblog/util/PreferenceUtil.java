
package com.brian.csdnblog.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.brian.csdnblog.Env;

public class PreferenceUtil {
    
    public static final String pre_key_loadImg = "pre_key_loadImg";
    public static final String pre_key_updateDetail = "pre_key_updateDetail";
    public static final String pre_key_upgrade = "pre_key_upgrade";
    public static final String pre_key_runInBack = "pre_key_runInBack";
    public static final String pre_key_recoveryLastStatus = "pre_key_recoveryLastStatus";
    public static final String pre_key_blog_position = "pre_key_blog_position";
    public static final String pre_key_indicator_position = "pre_key_indicator_position";
    public static final String pre_key_article_type = "pre_key_article_type";
    public static final String pre_key_version_code = "pre_key_version_code";
    public static final String init_position = "init_position";
    public static final String last_refresh_date = "last_refresh_date";

    private static SharedPreferences preferences = null;

    private PreferenceUtil(Activity activity) {}

    private static SharedPreferences getInstance(Context context) {

        if (preferences == null) {
            synchronized (PreferenceUtil.class) {
                if (context == null) {
                    context = Env.getContext().getApplicationContext();
                } else {
                    context = context.getApplicationContext();
                }
                preferences = PreferenceManager.getDefaultSharedPreferences(context);
            }
        }
        return preferences;
    }

    /**
     * 读取对应的键值
     * 
     * @param key
     * @return String
     */
    public static String getPreference(Context context, String key,
            String defaultValue) {
        // 获得当前的SharedPreferences对象
        SharedPreferences preference = getInstance(context);
        // 获取消息
        return preference.getString(key, defaultValue);
    }

    /**
     * 读取对应的键值
     * 
     * @param key
     * @return String
     */
    public static int getInt(Context context, String key, int defaultValue) {
        // 获得当前的SharedPreferences对象
        SharedPreferences preference = getInstance(context);
        // 获取消息
        return preference.getInt(key, defaultValue);
    }

    /**
     * 读取对应的键值
     * 
     * @param key
     * @return String
     */
    public static long getLong(Context context, String key,
            long defaultValue) {
        // 获得当前的SharedPreferences对象
        SharedPreferences preference = getInstance(context);
        // 获取消息
        long tmp = preference.getLong(key, defaultValue);
        return tmp;
    }

    /**
     * 将键值对写入配置文件
     * 
     * @param key
     * @param value
     */
    public static boolean getBoolean(Context context, String key,
            boolean value) {
        // 获得当前的SharedPreferences对象
        SharedPreferences preference = getInstance(context);
        // 获取消息
        boolean tmp = preference.getBoolean(key, value);
        return tmp;
    }

    /**
     * 将键值对写入配置文件
     * 
     * @param key
     * @param value
     */
    public static void setString(Context context, String key, String value) {
        // 创建一个SharedPreferences对象
        SharedPreferences message = getInstance(context);
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = message.edit();
        // 插入一个数据
        editor.putString(key, value);
        // 提交数据
        editor.commit();
    }

    /**
     * 将键值对写入配置文件
     * 
     * @param key
     * @param value
     */
    public static void setInt(Context context, String key, int value) {
        // 创建一个SharedPreferences对象
        SharedPreferences message = getInstance(context);
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = message.edit();
        // 插入一个数据
        editor.putInt(key, value);
        // 提交数据
        editor.commit();
    }

    /**
     * 将键值对写入配置文件
     * 
     * @param key
     * @param value
     */
    public static void setLong(Context context, String key, long value) {
        // 创建一个SharedPreferences对象
        SharedPreferences message = getInstance(context);
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = message.edit();
        // 插入一个数据
        editor.putLong(key, value);
        // 提交数据
        editor.commit();
    }

    /**
     * 获取设置的值
     * 
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getPreferenceSetting(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, defaultValue);
    }
    
    /**
     * 设置的值
     * 
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static void setPreferenceSetting(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        // 插入一个数据
        editor.putString(key, value);
        // 提交数据
        editor.commit();
    }

}
