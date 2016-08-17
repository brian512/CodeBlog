
package com.brian.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brian.csdnblog.BaseActivity;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.util.DeviceUtil;

/**
 * 首页的标题栏
 */
public class TitleBar extends LinearLayout {

    /**
     * UI元素
     */
    private TextView mLeftText;
    private TextView mRightText;
    private TextView mTitleText;

    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUI(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
    }

    public TitleBar(Context context) {
        super(context);
        initUI(context);
    }

    private void initUI(Context ctx) {
        LayoutInflater.from(ctx).inflate(R.layout.layout_common_title_bar, this);

        mLeftText = (TextView) findViewById(R.id.left_text);
        mRightText = (TextView) findViewById(R.id.right_text);
        mTitleText = (TextView) findViewById(R.id.title_text);
        setStatusBarVisible(BaseActivity.getTopActivity().isStatusBarTranslate());
    }

    private void setStatusBarVisible(boolean visible) {
        View statusBar = findViewById(R.id.status_bar);
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        if (visible) {
            params.height = DeviceUtil.getStatusHeightNew(Env.getContext());
        } else {
            params.height = 0;
        }
        statusBar.setLayoutParams(params);
    }

    /**
     * 左边按钮点击
     * 
     * @param l
     */
    public void setLeftListener(OnClickListener l) {
        mLeftText.setOnClickListener(l);
    }

    /**
     * 设置左边按钮的显示 默认是显示
     */
    public void setLeftImageVisible(int visible) {
        mLeftText.setVisibility(visible);
    }

    /**
     * 设置左边按钮图标 默认是返回键
     * 
     * @param resourceID
     */
    public void setLeftImageResource(int resourceID) {
        mLeftText.setBackgroundResource(resourceID);
    }

    /**
     * 右边按钮点击
     * 
     * @param l
     */
    public void setRightListener(OnClickListener l) {
        mRightText.setOnClickListener(l);
    }

    /**
     * 设置左边按钮的显示 默认不显示
     */
    public void setRightImageVisible(int visible) {
        mRightText.setVisibility(visible);
    }

    /**
     * 设置右边按钮图标 没有默认图标，需要自己设置
     * 
     * @param resourceID
     */
    public void setRightImageResource(int resourceID) {
        mRightText.setBackgroundResource(resourceID);
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        mTitleText.setText(title);
    }

    /**
     * 设置标题
     * 
     * @param resourceID
     */
    public void setTitleResource(int resourceID) {
        mTitleText.setText(resourceID);
    }

    /**
     * 设置title是否显示 默认显示title，没有文字
     * 
     * @param visible
     */
    public void setTitleVisible(int visible) {
        mTitleText.setVisibility(visible);
    }

    /**
     * 设置title 颜色
     */
    public void setTitleTextColor(int color) {
        mTitleText.setTextColor(color);
    }


    /**
     * 获取右边按钮
     * 
     * @return
     */
    public TextView getRightButton() {
        return mRightText;
    }

    /**
     * 获取左边按钮
     * 
     * @return
     */
    public TextView getLeftButton() {
        return mLeftText;
    }
}
