package com.brian.csdnblog;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * 内存泄漏跟踪，需要在Application类中集成
 * @author huamm
 */
public class RefWatcherHelper {
    
    private static RefWatcher sRefWatcher;

    /**
     * 集成组件
     * @param application
     */
    public static void install(Application application) {
        if (LeakCanary.isInAnalyzerProcess(application)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(application);
    }
    
    public static void watch(Object object) {
        if (Config.DEBUG_LEAKCANARY_ENABLE && sRefWatcher != null) {
            sRefWatcher.watch(object);
        }
    }
}
