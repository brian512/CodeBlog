
package com.brian.codeblog.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.brian.codeblog.Env;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.datacenter.DataManager;
import com.brian.codeblog.manager.ShareManager;
import com.brian.codeblog.update.UpdateManager;
import com.brian.codeblog.manager.UsageStatsManager;
import com.brian.common.tools.DayNightHelper;
import com.brian.common.utils.FileUtil;
import com.brian.common.utils.MarketUtils;
import com.brian.common.utils.ToastUtil;
import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.title_bar)               TitleBar mTitleBar;

    @BindView(R.id.switch_night_mode)       TextView mNightModeText;
    @BindView(R.id.switch_show_ad_text)     TextView mSwitchAdText;
    @BindView(R.id.switch_vertical_text)    TextView mSwitchVerticalText;
    @BindView(R.id.switch_picinfwifi_text)  TextView mSwitchPicWifiText;
    @BindView(R.id.switch_staybg_text)      TextView mSwitchStayBgText;
    @BindView(R.id.market)                  TextView mMarketText;
    @BindView(R.id.update)                  TextView mCheckUpdateText;
    @BindView(R.id.cache)                   TextView mClearCacheText;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, SettingActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        initUI();
    }
    
    private void initUI() {
        mTitleBar.setTitle("设置");
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.setRightImageResource(R.drawable.ic_share);
        mTitleBar.setRightListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareManager.getInstance().shareAppToQQ(SettingActivity.this);
            }
        });
        
        mSwitchAdText.setSelected(SettingPreference.getInstance().getAdsEnable());
        mSwitchAdText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "Ad");
                if (SettingPreference.getInstance().getAdsEnable()) {
                    showAdConfirmDialog();
                } else {
                    boolean selected = mSwitchAdText.isSelected();
                    selected = !selected;
                    mSwitchAdText.setSelected(selected);

                    // 保存preference
                    SettingPreference.getInstance().setAdsEnable(selected);
                }
            }
        });
        
        mSwitchVerticalText.setSelected(true);
        mSwitchVerticalText.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "screen");
                boolean selected = mSwitchVerticalText.isSelected();
                selected = !selected;
                mSwitchVerticalText.setSelected(selected);
                
                // 保存preference
//                SettingPreference.setIsVertical(Env.getContext(), selected);
            }
        });
        
        mSwitchPicWifiText.setSelected(SettingPreference.getInstance().getLoadImgOnlyInWifiEnable());
        mSwitchPicWifiText.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "PicWifi");
                boolean selected = mSwitchPicWifiText.isSelected();
                selected = !selected;
                mSwitchPicWifiText.setSelected(selected);
                
                // 保存preference
                SettingPreference.getInstance().setLoadImgOnlyInWifiEnable(selected);
            }
        });
        
        mSwitchStayBgText.setSelected(SettingPreference.getInstance().getRunInBackEnable());
        mSwitchStayBgText.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "bg");
                boolean selected = mSwitchStayBgText.isSelected();
                selected = !selected;
                mSwitchStayBgText.setSelected(selected);
                
                // 保存preference
                SettingPreference.getInstance().setRunInBackEnable(selected);
            }
        });

        mNightModeText.setSelected(SettingPreference.getInstance().getNightModeEnable());
        mNightModeText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_NIGHT, "night_mode");
                boolean selected = mNightModeText.isSelected();
                selected = !selected;
                mNightModeText.setSelected(selected);

                // 保存preference
                SettingPreference.getInstance().setNightModeEnable(selected);

                DayNightHelper.getInstance().setDayNightMode(selected);
            }
        });

        mMarketText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "market");
                MarketUtils.launchAppDetail(getPackageName(), "");
            }
        });

        mCheckUpdateText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "update");
                UpdateManager.getInstance().checkUpdate(Env.getContext());
            }
        });

        mClearCacheText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_LIST, "cache");
                ToastUtil.showMsg("清除了缓存：" + FileUtil.fileSizeToString(DataManager.getInstance().getRemovableDataSize()));
                DataManager.getInstance().clearAllCacheData();
            }
        });
    }
    
    private void showAdConfirmDialog() {
        new AlertDialog.Builder(this).setTitle("真的不让作者赚这点广告费吗？T_T") 
        .setIcon(android.R.drawable.ic_dialog_info) 
        .setPositiveButton("残忍拒绝", new DialogInterface.OnClickListener() { 
     
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
                UsageStatsManager.sendUsageData(UsageStatsManager.SETTING_AD);
                boolean selected = mSwitchAdText.isSelected();
                selected = !selected;
                mSwitchAdText.setSelected(selected);

                // 保存preference
                SettingPreference.getInstance().setAdsEnable(selected);
            } 
        }) 
        .setNegativeButton("支持作者", new DialogInterface.OnClickListener() { 
     
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
            } 
        }).show(); 
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); // 统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
