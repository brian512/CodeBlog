package com.brian.csdnblog.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.brian.csdnblog.BuildConfig;
import com.brian.csdnblog.Config;

/**
 * 初始化AndroidManifest.xml中的配置信息
 * Created by huamm on 2016/7/31 0031.
 */
public class ConfigHelper {

    public static void init(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            // 打包时动态更改LOG_DEBUG
            Config.isDebug = BuildConfig.LOG_DEBUG;
            LogUtil.mIsDebugMode = Config.isDebug;

            AppInfoUtil.sChannelName = info.metaData.getString("UMENG_CHANNEL");
        } catch (Exception e) {
            LogUtil.printError(e);
        }
    }
}
