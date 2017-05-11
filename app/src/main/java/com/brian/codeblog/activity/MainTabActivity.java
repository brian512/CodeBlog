
package com.brian.codeblog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.brian.codeblog.App;
import com.brian.codeblog.Config;
import com.brian.codeblog.activity.adapter.MainTabAdapter;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.manager.ShareManager;
import com.brian.codeblog.stat.UsageStatsManager;
import com.brian.codeblog.update.UpdateManager;
import com.brian.common.tools.DayNightHelper;
import com.brian.common.utils.TimeUtil;
import com.brian.common.utils.ToastUtil;
import com.brian.common.view.DrawerArrowDrawable;
import com.brian.csdnblog.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.umeng.onlineconfig.OnlineConfigLog;
import com.umeng.onlineconfig.UmengOnlineConfigureListener;

import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面
 */
public class MainTabActivity extends BaseActivity {
    private static final String TAG = MainTabActivity.class.getSimpleName();

    @BindView(R.id.left_menu) ImageView mBtnMenu;
    @BindView(R.id.right_search) ImageView mBtnSearch;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.drawer_menu) FrameLayout mDrawerContainer;
    // 主界面的页面切换
    @BindView(R.id.pager) ViewPager mViewpager = null;

    private DrawerArrowDrawable mArrowDrawable;

    private MainTabAdapter mTabAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false); // 不可滑动返回，否则容易异常退出
        setContentView(R.layout.activity_main_tab);
        ButterKnife.bind(this);

        MobclickAgent.enableEncrypt(true);

        initUI();
        initListener();
        recoveryUI(); // 恢复上次浏览视图：主要是tab位置
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 延迟初始化非必要组件
        getUIHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 初始化更新模块
                UpdateManager.getInstance().initUpdate();
                initOnlineParams();
            }
        }, 3000);
    }

    private void initUI() {
        mTabAdapter = new MainTabAdapter(getSupportFragmentManager());
        // 视图切换器
        mViewpager.setOffscreenPageLimit(1);// 预先加载页面的数量
        mViewpager.setAdapter(mTabAdapter);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);//设置滑动模式
        mTabLayout.setupWithViewPager(mViewpager);

        mArrowDrawable = new DrawerArrowDrawable(this);
        mArrowDrawable.setColor(getResources().getColor(R.color.white));
        mBtnMenu.setImageDrawable(mArrowDrawable);
    }

    private void recoveryUI() {
        int initPosition = mTabAdapter.getCount()/2;

        if (CommonPreference.getInstance().isNeedRecoveryLastStatus()) {
            // 读取上次退出时停留的页面序号
            initPosition = CommonPreference.getInstance().getIndicatorPosition(initPosition);
        }
        mViewpager.setCurrentItem(initPosition, false);

        UsageStatsManager.reportData(UsageStatsManager.USAGE_MAIN_TAB, mTabAdapter.getPageTitle(initPosition));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ShareManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    private void initListener() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mArrowDrawable.setProgress(slideOffset);
            }
        });

        mBtnMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });
        mBtnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.startActivity(MainTabActivity.this);

                UsageStatsManager.reportData(UsageStatsManager.USAGE_SEARCH);
            }
        });

        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                UsageStatsManager.reportData(UsageStatsManager.USAGE_MAIN_TAB, mTabAdapter.getPageTitle(position));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void toggleMenu() {
        if (mDrawerLayout.isDrawerOpen(mDrawerContainer)) {
            mDrawerLayout.closeDrawer(mDrawerContainer);
        } else {
            mDrawerLayout.openDrawer(mDrawerContainer);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(mDrawerContainer)) {
                mDrawerLayout.closeDrawer(mDrawerContainer);
                return true;
            }
            boolean isRunInBack = SettingPreference.getInstance().getRunInBackEnable();
            if (isRunInBack) {
                moveTaskToBack(true);
            } else {
                finish();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleMenu();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        PushManager.getInstance().handlePushMessageIfNeed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        CommonPreference.getInstance().setIndicatorPosition(mViewpager.getCurrentItem());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        int count = CommonPreference.getInstance().getBlogReadCount();
        String time = TimeUtil.convCountTime(CommonPreference.getInstance().getBlogReadTime());
        ToastUtil.showMsg(String.format(Locale.CHINA, "累计学习 %d篇博文，用时 %s", count, time));
        OnlineConfigAgent.getInstance().removeOnlineConfigListener();
        super.onDestroy();

        if (DayNightHelper.getInstance().hasModeChanged()) {
            getUIHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    App.exit();
                }
            }, 500);
        }
    }

    // 当启动模式为singletask，重新被启动时调用
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 若参数中无
        int initPosition = CommonPreference.getInstance().getIndicatorPosition(mTabAdapter.getCount()/2);
        mViewpager.setCurrentItem(initPosition, false);

        getIntent().putExtras(intent);// 共享数据
    }

    private void initOnlineParams() {
        OnlineConfigAgent.getInstance().setDebugMode(Config.DEBUG_ENABLE);
        OnlineConfigAgent.getInstance().updateOnlineConfig(this);

        OnlineConfigAgent.getInstance().setOnlineConfigListener(new UmengOnlineConfigureListener() {
            @Override
            public void onDataReceived(JSONObject json) {
                OnlineConfigLog.d("OnlineConfig", "json=" + json);
            }
        });
    }

}
