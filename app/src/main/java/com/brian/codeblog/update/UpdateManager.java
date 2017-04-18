package com.brian.codeblog.update;

import android.content.Context;
import android.widget.Toast;

import com.brian.codeblog.Config;
import com.brian.common.tools.Env;
import com.xiaomi.market.sdk.UpdateResponse;
import com.xiaomi.market.sdk.UpdateStatus;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.market.sdk.XiaomiUpdateListener;

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
        XiaomiUpdateAgent.update(Env.getContext(), Config.DEBUG_ENABLE);
        XiaomiUpdateAgent.setCheckUpdateOnlyWifi(false);
    }

    public void checkUpdate(final Context context) {
        XiaomiUpdateAgent.setUpdateAutoPopup(false);
        XiaomiUpdateAgent.setUpdateListener(new XiaomiUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.STATUS_UPDATE: // has update
                        XiaomiUpdateAgent.arrange();
                        break;
                    case UpdateStatus.STATUS_NO_UPDATE: // has no update
                        Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                        break;
                    case UpdateStatus.STATUS_NO_WIFI: // none wifi
                        Toast.makeText(context, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
                        break;
                    case UpdateStatus.STATUS_NO_NET: // time out
                    case UpdateStatus.STATUS_FAILED: // time out
                    case UpdateStatus.STATUS_LOCAL_APP_FAILED: // time out
                        Toast.makeText(context, "服务器访问超时", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        XiaomiUpdateAgent.update(context);
    }
}
