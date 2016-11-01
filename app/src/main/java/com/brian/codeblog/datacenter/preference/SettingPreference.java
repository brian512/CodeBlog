package com.brian.codeblog.datacenter.preference;

/**
 * 设置相关的偏好
 * Created by Brian on 2016/9/10 0010.
 */
public class SettingPreference extends BasePreference {

    private static final String KEY_NIGHT_MODE      = "setting_night_mode";
    private static final String KEY_LOAD_IMAGE_WIFI = "setting_load_Img";
    private static final String KEY_RUN_IN_BACK     = "setting_run_in_back";
    private static final String KEY_SHOW_ADS        = "setting_show_ads";

    private static SettingPreference sInstance;
    private SettingPreference(){
        initPreference("setting");
    }

    public static SettingPreference getInstance() {
        if (sInstance == null) {
            synchronized (SettingPreference.class) {
                if (sInstance == null) {
                    sInstance = new SettingPreference();
                }
            }
        }
        return sInstance;
    }

    public void setRunInBackEnable(boolean enable) {
        putBoolean(KEY_RUN_IN_BACK, enable);
    }

    public void setLoadImgOnlyInWifiEnable(boolean enable) {
        putBoolean(KEY_LOAD_IMAGE_WIFI, enable);
    }

    public void setNightModeEnable(boolean enable) {
        putBoolean(KEY_NIGHT_MODE, enable);
    }

    public void setAdsEnable(boolean isAdsEnable) {
        putBoolean(KEY_SHOW_ADS, isAdsEnable);
    }

    public boolean getRunInBackEnable() {
        return getBoolean(KEY_RUN_IN_BACK, false);
    }

    public boolean getLoadImgOnlyInWifiEnable() {
        return getBoolean(KEY_LOAD_IMAGE_WIFI, false);
    }

    public boolean getAdsEnable() {
        return getBoolean(KEY_SHOW_ADS, true);
    }

    public boolean getNightModeEnable() {
        return getBoolean(KEY_NIGHT_MODE, false);
    }
}
