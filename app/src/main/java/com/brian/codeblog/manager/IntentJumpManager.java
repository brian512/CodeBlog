package com.brian.codeblog.manager;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.brian.codeblog.activity.BaseActivity;
import com.brian.codeblog.activity.BlogContentActivity;
import com.brian.codeblog.activity.ChatActivity;
import com.brian.codeblog.model.NotifyMsgInfo;
import com.brian.codeblog.model.PushInfo;
import com.brian.codeblog.util.LogUtil;

/**
 * 消息跳转
 * Created by Brian on 2016/7/10 0010.
 */
public class IntentJumpManager {

    public static boolean handleMessage(PushInfo pushInfo) {
        switch (pushInfo.type) {
            case PushInfo.TYPE_BLOG:
                BlogContentActivity.startActvity(BaseActivity.getTopActivity(), pushInfo.blogInfo);
                break;
            case PushInfo.TYPE_REPLY_CHAT:
            case PushInfo.TYPE_REPLY_FEEDBACK:
                ChatActivity.startActivity(BaseActivity.getTopActivity(), pushInfo.chatMsg);
                break;
            case PushInfo.TYPE_NOTIFY:
                showNotifyMessage(pushInfo.notifyMsgInfo);
                break;
            case PushInfo.TYPE_UPDATE:
//                pushInfo.updateInfo;
                break;
        }
        return false;
    }

    public static void showNotifyMessage(NotifyMsgInfo message) {
        LogUtil.log(message.title + ":" +  message.content);
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.getTopActivity()); // 先得到构造器
        builder.setTitle(message.title); // 设置标题
        builder.setMessage(message.content); // 设置内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // 设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 关闭dialog
            }
        });
        // 参数都设置完成了，创建并显示出来
        builder.create().show();
    }
}
