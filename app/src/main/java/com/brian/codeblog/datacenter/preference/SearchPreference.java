package com.brian.codeblog.datacenter.preference;

import com.brian.common.datacenter.preference.BasePreference;

/**
 * 搜索相关的一些数据保存
 * Created by huamm on 2016/11/1 0001.
 */

public class SearchPreference extends BasePreference {

    private static final String KEY_HISTORY_LIST = "pre_key_history_list";


    private static SearchPreference sInstance;
    private SearchPreference(){
        initPreference("search");
    }

    public static SearchPreference getInstance() {
        if (sInstance == null) {
            synchronized (SearchPreference.class) {
                if (sInstance == null) {
                    sInstance = new SearchPreference();
                }
            }
        }
        return sInstance;
    }

    public String getHistoryListJson() {
        return getString(KEY_HISTORY_LIST, "");
    }

    public void setHistoryListJson(String json) {
        putString(KEY_HISTORY_LIST, json);
    }
}
