package com.brian.common.view;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brian.csdnblog.R;
import com.brian.csdnblog.util.ResourceUtil;

/**
 * 通用的对话框，默认显示两个按钮，如果只设置一个按钮的文本，则只会显示一个按钮
 */
public class JDDialog extends JDBaseDialog {
    
    private static final String TAG = JDDialog.class.getSimpleName();

    /**
     * 界面元素
     */
    private Context  mContext;
    private TextView mTitle;
    private TextView mContent;
    private TextView mContentEx;
    private EditText mContentEdit;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private ImageView mBtnSplit;
    
    
    /**
     * 构造函数
     * @param context
     */
    public JDDialog(Context context) {
        super(context, R.style.jd_dialog);
        mContext = context;
        initUI();
    }
    

    /**
     * 初始化对话框
     */
    private void initUI() {
        
        View dlgView = LayoutInflater.from(mContext).inflate(R.layout.jd_dialog, null);

        mTitle      = (TextView) dlgView.findViewById(R.id.jd_dlg_title);
        mContent    = (TextView) dlgView.findViewById(R.id.jd_dlg_content);
        mContentEx  = (TextView) dlgView.findViewById(R.id.jd_dlg_content_ex);
        mContentEdit= (EditText) dlgView.findViewById(R.id.jd_dlg_edit_text);
        mLeftBtn    = (TextView) dlgView.findViewById(R.id.jd_dlg_left_btn);
        mRightBtn   = (TextView) dlgView.findViewById(R.id.jd_dlg_right_btn);
        mBtnSplit   = (ImageView)dlgView.findViewById(R.id.jd_dlg_btn_split);
        
        // 默认隐藏所有按钮，依据设置来进行显示
        mLeftBtn.setVisibility(View.GONE);
        mRightBtn.setVisibility(View.GONE);
        mBtnSplit.setVisibility(View.GONE);
        
        // 默认隐藏jd_dlg_msg_ex
        mContentEx.setVisibility(View.GONE);
        mContentEdit.setVisibility(View.GONE);
        
        // 默认按钮响应
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        };
        setLeftBtnListener(listener);
        setRightBtnListener(listener);

        // 设置对话框内容
        setContentView(dlgView);
    }

    /**
     * 设置标题内容
     * @param str
     */
    public void setTitle(String str) {
        mTitle.setText(str);
    }


    public void setTitle(int strId){
        mTitle.setText(ResourceUtil.getString(strId));
    }


    /**
     * 设置消息内容
     * @param str
     */
    public void setContent(String str) {
        mContent.setText(str);
    }

    /**
     * 设置消息内容
     * @param resID
     */
    public void setContent(int resID) {
        mContent.setText(resID);
    }


    /**
     * 设置消息内容
     * 采用SpannableString类型，用于实现个别字体的颜色不一样
     *
     * @param str
     */
    public void setSpannableMessage(SpannableString str) {
        mContent.setText(str);
    }

    /**
     * 设置消息内容
     * @param str
     */
    public void setContentEx(String str) {
        mContentEx.setText(str);
        mContentEx.setVisibility(View.VISIBLE);
    }


    /**
     * 设置左按钮文本
     * @param str
     */
    public void setLeftBtnText(String str) {
        mLeftBtn.setText(str);
        mLeftBtn.setVisibility(View.VISIBLE);

        // 如果两个按钮都显示，则显示分界线
        if (mRightBtn.getVisibility() == View.VISIBLE) {
            mBtnSplit.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置左边案件是否显示
     *
     * 用于只显示一个按钮的对话框
     * 注意：只支持Visible和Gone两种模式
     */
    public void setLeftBtnVisible(int visible){
        if (visible == View.VISIBLE) {
            mLeftBtn.setVisibility(View.VISIBLE);

            if (mRightBtn.getVisibility() == View.VISIBLE) {
                mBtnSplit.setVisibility(View.VISIBLE);
            }
        } else {
            mLeftBtn.setVisibility(View.GONE);
            mBtnSplit.setVisibility(View.GONE);
        }
    }


    /**
     * 设置右按钮文本
     * @param str
     */
    public void setRightBtnText(String str) {
        mRightBtn.setText(str);
        mRightBtn.setVisibility(View.VISIBLE);

        // 如果两个按钮都显示，则显示分界线
        if (mLeftBtn.getVisibility() == View.VISIBLE) {
            mBtnSplit.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 设置左按钮响应
     * @param listener
     */
    public void setLeftBtnListener(OnClickListener listener) {
        if ((mLeftBtn != null) && (listener != null)) {
            mLeftBtn.setTag(listener);
            mLeftBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    OnClickListener listener = (OnClickListener) mLeftBtn.getTag();
                    listener.onClick(JDDialog.this, 0);
                }
            });
        }
    }

    /**
     * 设置右按钮响应
     * @param listener
     */
    public void setRightBtnListener(OnClickListener listener) {
        if ((mRightBtn != null) && (listener != null)) {
            mRightBtn.setTag(listener);
            mRightBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    OnClickListener listener = (OnClickListener) mRightBtn.getTag();
                    listener.onClick(JDDialog.this, 0);
                }
            });
        }
    }      
    
    /**
     * 切换到编辑模式
     */
    public void setEditMode(){
        mContent.setVisibility(View.GONE);
        mContentEx.setVisibility(View.GONE);
        mContentEdit.setVisibility(View.VISIBLE);
    }
    
    /**
     * 设置输入框的hint
     * @param hint
     */
    public void setEditHint(String hint){
        mContentEdit.setHint(hint);
    }
    
    /**
     * 设置输入框的内容
     * @param text
     */
    public void setEditText(String text){
        mContentEdit.setText(text);
    }
    
    /**
     * 获取输入框内容
     */
    public String getEditContent(){
        return mContentEdit.getText().toString();
    }

}
