
package com.brian.csdnblog.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingPreference {
    private static final String PREFER_NAME = "Setting";

    public static void setIsShowAd(Context context, boolean selected) {
        String key = "setting_show_ad";
        setBoolean(context, key, selected);
    }

    public static boolean getIsShowAd(Context context) {
        String key = "setting_show_ad";
        return getBoolean(context, key, true);
    }
    
    public static void setIsVertical(Context context, boolean selected) {
        String key = "setting_vertical";
        setBoolean(context, key, selected);
    }
    
    public static boolean getIsVertical(Context context) {
        String key = "setting_vertical";
        return getBoolean(context, key, true);
    }
    
    public static void setIsShowPicOnInWifi(Context context, boolean selected) {
        String key = "setting_pic_wifi";
        setBoolean(context, key, selected);
    }
    
    public static boolean getIsShowPicOnInWifi(Context context) {
        String key = "setting_pic_wifi";
        return getBoolean(context, key, false);
    }
    
    public static void setIsStayBg(Context context, boolean selected) {
        String key = "setting_stay_background";
        setBoolean(context, key, selected);
    }
    
    public static boolean getIsStayBg(Context context) {
        String key = "setting_stay_background";
        return getBoolean(context, key, true);
    }
    
    private static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }
    
    public static void setBoolean(Context context, String key, boolean selected) {
        SharedPreferences sp = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, selected);
        edit.commit();
    }

}
