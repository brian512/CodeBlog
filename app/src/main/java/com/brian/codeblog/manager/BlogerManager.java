package com.brian.codeblog.manager;

import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.model.Bloger;
import com.brian.codeblog.model.event.CurrBlogerEvent;
import com.brian.common.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by huamm on 2016/9/19 0019.
 */
public class BlogerManager {

    private Bloger mCurrBloger;

    private static BlogerManager sInstance;
    private BlogerManager() {}

    public static BlogerManager getsInstance() {
        if (sInstance == null) {
            synchronized (BlogerManager.class) {
                if (sInstance == null) {
                    sInstance = new BlogerManager();
                }
            }
        }
        return sInstance;
    }

    public Bloger getCurrBloger() {
        return Bloger.fromJson(CommonPreference.getInstance().getCurrBlogerJson());
    }

    public void setCurrBloger(Bloger bloger) {
        if (bloger == null) {
            return;
        }
        if (mCurrBloger == null || !mCurrBloger.blogerID.equals(bloger.blogerID)) {
            mCurrBloger = bloger.clone();// 防止静态引用导致内存泄漏
            CommonPreference.getInstance().setCurrBlogerJson(bloger.toJson());

            CurrBlogerEvent event = new CurrBlogerEvent();
            event.bloger = mCurrBloger;
            EventBus.getDefault().post(event);

            LogUtil.log("mCurrBloger=" + mCurrBloger.toJson());
        }
    }
}
