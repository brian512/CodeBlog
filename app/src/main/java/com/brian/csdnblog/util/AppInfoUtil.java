package com.brian.csdnblog.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.List;

/**
 * Created by huamm on 2016/8/19 0019.
 */
public class AppInfoUtil {

    private static int mgprocessId = 0;

    /**
     * 获取进程名
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


    /**
     * 判断进程是否存在
     *
     * @param context
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
     *
     * @param con
     * @return
     */
    public static int getProcessId(Context con, String processName) {
        if (mgprocessId == 0) {
            ActivityManager am = (ActivityManager) con.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();// 返回进程列表信息
            for (ActivityManager.RunningAppProcessInfo p : apps) {
                if (p.processName.equals(processName)) {
                    mgprocessId = p.pid;
                    break;
                }
            }
        }
        return mgprocessId;
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
