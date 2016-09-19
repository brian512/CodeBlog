package com.brian.csdnblog.manager;

import com.brian.csdnblog.datacenter.preference.CommonPreference;
import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.model.event.CurrBlogerEvent;

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
        }
    }
}
