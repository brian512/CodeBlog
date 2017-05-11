
package com.brian.codeblog;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.brian.codeblog.ad.AdMobHelper;
import com.brian.codeblog.manager.ConfigHelper;
import com.brian.codeblog.pay.PayHelper;
import com.brian.common.tools.DayNightHelper;
import com.brian.common.tools.Env;
import com.brian.common.utils.AppInfoUtil;
import com.brian.common.utils.LogUtil;
import com.networkbench.agent.impl.NBSAppAgent;
import com.oasisfeng.condom.CondomContext;
import com.tencent.bugly.Bugly;
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
            LogUtil.w("Config.DEBUG_ENABLE=" + Config.DEBUG_ENABLE);
//            setStrictModeEnable(Config.DEBUG_ENABLE);
            DayNightHelper.getInstance().initDayNightMode();

            AdMobHelper.init(this);
//            AdMobHelper.init(CondomContext.wrap(this, "AdMobSDK"));
            PayHelper.initPay();

            NBSAppAgent.setLicenseKey("46d5cde6e3ea4682b598b9a3101f9c5d")
                    .withLocationServiceEnabled(true)
                    .start(CondomContext.wrap(this, "NBSAppSDK"));
        }
        handleCrash();

//        try {
//            PushManager.getInstance().initPushMsg(this);
//        } catch (Exception e) {}
    }

    private void handleCrash() {
        // init bugly
        CrashReport.setAppChannel(CondomContext.wrap(this, "BuglySDK"), AppInfoUtil.sChannelName);
        Bugly.init(CondomContext.wrap(this, "BuglySDK"), "900033443", false);

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
