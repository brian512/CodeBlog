package com.brian.codeblog.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.brian.codeblog.Config;
import com.brian.common.utils.AppInfoUtil;
import com.brian.common.utils.LogUtil;
import com.brian.csdnblog.BuildConfig;

/**
 * 初始化AndroidManifest.xml中的配置信息
 * Created by huamm on 2016/7/31 0031.
 */
public class ConfigHelper {

    public static void init(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            // 打包时动态更改LOG_DEBUG
            Config.DEBUG_ENABLE = BuildConfig.DEBUG_ENABLE;
            LogUtil.mIsDebugMode = Config.DEBUG_ENABLE;

            AppInfoUtil.sChannelName = info.metaData.getString("UMENG_CHANNEL");
        } catch (Exception e) {
            LogUtil.printError(e);
        }
    }
}
