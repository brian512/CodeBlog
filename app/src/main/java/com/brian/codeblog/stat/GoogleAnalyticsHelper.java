package com.brian.codeblog.stat;

import android.content.Context;

import com.brian.codeblog.Config;
import com.brian.common.tools.Env;
import com.brian.csdnblog.BuildConfig;
import com.brian.csdnblog.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

/**
 * Google Analytics的封装类
 * <p>
 * 开发文档https://developers.google.com/analytics/devguides/collection/android/v4/
 * <p>
 * Created by huamm on 2016/11/24 0024.
 */

public class GoogleAnalyticsHelper {

    private static final String CATEGORY_CLICK = "category_click";
    private static final String CATEGORY_DATA = "category_data";

    private static Tracker mTracker;

    public void init(Context context) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        mTracker = analytics.newTracker(R.xml.global_tracker);
        analytics.setDryRun(Config.DEBUG_ENABLE);
        analytics.setLocalDispatchPeriod(2);


        mTracker.enableExceptionReporting(true);
        mTracker.enableAutoActivityTracking(true);
        mTracker.enableAdvertisingIdCollection(true);
        mTracker.set("&cs", BuildConfig.FLAVOR); // 渠道号
    }

    public void setUserID(String userID) {
        mTracker.set("&uid", userID);
    }

    public void onEvent(String eventID) {
        mTracker.send(new HitBuilders.EventBuilder().setCategory(CATEGORY_CLICK).setAction(eventID).build());
    }

    public void reportData(String eventID, String key) {
        mTracker.send(new HitBuilders.EventBuilder().setCategory(CATEGORY_DATA).setAction(eventID).setLabel(key).build());
    }

    public void reportData(String eventID, String key, long value) {
        mTracker.send(new HitBuilders.EventBuilder().setCategory(CATEGORY_DATA).setAction(eventID).setLabel(key).setValue(value).build());
    }

    public void reportError(Throwable e) {
        mTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(new StandardExceptionParser(Env.getContext(), null).getDescription(Thread.currentThread().getName(), e))
                .setFatal(false)
                .build());
    }

    public void newPage(String pageName) {
        mTracker.setScreenName(pageName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
