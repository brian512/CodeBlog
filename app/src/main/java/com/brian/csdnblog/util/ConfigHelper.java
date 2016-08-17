package com.brian.csdnblog.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.Env;

/**
 * Created by huamm on 2016/7/31 0031.
 */
public class ConfigHelper {

    public static void init() {
        try {
            ApplicationInfo info = Env.getContext().getPackageManager().getApplicationInfo(Env.getContext().getPackageName(), PackageManager.GET_META_DATA);
            // 调试 (PS:默认为false)
            Config.isDebug = info.metaData.getBoolean("DEBUG_ENABLE");
        } catch (Exception e) {
        }
    }
}
