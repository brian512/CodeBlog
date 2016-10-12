package com.brian.codeblog.datacenter.preference;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.brian.codeblog.Env;

/**
 * Created by Brian on 2016/9/10 0010.
 */
public class BasePreference {

    private SharedPreferences sPreference;

    private SharedPreferences getPreference() {
        if (sPreference == null) {
            synchronized (BasePreference.class) {
                sPreference = PreferenceManager.getDefaultSharedPreferences(Env.getContext().getApplicationContext());
            }
        }
        return sPreference;
    }

    /**
     * 读取对应的键值
     */
    String getString(String key, String defaultValue) {
        return getPreference().getString(key, defaultValue);
    }

    /**
     * 读取对应的键值
     */
    int getInt(String key, int defaultValue) {
        return getPreference().getInt(key, defaultValue);
    }

    /**
     * 读取对应的键值
     */
    long getLong(String key, long defaultValue) {
        return getPreference().getLong(key, defaultValue);
    }

    /**
     * 将键值对写入配置文件
     */
    boolean getBoolean(String key, boolean value) {
        return getPreference().getBoolean(key, value);
    }

    /**
     * 将键值对写入配置文件
     */
    void putString(String key, String value) {
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = getPreference().edit();
        // 插入一个数据
        editor.putString(key, value);
        // 提交数据
        editor.apply();
    }

    /**
     * 将键值对写入配置文件
     */
    void putInt(String key, int value) {
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = getPreference().edit();
        // 插入一个数据
        editor.putInt(key, value);
        // 提交数据
        editor.apply();
    }

    /**
     * 将键值对写入配置文件
     */
    void putLong(String key, long value) {
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = getPreference().edit();
        // 插入一个数据
        editor.putLong(key, value);
        // 提交数据
        editor.apply();
    }

    /**
     * 将键值对写入配置文件
     */
    void putBoolean(String key, boolean value) {
        // 编辑SharedPreferences对象
        SharedPreferences.Editor editor = getPreference().edit();
        // 插入一个数据
        editor.putBoolean(key, value);
        // 提交数据
        editor.apply();
    }

}
