
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.brian.common.view.ChatToolLayout;
import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.brian.csdnblog.adapter.ChatListAdapter;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.ChatMsgInfo;
import com.brian.csdnblog.robot.ChatRobot;
import com.brian.csdnblog.robot.ChatRobot.OnReplyListener;
import com.brian.csdnblog.util.DeviceUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity {

    @BindView(R.id.title_bar) TitleBar mTitleBar;
    @BindView(R.id.lv_chatlist) ListView mListView = null;
    @BindView(R.id.input_ly) ChatToolLayout mChatToolLayout;
    private ChatListAdapter mAdapter = null;

    private ChatRobot mRobot = null;

    public static final String BUNDLE_EXTRAS_MSG = "extraMsg";
    public static void startActivity(Activity activity, String msg) {
        Intent intent = new Intent();
        intent.setClass(activity, ChatActivity.class);
        intent.putExtra(BUNDLE_EXTRAS_MSG, msg);
        activity.startActivity(intent);
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, ChatActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initUI();

        String initMsg = getIntent().getStringExtra(BUNDLE_EXTRAS_MSG);
        if (TextUtils.isEmpty(initMsg)) {
            initMsg = mRobot.getRandomWelcome();
        }
        mAdapter.addChatItem(new ChatMsgInfo(ChatMsgInfo.ROBOT, initMsg));

        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                ChatRobot.getInstance().initMap();
            }
        });
    }

    private void initUI() {
        mAdapter = new ChatListAdapter();
        mListView.setAdapter(mAdapter);

        mRobot = ChatRobot.getInstance();

        mTitleBar.setTitle("消息");
        mTitleBar.setRightImageVisible(View.INVISIBLE);
        mTitleBar.setLeftListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mChatToolLayout.setOnSendTextListener(new ChatToolLayout.OnSendTextListener() {
            @Override
            public void onSendText(String text) {
                mAdapter.addChatItem(new ChatMsgInfo(ChatMsgInfo.SELF, text));

                mListView.setSelection(mAdapter.getCount());

                chat(text);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void chat(String text) {
        ChatRobot.getInstance().getMessage(text, new OnReplyListener() {
            @Override
            public void onReply(String reply) {
                mAdapter.addChatItem(new ChatMsgInfo(ChatMsgInfo.ROBOT, reply));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        formatChatList();
    }

    private void formatChatList() {
        List<ChatMsgInfo> chatList = mAdapter.getChatList();
        if (chatList == null || chatList.size() <= 1) {
            return;
        }
        ArrayList<String> list = new ArrayList<>(chatList.size());
        for (ChatMsgInfo chatInfo : chatList) {
            list.add(chatInfo.toString());
        }
        UsageStatsManager.reportErrorToUmeng(new Gson().toJson(list, new TypeToken<ArrayList<String>>() {}.getType()) + "\n DeviceToken=" + DeviceUtil.getUUID());
    }
}
