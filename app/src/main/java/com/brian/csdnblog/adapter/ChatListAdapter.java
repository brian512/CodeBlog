package com.brian.csdnblog.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.model.MsgInfo;
import com.brian.csdnblog.util.ViewHolderUtil;

public class ChatListAdapter extends BaseAdapter {

    private List<MsgInfo> mChatList = new ArrayList<MsgInfo>();
    
    public ChatListAdapter() {
    }
    
    public ChatListAdapter(List<MsgInfo> chatList) {
        mChatList = chatList;
    }
    
    public void setChatList(List<MsgInfo> chatList) {
        mChatList = chatList;
        notifyDataSetChanged();
    }
    
    public void addChatItem(MsgInfo info) {
        mChatList.add(info);
        notifyDataSetChanged();
    }
    
    public void attachList(List<MsgInfo> chatList) {
        mChatList.addAll(chatList);
        notifyDataSetChanged();
    }
    
    public void insertList(List<MsgInfo> chatList) {
        mChatList.addAll(0, chatList);
        notifyDataSetChanged();
    }
    
    public boolean isEmpty() {
        return mChatList == null || mChatList.isEmpty();
    }
    
    public List<MsgInfo> getChatList() {
        return mChatList;
    }
    
    @Override
    public int getCount() {
        return mChatList.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        MsgInfo info = mChatList.get(position);
        if (info.id == MsgInfo.SELF) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (getItemViewType(position) == 1) {
                convertView = LayoutInflater.from(Env.getContext()).inflate(R.layout.item_list_chat_right, null);
            } else {
                convertView = LayoutInflater.from(Env.getContext()).inflate(R.layout.item_list_chat_left, null);
            }
        }
        final TextView content = ViewHolderUtil.get(convertView, R.id.chat_content);
        content.setText(mChatList.get(position).text);
        content.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager copy = (ClipboardManager) Env.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                copy.setPrimaryClip(ClipData.newPlainText(null, content.getText()));
                Toast.makeText(Env.getContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        return convertView;
    }
}