package com.brian.codeblog.update;

import android.content.Context;
import android.view.View;

import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.ui.UILifecycleListener;

/**
 * Created by huamm on 2016/9/19 0019.
 */
public class UpdateManager {

    private static UpdateManager sInstance;
    private UpdateManager() {}

    public static UpdateManager getInstance() {
        if (sInstance == null) {
            synchronized (UpdateManager.class) {
                if (sInstance == null) {
                    sInstance = new UpdateManager();
                }
            }
        }
        return sInstance;
    }

    public void initUpdate() {
        Beta.initDelay = 3_000;
        Beta.enableNotification = false;
        Beta.autoDownloadOnWifi = true;

        Beta.upgradeDialogLifecycleListener = new UILifecycleListener<UpgradeInfo>() {

            @Override
            public void onCreate(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onStart(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onResume(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onPause(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onStop(Context context, View view, UpgradeInfo upgradeInfo) {

            }

            @Override
            public void onDestroy(Context context, View view, UpgradeInfo upgradeInfo) {

            }
        };
    }

    public void checkUpdate(final Context context) {
        Beta.checkUpgrade(true, true);
    }
}
