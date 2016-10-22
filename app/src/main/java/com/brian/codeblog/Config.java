
package com.brian.codeblog;

import android.text.TextUtils;

import com.brian.codeblog.manager.Constants;
import com.umeng.onlineconfig.OnlineConfigAgent;

public class Config {

    /**
     * 正式版本打包时自动更改为false
     */
    public static boolean DEBUG_ENABLE = true;
    
    private static final String KEY_TULING = "api_key_tuling";
    private static final String KEY_XIAODOUBI = "api_key_xiaodoubi";
    
    private static final String KEY_TENCENT_APPID = "tencent_appid";
    private static final String KEY_TENCENT_APPKEY = "tencent_appkey";
    
    private static final String KEY_URL_SHARE_APP = "url_share_app";
    
    private static final String KEY_CHAT = "key_chat";
    private static final String KEY_CHAT_WELCOME = "key_chat_welcome";
    private static final String KEY_CHAT_UNKNOW = "key_chat_unknow";
    
    
    public static String getTulingUrl() {
        return getConfigParams(KEY_TULING);
    }
    
    public static String getXiaoDouBiUrl() {
        return getConfigParams(KEY_XIAODOUBI);
    }
    
    public static String getTencentAppID() {
        return getConfigParams(KEY_TENCENT_APPID);
    }
    
    public static String getTencentAppKey() {
        return getConfigParams(KEY_TENCENT_APPKEY);
    }
    
    public static String getShareAppUrl() {
        String url = getConfigParams(KEY_URL_SHARE_APP);
        url = TextUtils.isEmpty(url) ? Constants.SHARE_YINGYONGBAO : url;
        return url;
    }
    
    public static String getWelcomeJson() {
        return getConfigParams(KEY_CHAT_WELCOME);
    }
    
    public static String getUnknowJson() {
        return getConfigParams(KEY_CHAT_UNKNOW);
    }
    
    public static String getChatJson() {
        return getConfigParams(KEY_CHAT);
    }
    
    private static String getConfigParams(String key) {
        return OnlineConfigAgent.getInstance().getConfigParams(Env.getContext(), key);
    }
    
}
