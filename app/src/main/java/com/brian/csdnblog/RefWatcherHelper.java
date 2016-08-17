package com.brian.csdnblog;

import android.app.Application;

/**
 * 内存泄漏跟踪，需要在Application类中集成
 * @author huamm
 */
public class RefWatcherHelper {
    
//    private static RefWatcher sRefWatcher;
//
//    /**
//     * 集成组件
//     * @param application
//     */
//    public static void install(Application application) {
//        if (Config.DEBUG_LEAKCANARY_ENABLE) {
//            sRefWatcher = LeakCanary.install(application);
//        } 
//    }
    
    public static void watch(Object object) {
//        if (Config.DEBUG_LEAKCANARY_ENABLE && sRefWatcher != null) {
//            sRefWatcher.watch(object);
//        }
    }
}
