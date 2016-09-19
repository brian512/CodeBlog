
package com.brian.csdnblog.manager;

import android.text.TextUtils;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.util.LogUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * @author brian @date 2015-9-8
 */
public class UsageStatsManager {
    private static final boolean DEBUG = Config.isDebug;


    public static final String USAGE_SHARE = "share";
    public static final String USAGE_LOOKUP_BLOGER = "lookup_bloger";
    public static final String USAGE_LOOKUP_QQ = "lookup_qq";

    public static final String USAGE_BLOG_CATE = "blog_cate";
    public static final String USAGE_MAIN_TAB = "main_tab";
    public static final String USAGE_BLOG_COUNT = "blog_count";
    public static final String USAGE_FAVO = "blog_favo";
    
    public static final String MENU_LIST = "menu_list";
    public static final String USAGE_SEARCH = "search";
    public static final String USAGE_FEEDBACK = "feedback";
    public static final String USAGE_SLIDEMENU_SHOW = "show_slidemenu";
    
    public static final String EXP_EMPTY_BLOG = "empty_blog";
    public static final String EXP_EMPTY_LIST = "empty_list";
    public static final String EXP_EMPTY_SEARCH = "empty_search";
    
    public static final String SETTING_LIST = "setting_list";
    public static final String SETTING_AD = "setting_ad";

//    private static ArrayList<HashMap<String, Object>> mUsageDatas = null;
//    private static final int MAX_CACHE_SIZE = 10;

//    private static HandlerThread sHandlerThread = new HandlerThread(
//            "UsageStatsManager");
//    static {
//        sHandlerThread.start();
//    }
//
//    private static final int DEFAULT_SEND_DELAY = 5 * 60 * 1000;
//    private static final int MSG_USAGEDATA_SAVE = 1;
//    private static final int MSG_USAGEDATA_SEND = 2;
//    private static final int MSG_USAGEDATA_ADD = 3;
//
//    private static Handler sHandler = new Handler(sHandlerThread.getLooper()) {
//        @SuppressWarnings("unchecked")
//        public void handleMessage(Message extraMsg) {
//            switch (extraMsg.what) {
//            case MSG_USAGEDATA_ADD:
//                if (mUsageDatas == null) {
//                }
//                if (extraMsg.obj != null && extraMsg.obj instanceof HashMap) {
//                    addUsageData((HashMap<String, Object>) extraMsg.obj);
//                }
//                break;
//            case MSG_USAGEDATA_SEND:
//                removeMessages(MSG_USAGEDATA_SEND);
//                sendEmptyMessageDelayed(MSG_USAGEDATA_SEND, DEFAULT_SEND_DELAY);
//                break;
//            case MSG_USAGEDATA_SAVE:
//                break;
//            default:
//                break;
//            }
//        };
//    };

    /**
     * send analytic data to umeng
     */
    public static void sendUsageData(String key) {
        sendUsageData(key, "");
    }

    public static void sendUsageData(String key, CharSequence value) {
        sendUsageDataToUmeng(key, value);
    }

    private static void sendUsageDataToUmeng(String key, CharSequence value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        HashMap<String, String> values = new HashMap<>();
        values.put(key, value.toString());
        doSendUsageDataToUmeng(key, values);
    }

    private static void doSendUsageDataToUmeng(final String key, final HashMap<String, String> values) {
        LogUtil.d("key=" + key + ";values=" + values);
        if (DEBUG) {
            return;
        }
        MobclickAgent.onEvent(Env.getContext(), key, values);
    }

    public static void reportErrorToUmeng(String error) {
        LogUtil.v("error=" + error);
        if (DEBUG) {
            return;
        }
        MobclickAgent.reportError(Env.getContext(), error);
    }

    public static void reportErrorToUmeng(Throwable error) {
        LogUtil.v("error=" + error);
        if (DEBUG) {
            return;
        }
        MobclickAgent.reportError(Env.getContext(), error);
    }


//    private static void addUsageData(HashMap<String, Object> data) {
//        if (data == null) {
//            return;
//        }
//        mUsageDatas.add(data);
//        sHandler.obtainMessage(MSG_USAGEDATA_SAVE).sendToTarget();
//
//        if (mUsageDatas.size() >= MAX_CACHE_SIZE) {
//            LogUtil.v(MAX_CACHE_SIZE + " datas are ready to be send!");
//            sHandler.obtainMessage(MSG_USAGEDATA_SEND).sendToTarget();
//        } else {
//            LogUtil.v("it's time to send data!");
//            sHandler.sendEmptyMessageDelayed(MSG_USAGEDATA_SEND,
//                    DEFAULT_SEND_DELAY);
//        }
//    }

}
