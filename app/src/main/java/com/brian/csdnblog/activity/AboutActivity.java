
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends BaseActivity {
    
    @BindView(R.id.title_bar) TitleBar mTitleBar;
    @BindView(R.id.blogLink) TextView mAuthorLinkText;
    @BindView(R.id.madeby) TextView mMadeByText;
    @BindView(R.id.blogTV) TextView mAppDesText;
    @BindView(R.id.qq) TextView mQQText;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, AboutActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        initUI();
        initListener();
    }

    private void initUI() {
        mTitleBar.setRightImageResource(R.drawable.ic_share);
        mTitleBar.setTitle("关于");

        String versionName = AppInfoUtil.getVersionName(this);
        if (!TextUtils.isEmpty(versionName)) {
            mMadeByText.setText(mMadeByText.getText() + "  " + versionName);
        }

        mAppDesText.setText("    " + getString(R.string.app_description));
        mQQText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
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

        mQQText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url="mqqwpa://im/chat?chat_type=group&uin=194067225";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
    }
}
