
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.brian.common.view.TitleBar;
import com.brian.csdnblog.BaseActivity;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.ShareManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends BaseActivity {
    
    private TitleBar mTitleBar;

    private TextView tvAuthorLink = null;

    private TextView tvMadeBy = null;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, AboutActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setRightImageResource(R.drawable.ic_share);

        tvAuthorLink = (TextView) findViewById(R.id.blogLink);

        tvMadeBy = (TextView) findViewById(R.id.madeby);

        mTitleBar.setTitle("关于");
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

        tvAuthorLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 友盟统计分享事件
                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_LOOKUP_BLOGER);
            }
        });

        try {
            tvMadeBy.setText(tvMadeBy.getText()
                    + " _"
                    + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); // 统计时长
        MobclickAgent.onPageStart(this.getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

}
