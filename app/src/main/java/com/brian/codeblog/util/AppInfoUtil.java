package com.brian.codeblog.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by huamm on 2016/8/19 0019.
 */
public class AppInfoUtil {

    private static int sMyProcessId = 0;

    /**
     *  客户端渠道名称
     */
    public static String sChannelName;

    /**
     * 获取进程名
     * @return
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = mActivityManager.getRunningAppProcesses();
        if (runningAppProcesses == null || runningAppProcesses.isEmpty()) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


    /**
     * 判断进程是否存在
     * @return
     */
    public static boolean isExistProcessName(Context context, String processName) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据进程名获取进程id
     * @return
     */
    public static int getProcessId(Context con, String processName) {
        if (sMyProcessId == 0) {
            ActivityManager am = (ActivityManager) con.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();// 返回进程列表信息
            for (ActivityManager.RunningAppProcessInfo p : apps) {
                if (p.processName.equals(processName)) {
                    sMyProcessId = p.pid;
                    break;
                }
            }
        }
        return sMyProcessId;
    }

    /**
     * 获取客户端版本号
     */
    private static String sVersionName = "";
    public static String getVersionName(Context context) {
        if (TextUtils.isEmpty(sVersionName)) {
            try {
                sVersionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sVersionName;
    }

    /**
     * 获取客户端版本号
     */
    private static int sVersionCode;
    public static int getVersionCode(Context context) {
        // 只获取一次
        if (sVersionCode == 0) {
            try {
                PackageManager pm = context.getPackageManager();
                String packageName = context.getPackageName();
                PackageInfo pinfo = pm.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
                sVersionCode = pinfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                sVersionCode = 0; // 异常情况
            }
        }
        return sVersionCode;
    }
}
