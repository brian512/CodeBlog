
package com.brian.codeblog.manager;

import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.brian.codeblog.Config;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.NotifyMsgInfo;
import com.brian.codeblog.model.PushInfo;
import com.brian.codeblog.model.UpdateInfo;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.MarketUtils;
import com.google.gson.Gson;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class PushManager {

    private static PushManager sInstance = null;

    private static PushAgent sPushAgent;

    private List<PushInfo> mWaitingTask;
    private List<PushInfo> mHistoryTask;

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

    public void onAppStart() {
        sPushAgent.onAppStart();
    }

    public void initPushMsg(Context context) {
        sPushAgent = PushAgent.getInstance(context.getApplicationContext());
        sPushAgent.setDebugMode(Config.DEBUG_ENABLE);
        sPushAgent.setNotificaitonOnForeground(true); // 应用在前台也显示推送通知
        //注册推送服务，每次调用register方法都会回调该接口
        sPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                LogUtil.log("deviceToken=" + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                LogUtil.logError("s=" + s + "; s1=" + s1);
            }
        });

        UmengMessageHandler messageHandler = new UmengMessageHandler(){
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                LogUtil.log("msg.title=" + msg.title);
//                addPushMessage(msg);
                return super.getNotification(context, msg);
            }
        };
        sPushAgent.setMessageHandler(messageHandler);

        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
                LogUtil.log("msg.custom=" + msg.custom);
                if (!TextUtils.isEmpty(msg.custom) && msg.custom.startsWith("market")) {
                    MarketUtils.launchAppDetail(Uri.parse(msg.custom), "");
                }
            }
        };
        sPushAgent.setNotificationClickHandler(notificationClickHandler);
    }

    public void addPushMessage(UMessage message) {
        if (message == null) {
            return;
        }
        if (checkMsgExist(message.msg_id)) {
            return;
        }

        PushInfo pushInfo = convert2LocalPush(message);
        if(pushInfo != null) {
            mWaitingTask.add(pushInfo);
        }
    }

    private PushInfo convert2LocalPush(UMessage message) {
        PushInfo pushInfo = new PushInfo();
        pushInfo.msgID = message.message_id;
        pushInfo.content = message.text;
        JSONTokener jsonParser = new JSONTokener(pushInfo.content);
        String type = null,value = null;
        try {
            JSONObject pushMsg = (JSONObject) jsonParser.nextValue();
            type = pushMsg.getString("type");
            value = pushMsg.getString("value");
            LogUtil.log("type=" + type + ";value=" + value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(value)) {
            return null;
        }
        pushInfo.type = Integer.valueOf(type);
        switch (pushInfo.type) {
            case PushInfo.TYPE_BLOG:
                pushInfo.blogInfo = new Gson().fromJson(value, BlogInfo.class);
                break;
            case PushInfo.TYPE_NOTIFY:
                pushInfo.notifyMsgInfo =  new Gson().fromJson(value, NotifyMsgInfo.class);
                break;
            case PushInfo.TYPE_REPLY_CHAT:
                pushInfo.chatMsg =  value;
                break;
            case PushInfo.TYPE_REPLY_FEEDBACK:
                pushInfo.notifyMsgInfo =  new Gson().fromJson(value, NotifyMsgInfo.class);
                break;
            case PushInfo.TYPE_UPDATE:
                pushInfo.updateInfo =  new Gson().fromJson(value, UpdateInfo.class);
                break;
        }
        return pushInfo;
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
