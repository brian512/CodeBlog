
package com.brian.codeblog.manager;

import android.content.Context;
import android.text.TextUtils;

import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.NotifyMsgInfo;
import com.brian.codeblog.model.PushInfo;
import com.brian.codeblog.model.UpdateInfo;
import com.brian.codeblog.util.DeviceUtil;
import com.brian.codeblog.util.LogUtil;
import com.google.gson.Gson;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class PushManager {

    private static PushManager sInstance = null;

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

    public void initPushMsg(Context context) {
        MiPushClient.registerPush(context, "2882303761517322008", "5871732210008");
        MiPushClient.setUserAccount(context, DeviceUtil.getUUID(), null);
        //打开Log
        LoggerInterface newLogger = new LoggerInterface() {
            @Override
            public void setTag(String tag) {
            }

            @Override
            public void log(String content, Throwable t) {
            }

            @Override
            public void log(String content) {
            }
        };
        Logger.setLogger(context, newLogger);
    }

    public void addPushMessage(MiPushMessage message) {
        if (message == null) {
            return;
        }
        if (checkMsgExist(message.getMessageId())) {
            return;
        }

        PushInfo pushInfo = convert2LocalPush(message);
        mWaitingTask.add(pushInfo);
    }

    private PushInfo convert2LocalPush(MiPushMessage message) {
        PushInfo pushInfo = new PushInfo();
        pushInfo.msgID = message.getMessageId();
        pushInfo.content = message.getContent();
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
