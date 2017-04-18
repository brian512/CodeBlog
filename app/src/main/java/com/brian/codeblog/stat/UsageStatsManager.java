
package com.brian.codeblog.stat;

import android.content.Context;

import com.brian.codeblog.Config;
import com.brian.common.utils.LogUtil;

/**
 * @author brian @date 2015-9-8
 */
public class UsageStatsManager {
    public static final String TAG = UsageStatsManager.class.getSimpleName();
    /**
     * 开关总入口
     */
//    public static final boolean ENABLE = true;// 始终上报
    public static final boolean ENABLE = !Config.DEBUG_ENABLE;// 非调试模式才上报


    public static final String USAGE_SHARE = "share";
    public static final String USAGE_LOOKUP_BLOGER = "lookup_bloger";
    public static final String USAGE_LOOKUP_QQ = "lookup_qq";

    public static final String USAGE_BLOG_CATE = "blog_cate";
    public static final String USAGE_MAIN_TAB = "main_tab";
    public static final String USAGE_BLOG_COUNT = "blog_count";
    public static final String USAGE_FAVO = "blog_favo";

    public static final String USAGE_BLOGER_ENTR = "bloger_entrance";

    public static final String MENU_LIST = "menu_list";
    public static final String MENU_CONTENT_LIST = "menu_content_list";
    public static final String USAGE_SEARCH = "search";
    public static final String USAGE_FEEDBACK = "feedback";
    public static final String USAGE_SLIDEMENU_SHOW = "show_slidemenu";
    
    public static final String EXP_EMPTY_BLOG = "empty_blog";
    public static final String EXP_EMPTY_LIST = "empty_list";
    public static final String EXP_EMPTY_SEARCH = "empty_search";
    
    public static final String SETTING_LIST = "setting_list";
    public static final String SETTING_AD = "setting_ad";
    public static final String SETTING_NIGHT= "setting_night";

    /**
     * 是否进行Google上报
     */
    public static final boolean ENABLE_GOOGLE = true;

    public static final boolean ENABLE_UMENG= true;


    private static GoogleAnalyticsHelper mGAHelper;
    private static UmengAnalyticsHelper mUmengHelper;


    public static void init(Context context) {
        mGAHelper = new GoogleAnalyticsHelper();
        mGAHelper.init(context);

        mUmengHelper = new UmengAnalyticsHelper();
        mUmengHelper.init(context);
        mUmengHelper.setDebugMode(ENABLE);
    }

    /**
     * 不带参数的统计
     */
    public static void reportData(String eventID) {
        if (ENABLE) {
            try {
                if (ENABLE_GOOGLE) {
                    mGAHelper.onEvent(eventID);
                }
                if (ENABLE_UMENG) {
                    mUmengHelper.onEvent(eventID);
                }
            } catch (Throwable error) {}
        }
        LogUtil.log("eventID = " + eventID);
    }

    public static void reportData(String eventID, String key) {
        if (ENABLE) {
            try {
                if (ENABLE_GOOGLE) {
                    mGAHelper.reportData(eventID, key);
                }
                if (ENABLE_UMENG) {
                    mUmengHelper.onEvent(eventID, key);
                }
            } catch (Throwable error) {}
        }
        LogUtil.log("eventID=" + eventID + "; key=" + key);
    }

    /**
     * 带参数的统计
     */
    public static void reportData(String eventID, String key, long value) {
        if (ENABLE) {
            if (ENABLE_GOOGLE) {
                mGAHelper.reportData(eventID, key, value);
            }
        }
        LogUtil.log("eventID = " + eventID + "; key/value:" + key + ":" + value);
    }

    public static void reportError(String error) {
        if (ENABLE) {
            try {
                if (ENABLE_UMENG) {
                    mUmengHelper.reportError(error);
                }
            } catch (Throwable e) {}
        }
        LogUtil.logError(error);
    }

    public static void reportError(Throwable e) {
        if (ENABLE) {
            try {
                if (ENABLE_GOOGLE) {
                    mGAHelper.reportError(e);
                }
                if (ENABLE_UMENG) {
                    mUmengHelper.reportError(e);
                }
            } catch (Throwable error) {}
        }
        LogUtil.printError(e);
    }

    public static void onResume(Context context, String pageName) {
        if (ENABLE) {
            try {
                if (ENABLE_GOOGLE) {
                    mGAHelper.newPage(pageName);
                }
                if (ENABLE_UMENG) {
                    mUmengHelper.onResume(context);
                }
            } catch (Throwable error) {}
        }
        LogUtil.log(pageName);
    }

    public static void onPause(Context context, String pageName) {
        if (ENABLE) {
            try {
                if (ENABLE_UMENG) {
                    mUmengHelper.onPause(context);
                }
            } catch (Throwable error) {}
        }
        LogUtil.log(pageName);
    }

    public static void onPageStart(Context context, String pageName) {
        if (ENABLE) {
            try {
                if (ENABLE_UMENG) {
                    mUmengHelper.onPageStart(context, pageName);
                }
            } catch (Throwable error) {}
        }
        LogUtil.log(pageName);
    }

    public static void onPageEnd(Context context, String pageName) {
        if (ENABLE) {
            try {
                if (ENABLE_UMENG) {
                    mUmengHelper.onPageEnd(context, pageName);
                }
            } catch (Throwable error) {}
        }
        LogUtil.log(pageName);
    }

    public static void setUserID(String userID) {
        if (ENABLE) {
            if (ENABLE_GOOGLE) {
                mGAHelper.setUserID(userID);
            }
        }
        LogUtil.log(userID);
    }
}
