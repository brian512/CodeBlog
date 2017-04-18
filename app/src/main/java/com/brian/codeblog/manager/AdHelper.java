package com.brian.codeblog.manager;

import android.content.Context;

import com.brian.codeblog.Config;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.common.utils.LogUtil;

import net.youmi.android.AdManager;
import net.youmi.android.onlineconfig.OnlineConfigCallBack;

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
        AdManager.getInstance(context).init(Constants.AD_YOUMI_APPID, Constants.AD_YOUMI_APPSECTET, Config.DEBUG_ENABLE);
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
