
package com.brian.codeblog;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.brian.codeblog.util.AppInfoUtil;
import com.brian.codeblog.util.ConfigHelper;
import com.brian.codeblog.util.LogUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

public class App extends Application {

    public static final String PROCESS_NAME_MAIN = "com.brian.csdnblog";

    @Override
    protected void attachBaseContext(Context base) {
        Env.setContext(this);
        LogUtil.log("AppStart");
        // app start here
        Env.setAppStartTime();
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (PROCESS_NAME_MAIN.equals(AppInfoUtil.getProcessName(this))) {
            ConfigHelper.init(this);
            LogUtil.w("Config.isDebug=" + Config.isDebug);
            setStrictModeEnable(Config.isDebug);
        }
        handleCrash();
    }

    private void handleCrash() {
        // init bugly
        CrashReport.initCrashReport(getApplicationContext(), "900033443", false);
        CrashReport.setAppChannel(this, AppInfoUtil.sChannelName);

        // 友盟crash统计，目前使用bugly
        MobclickAgent.setCatchUncaughtExceptions(false);
//        CrashHandler.getInstance().initCrashHandler(getApplicationContext());
    }

    private void setStrictModeEnable(boolean enable) {
        if (!enable) {
            return;
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    /**
     * 退出应用
     */
    public static void exit() {
        MobclickAgent.onKillProcess(Env.getContext());
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
