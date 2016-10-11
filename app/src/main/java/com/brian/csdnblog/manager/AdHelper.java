package com.brian.csdnblog.manager;

import android.content.Context;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.datacenter.preference.CommonPreference;
import com.brian.csdnblog.util.LogUtil;

import tj.zl.op.AdManager;
import tj.zl.op.onlineconfig.OnlineConfigCallBack;

/**
 * 广告封装
 * Created by huamm on 2016/10/11 0011.
 */
public class AdHelper {

    /**
     * 在线参数配置是否显示打开广告
     */
    public static boolean isAdCanShow = CommonPreference.getInstance().getAdIsOpened();

    public static void initAd(Context context) {
        AdManager.getInstance(context).init(Constants.APPID, Constants.APPSECTET, Config.isDebug, Config.isDebug);
        AdManager.getInstance(context).asyncGetOnlineConfig("is_ad_open", new OnlineConfigCallBack() {
            @Override
            public void onGetOnlineConfigSuccessful(String key, String value) {
                // 获取在线参数成功
                if (key.equals("is_ad_open")) {
                    isAdCanShow = value.equals("1");
                    CommonPreference.getInstance().setAdIsOpened(isAdCanShow);
                }
            }

            @Override
            public void onGetOnlineConfigFailed(String key) {
                LogUtil.e("failed to get value: " + key);
            }
        });
    }
}
