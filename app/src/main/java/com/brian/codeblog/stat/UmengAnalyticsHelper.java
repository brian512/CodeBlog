package com.brian.codeblog.stat;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * 友盟统计
 * Created by huamm on 2017/3/29 0029.
 */

public class UmengAnalyticsHelper {

    private Context mContext;

    public void init(Context context) {
        mContext = context.getApplicationContext();
        MobclickAgent.enableEncrypt(true);
        MobclickAgent.setCatchUncaughtExceptions(false);
    }

    public void setDebugMode(boolean isDebug) {
        MobclickAgent.setDebugMode(isDebug);
    }

    public void onResume(Context context) {
        MobclickAgent.onResume(context);
    }
    public void onPause(Context context) {
        MobclickAgent.onPause(context);
    }

    public void onPageStart(Context context, String pageName) {
        MobclickAgent.onPageStart(pageName);
    }
    public void onPageEnd(Context context, String pageName) {
        MobclickAgent.onPageEnd(pageName);
    }

    public void onEvent(String eventID) {
        MobclickAgent.onEvent(mContext, eventID);
    }

    public void onEvent(String eventID, String key) {
        MobclickAgent.onEvent(mContext, eventID, key);
    }

    public void onEvent(String eventID, HashMap<String,String> map) {
        MobclickAgent.onEvent(mContext, eventID, map);
    }

    public void reportError(Throwable e) {
        MobclickAgent.reportError(mContext, e);
    }

    public void reportError(String error) {
        MobclickAgent.reportError(mContext, error);
    }

}
