package com.brian.common.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.brian.csdnblog.R;

/**
 * Created by huamm on 2016/8/1 0001.
 */
public class ChatToolLayout extends FrameLayout {

    private TextView mBtSend = null;

    private EditText mEtContent = null;

    public ChatToolLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUI();
    }

    public ChatToolLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatToolLayout(Context context) {
        super(context);
        initUI();
    }

    private void initUI() {
        LayoutInflater.from(getContext()).inflate(R.layout.chat_input_ly, this);

        mEtContent = (EditText) findViewById(R.id.et_content);
        mBtSend = (TextView) findViewById(R.id.bt_send);

        mBtSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onEditFinished();
            }
        });

        mEtContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event!=null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    onEditFinished();
                    return true;
                }
                return false;
            }
        });
    }

    private void onEditFinished() {
        Editable editable = mEtContent.getText();
        if (editable == null || TextUtils.isEmpty(editable.toString())) {
        } else {
            if (mListener != null) {
                mListener.onSendText(editable.toString());
                mEtContent.setText("");
            }
        }
    }

    private OnSendTextListener mListener;

    public void setOnSendTextListener(OnSendTextListener listener) {
        mListener = listener;
    }

    public interface OnSendTextListener {
        void onSendText(String text);
    }
}
