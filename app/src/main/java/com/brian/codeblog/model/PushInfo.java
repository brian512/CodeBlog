package com.brian.codeblog.model;

/**
 * Created by Brian on 2016/7/10 0010.
 */
public class PushInfo {

    public static final int TYPE_BLOG = 1; // 博客推送，打开博客详情页
    public static final int TYPE_NOTIFY = 2; // 提醒推送，弹窗显示内容
    public static final int TYPE_REPLY_CHAT = 3; // 消息回复推送，打开聊天页面
    public static final int TYPE_REPLY_FEEDBACK = 4; // 消息回复推送，打开反馈页面
    public static final int TYPE_UPDATE = 5; // 升级推送

    public String msgID;

    public String title;
    public String summary;
    public int type;

    public String content; // 内容主体，json格式

    public BlogInfo blogInfo; // TYPE_BLOG
    public NotifyMsgInfo notifyMsgInfo; // TYPE_NOTIFY
    public String chatMsg; // TYPE_REPLY_CHAT TYPE_REPLY_FEEDBACK
    public UpdateInfo updateInfo; // TYPE_UPDATE
}
