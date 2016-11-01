package com.brian.codeblog.datacenter.dataupdate;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.brian.codeblog.Env;
import com.brian.codeblog.datacenter.preference.SettingPreference;

/**
 * 将设置的preference独立出去
 * Created by huamm on 2016/11/1 0001.
 */
public class V80 {

    private static final String KEY_LOAD_IMAGE_WIFI = "setting_load_Img";
    private static final String KEY_RUN_IN_BACK     = "setting_run_in_back";
    private static final String KEY_SHOW_ADS        = "setting_show_ads";

    public static void updateData() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(Env.getContext());
        SettingPreference settingPreference = SettingPreference.getInstance();

        settingPreference.setAdsEnable(preference.getBoolean(KEY_SHOW_ADS, true));
        settingPreference.setLoadImgOnlyInWifiEnable(preference.getBoolean(KEY_LOAD_IMAGE_WIFI, false));
        settingPreference.setRunInBackEnable(preference.getBoolean(KEY_RUN_IN_BACK, true));
    }
}
