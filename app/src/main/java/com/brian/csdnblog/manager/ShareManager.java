package com.brian.csdnblog.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.R;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class ShareManager {

    /**
     * 腾讯分享对象的应用
     */
    private Tencent mTencent = null;
    
    private static ShareManager sInstance = null;
    
    private ShareManager() {}
    
    public static ShareManager getInstance() {
        if (sInstance == null) {
            synchronized (ShareManager.class) {
                if (sInstance == null) {
                    sInstance = new ShareManager();
                }
            }
        }
        return sInstance;
    }
    
    private void init(Context context) {
     // 初始化腾讯分享对象
        if (mTencent == null) {
            mTencent = Tencent.createInstance(Config.getTencentAppID(), context);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            mTencent.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
        }
    }
    
    private void shareToQQ(Activity activity, Bundle bundle, IUiListener listener) {
        try {
            init(activity);
            mTencent.shareToQQ(activity, bundle, listener);
        } catch (Exception e) {
        }
    }
    
    public void shareAppToQQ(Activity activity) {
        final Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, activity.getString(R.string.app_name));
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, activity.getString(R.string.app_description));
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, Config.getShareAppUrl());
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, Constants.URL_CSDN_LOGO);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getString(R.string.app_name));
        shareToQQ(activity, bundle, new BaseUiListener());
    }
    
    public void shareBlogToQQ(Activity activity, Bundle bundle) {
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, Constants.URL_CSDN_LOGO);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getString(R.string.app_name));
        shareToQQ(activity, bundle, new BaseUiListener());
    }

    /**
     * 分享需要的
     */
    public static class BaseUiListener implements IUiListener {
        
        @Override
        public void onError(UiError e) {
        }
        
        @Override
        public void onCancel() {
        }
        
        @Override
        public void onComplete(Object response) {
        }
    }
}


