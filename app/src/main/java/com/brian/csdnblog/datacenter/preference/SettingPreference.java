package com.brian.csdnblog.datacenter.preference;

/**
 * Created by Brian on 2016/9/10 0010.
 */
public class SettingPreference extends BasePreference {

    private static final String KEY_LOAD_IMAGE_WIFI = "setting_load_Img";
    private static final String KEY_RUN_IN_BACK     = "setting_run_in_back";
    private static final String KEY_SHOW_ADS        = "setting_show_ads";

    private static SettingPreference sInstance;
    private SettingPreference(){}

    public static final SettingPreference getInstance() {
        if (sInstance == null) {
            synchronized (SettingPreference.class) {
                if (sInstance == null) {
                    sInstance = new SettingPreference();
                }
            }
        }
        return sInstance;
    }

    public void setRunInBackEnable(boolean isInBackEnable) {
        putBoolean(KEY_RUN_IN_BACK, isInBackEnable);
    }

    public void setLoadImgOnlyInWifiEnable(boolean isWifiOnly) {
        putBoolean(KEY_LOAD_IMAGE_WIFI, isWifiOnly);
    }

    public void setAdsEnable(boolean isAdsEnable) {
        putBoolean(KEY_SHOW_ADS, isAdsEnable);
    }

    public boolean getRunInBackEnable() {
        return getBoolean(KEY_RUN_IN_BACK, true);
    }

    public boolean getLoadImgOnlyInWifiEnable() {
        return getBoolean(KEY_LOAD_IMAGE_WIFI, false);
    }

    public boolean getAdsEnable() {
        return getBoolean(KEY_SHOW_ADS, true);
    }
}
