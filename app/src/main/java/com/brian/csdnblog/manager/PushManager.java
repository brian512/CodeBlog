
package com.brian.csdnblog.manager;

import android.app.Notification;
import android.content.Context;
import android.text.TextUtils;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.NotifyMsgInfo;
import com.brian.csdnblog.model.PushInfo;
import com.brian.csdnblog.model.UpdateInfo;
import com.brian.csdnblog.util.LogUtil;
import com.google.gson.Gson;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.UmengRegistrar;
import com.umeng.message.entity.UMessage;

import java.util.ArrayList;
import java.util.List;

public class PushManager {

    private static PushManager sInstance = null;

    private List<PushInfo> mWaitingTask;
    private List<PushInfo> mHistoryTask;

    private String mDeviceToken;

    private PushManager() {
        mWaitingTask = new ArrayList<>();
        mHistoryTask = new ArrayList<>();
    }

    public static PushManager getInstance() {
        if (sInstance == null) {
            synchronized (ShareManager.class) {
                if (sInstance == null) {
                    sInstance = new PushManager();
                }
            }
        }
        return sInstance;
    }

    public void handlePushMessageIfNeed() {
        if (mWaitingTask.size() > 0) {
            PushInfo info = mWaitingTask.remove(0);
            IntentJumpManager.handleMessage(info);
            mHistoryTask.add(info);
        }
    }

    public String getDeviceToken() {
        return mDeviceToken;
    }

    public void initPushMsg(Context context) {

        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.setDebugMode(Config.isDebug);
        //开启推送并设置注册的回调处理
//        mPushAgent.enable(new IUmengRegisterCallback() {
//            @Override
//            public void onRegistered(String registrationId) {
//                //onRegistered方法的参数registrationId即是device_token
//                LogUtil.e("device_token=" + registrationId);
//            }
//        });
        mPushAgent.enable();
        mDeviceToken = UmengRegistrar.getRegistrationId(context);
        LogUtil.e("device_token=" + mDeviceToken);
        /**
         * 该Handler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(Context context, UMessage message) {
                LogUtil.log(message.toString());
                addPushMessage(message);
            }

            @Override
            public void launchApp(Context context, UMessage message) {
                super.launchApp(context, message);
                LogUtil.log(new Gson().toJson(message));
                addPushMessage(message);
            }
        };
        mPushAgent.setNotificationClickHandler(notificationClickHandler);


        UmengMessageHandler messageHandler = new UmengMessageHandler(){
//            /**
//             * 参考集成文档的1.6.3
//             * http://dev.umeng.com/push/android/integration#1_6_3
//             * */
//            @Override
//            public void dealWithCustomMessage(final Context context, final UMessage extraMsg) {
//                LogUtil.log(extraMsg.toString());
//                addPushMessage(extraMsg);
//            }

            @Override
            public Notification getNotification(Context context, UMessage msg) {
                LogUtil.log(msg.toString());
                addPushMessage(msg);
                return super.getNotification(context, msg);
            }
        };
        mPushAgent.setMessageHandler(messageHandler);
    }

    private void addPushMessage(UMessage message) {
        if (message == null || message.extra == null || message.extra.isEmpty()) {
            return;
        }
        if (checkMsgExist(message.msg_id)) {
            return;
        }
        String type = message.extra.get("type");
        String value = message.extra.get("value");
        LogUtil.log("type=" + type + ";value=" + value);
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(value)) {
            return;
        }
        PushInfo pushInfo = new PushInfo();
        pushInfo.msgID = message.msg_id;
        pushInfo.msg = message.text;
        pushInfo.type = Integer.valueOf(type);
        switch (pushInfo.type) {
            case PushInfo.TYPE_BLOG:
                pushInfo.blogInfo = new Gson().fromJson(value, BlogInfo.class);
                break;
            case PushInfo.TYPE_NOTIFY:
                pushInfo.notifyMsgInfo =  new Gson().fromJson(value, NotifyMsgInfo.class);
                break;
            case PushInfo.TYPE_REPLY_CHAT:
                pushInfo.msg =  value;
                break;
            case PushInfo.TYPE_REPLY_FEEDBACK:
                pushInfo.notifyMsgInfo =  new Gson().fromJson(value, NotifyMsgInfo.class);
                break;
            case PushInfo.TYPE_UPDATE:
                pushInfo.updateInfo =  new Gson().fromJson(value, UpdateInfo.class);
                break;
            default:
                return;
        }
        mWaitingTask.add(pushInfo);
    }

    private boolean checkMsgExist(String msgID) {
        for (PushInfo info : mWaitingTask) {
            if (info.msgID.equals(msgID)) {
                return true;
            }
        }
        for (PushInfo info : mHistoryTask) {
            if (info.msgID.equals(msgID)) {
                return true;
            }
        }
        return false;
    }
}
