
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.ShareManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.util.AppInfoUtil;

public class AboutActivity extends BaseActivity {
    
    private TitleBar mTitleBar;
    private TextView mAuthorLinkText;
    private TextView mMadeByText;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, AboutActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initUI();
        initListener();
    }

    private void initUI() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setRightImageResource(R.drawable.ic_share);
        mTitleBar.setTitle("关于");

        mAuthorLinkText = (TextView) findViewById(R.id.blogLink);
        mMadeByText = (TextView) findViewById(R.id.madeby);

        String versionName = AppInfoUtil.getVersionName(this);
        if (!TextUtils.isEmpty(versionName)) {
            mMadeByText.setText(mMadeByText.getText() + "  " + versionName);
        }
    }

    private void initListener() {
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // 分享按钮监听
        mTitleBar.setRightListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 分享app
                ShareManager.getInstance().shareAppToQQ(AboutActivity.this);
                // 友盟统计分享事件
                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_SHARE, "app");
            }
        });

        mAuthorLinkText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 友盟统计分享事件
                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_LOOKUP_BLOGER);
            }
        });
    }
}
