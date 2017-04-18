package com.brian.codeblog.robot;

import android.text.TextUtils;

import com.brian.codeblog.Config;
import com.brian.common.tools.Env;
import com.brian.codeblog.stat.UsageStatsManager;
import com.brian.codeblog.proctocol.HttpGetChatRequest;
import com.brian.common.datacenter.network.IResponseCallback;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.NetStatusUtil;
import com.brian.common.utils.RandomUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ChatRobot {

    private static final String TAG = ChatRobot.class.getSimpleName();
    
    public static final int ROBOT_TYPE_TULING      = 0;
    
    public static final int ROBOT_TYPE_XIAODOUBI   = 1;
    
    
    
    private static final int CODE_ERROR_KEY         = 40001;
    private static final int CODE_ERROR_EMPTY       = 40002;
    private static final int CODE_ERROR_OUTINDEX    = 40004;
    private static final int CODE_ERROR_FORMAT      = 40007;
    
    private static final int CODE_TYPE_TEXT = 100000;
    private static final int CODE_TYPE_LINK = 200000;
    private static final int CODE_TYPE_NEWS = 302000;
    private static final int CODE_TYPE_MENU = 308000;
    
    private HashMap<ArrayList<String>, ArrayList<String>> mChatMap = null;
    
    private ArrayList<String> mWelcomeReply = null;
    
    private ArrayList<String> mUnknowReply = null;

    private HttpGetChatRequest mHttpRequestClient;
    
    
    private final String[] REPLY_WELCOMES = new String[] {
            "客官，奴家等你好久了",
            "我是小灵，很高兴为您服务",
    };
    
    
    private final String[] REPLY_DEFAULTS = new String[] {
            "好吧，我竟无言以对",
            "你是在逗我呢，还是在逗我呢",
    };
    
    private final String[] REPLY_NONET = new String[] {
            "没网络就不要跟我聊啦",
            "你看看你是不是没网了",
            "没网聊毛线",
            "我掉线了还是你掉线了",
            "没联网你怎么接收我的消息",
    };
    
    
    
    private static ChatRobot sInstance = null;
    
    private ChatRobot() {
        mHttpRequestClient = new HttpGetChatRequest();
    }
    
    public static ChatRobot getInstance() {
        if (sInstance == null) {
            synchronized (ChatRobot.class) {
                if (sInstance == null) {
                    sInstance = new ChatRobot();
                }
            }
        }
        return sInstance;
    }
    
    
    /**
     * [{ "key":["",""], "answer":["",""] }]
     */
    public void initMap() {
        mChatMap = new HashMap<>();
        mUnknowReply = new ArrayList<>();
        mWelcomeReply = new ArrayList<>();
        
        try {
            Gson gson = new Gson();
            
            String chatJson = Config.getChatJson();
            if (!TextUtils.isEmpty(chatJson)) {
                ArrayList<Enty> answers = gson.fromJson(chatJson, new TypeToken<ArrayList<Enty>>() {}.getType());
                if (answers != null && answers.size() > 0) {
                    for (Enty entry : answers) {
                        mChatMap.put(entry.key, entry.answer);
                    }
                }
            }
            
            String welcomeJson = Config.getWelcomeJson();
            if (!TextUtils.isEmpty(welcomeJson)) {
                mWelcomeReply = gson.fromJson(welcomeJson, new TypeToken<ArrayList<String>>() {}.getType());
            }
            
            String unknowJson = Config.getUnknowJson();
            if (!TextUtils.isEmpty(unknowJson)) {
                mUnknowReply = gson.fromJson(unknowJson, new TypeToken<ArrayList<String>>() {}.getType());
            }
            
        } catch (Exception e) {
            UsageStatsManager.reportError(e);
        }
    }
    
    class Enty {
        public ArrayList<String> key;
        public ArrayList<String> answer;
    }
    
    private String getDefaultReply(String msg) {
        
        if (mChatMap == null || mChatMap.isEmpty()) {
            return "";
        }
        
        Set<Entry<ArrayList<String>, ArrayList<String>>> entries = mChatMap.entrySet();
        for (Entry<ArrayList<String>, ArrayList<String>> entry : entries) {
            if (checkContain(entry.getKey(), msg)) {
                ArrayList<String> answers = entry.getValue();
                return answers.get(RandomUtil.getRandInt(answers.size()));
            }
        }
        return "";
    }
    
    private boolean checkContain(ArrayList<String> list, String msg) {
        for (String str : list) {
            if (str.toLowerCase().contains(msg.toLowerCase()) || msg.toLowerCase().contains(str.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    

    public String getRandomWelcome() {
        if (mWelcomeReply != null && !mWelcomeReply.isEmpty()) {
            int index = RandomUtil.getRandInt(mWelcomeReply.size());
            return mWelcomeReply.get(index);
        } else {
            int index = RandomUtil.getRandInt(REPLY_WELCOMES.length);
            return REPLY_WELCOMES[index];
        }
    }
    
    public String getRandomDefault() {
        if (mUnknowReply != null && !mUnknowReply.isEmpty()) {
            int index = RandomUtil.getRandInt(mUnknowReply.size());
            return mUnknowReply.get(index);
        } else {
            int index = RandomUtil.getRandInt(REPLY_DEFAULTS.length);
            return REPLY_DEFAULTS[index];
        }
    }
    
    public void getMessage(String info, OnReplyListener listener) {
        if (listener == null) {
            return;
        }
        info = info.trim();
        if (TextUtils.isEmpty(info)) {
            listener.onReply("");
            return;
        }
        //获取默认的回答
        String answer = getDefaultReply(info);
        if (!TextUtils.isEmpty(answer)) {
            listener.onReply(answer);
            return;
        }
        
        int type = ROBOT_TYPE_TULING;
//        int type = getTypeByInfo(info);
        
        getMessage(type, info, listener);
    }
    
    private void getMessage(int type, String info, final OnReplyListener listener) {
        
        if (type == ROBOT_TYPE_TULING) {
            getTuLingMessage(info, listener);
        } else if (type == ROBOT_TYPE_XIAODOUBI) {
            getXiaoDouBiMessage(info, listener);
        }
    }
    
    private int getTypeByInfo(String info) {
        if (info.contains("天气")) {
            return ROBOT_TYPE_TULING;
        } else {
            return ROBOT_TYPE_XIAODOUBI;
        }
    }
    
    
    private void getXiaoDouBiMessage(final String info, final OnReplyListener listener) {
        
        String url = getXiaoDouBiUrl(info);
        LogUtil.d("url=" + url);

        HttpGetChatRequest.RequestParam param = new HttpGetChatRequest.RequestParam();
        param.url = url;
        mHttpRequestClient.request(param, new IResponseCallback<HttpGetChatRequest.ResultData>() {

            @Override
            public void onSuccess(HttpGetChatRequest.ResultData resultData) {
                String data = resultData.responseMsg;
                LogUtil.v("data=" + data);

                if (TextUtils.isEmpty(data) || data.contains("抱歉")) {
                    getMessage(ROBOT_TYPE_TULING, info, listener);
                    return;
                } else {
                    listener.onReply(data);
                }
            }

            @Override
            public void onError(int rtn, String msg) {

            }

            @Override
            public void onFailure(int errorCode, String msg) {

            }
        });
    }
    
    
    private void getTuLingMessage(String info, final OnReplyListener listener) {
        
        String url = getTulingUrl(info);
        LogUtil.d("url=" + url);
        
        if (!NetStatusUtil.isNetworkAvailable(Env.getContext())) {
            String noNetAns = REPLY_NONET[RandomUtil.getRandInt(REPLY_NONET.length)];
            listener.onReply(noNetAns);
            return;
        }

        HttpGetChatRequest.RequestParam param = new HttpGetChatRequest.RequestParam();
        param.url = url;
        mHttpRequestClient.request(param, new IResponseCallback<HttpGetChatRequest.ResultData>() {
            @Override
            public void onSuccess(HttpGetChatRequest.ResultData resultData) {
                String data = resultData.responseMsg;
                LogUtil.v("data=" + data);
                if (TextUtils.isEmpty(data.trim())
                        || data.contains("我不会说英语")
                        || data.contains("你说的话有错哦")) {
                    listener.onReply(getRandomDefault());
                    return;
                }
                try {
                    JSONObject json = new JSONObject(data);
                    int code = json.optInt("code", CODE_ERROR_FORMAT);
                    if (code / 10000 == 4) {
                        String errorText = json.optString("text");
                        handleError(code, errorText);
                    } else {
                        switch (code) {
                            case CODE_TYPE_TEXT:
//                                listener.onReply(json.optString("text"));
                                break;
                            case CODE_TYPE_LINK:

                                break;
                            case CODE_TYPE_NEWS:

                                break;
                            case CODE_TYPE_MENU:

                                break;
                        }

                        String text = json.optString("text");
                        text = text.replace("<br>", "\n");
                        listener.onReply(text);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int rtn, String msg) {

            }

            @Override
            public void onFailure(int errorCode, String msg) {

            }

            private void handleError(int code, String errorText) {
                switch (code) {
                    case CODE_ERROR_KEY:

                        break;
                    case CODE_ERROR_EMPTY:

                        break;
                    case CODE_ERROR_OUTINDEX:

                        break;
                    case CODE_ERROR_FORMAT:

                        break;
                }
            }
        });
    }
    
    private String getTulingUrl(String info) {
        String url = Config.getTulingUrl();
        if (url.contains("?")) {
            url = url + "&";
        } else {
            url = url + "?";
        }
        return url + "info=" + info + "&time=" + System.currentTimeMillis();
    }
    
    private String getXiaoDouBiUrl(String info) {
        String url = Config.getXiaoDouBiUrl();
        if (url.contains("?")) {
            url = url + "&";
        } else {
            url = url + "?";
        }
        return url + "chat=" + info + "&time=" + System.currentTimeMillis();
    }

    public static interface OnReplyListener {
        void onReply(String reply);
    }
}
