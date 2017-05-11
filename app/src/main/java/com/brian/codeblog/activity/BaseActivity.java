package com.brian.codeblog.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.brian.codeblog.stat.UsageStatsManager;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.ResourceUtil;
import com.brian.csdnblog.R;
import com.umeng.analytics.MobclickAgent;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class BaseActivity extends SwipeBackActivity {

    /**
     * 记录当前最顶部的Activity
     */
    private static BaseActivity sTopActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.log("onCreate：" + getClass().getSimpleName());
        initStatusBar();
        super.onCreate(savedInstanceState);

        sTopActivity = this;
    }
    
    @Override
    protected void onResume() {
        LogUtil.log("onResume：" + getClass().getSimpleName());
        super.onResume();
        UsageStatsManager.onResume(this, getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        LogUtil.log("onPause：" + getClass().getSimpleName());
        UsageStatsManager.onPause(this, getClass().getSimpleName());
        super.onPause();
    }

    @Override
    protected void onStart() {
        LogUtil.log("onStart：" + getClass().getSimpleName());
        super.onStart();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        LogUtil.log("onStop：" + getClass().getSimpleName());
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        super.onStop();
    }

    @Override
    protected void onRestart() {
        LogUtil.log("onRestart：" + getClass().getSimpleName());
        super.onRestart();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        LogUtil.log("onPostCreate：" + getClass().getSimpleName());
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        LogUtil.log("onSaveInstanceState：" + getClass().getSimpleName());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogUtil.log("onRestoreInstanceState：" + getClass().getSimpleName());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.log("onNewIntent：" + getClass().getSimpleName());
        super.onNewIntent(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtil.log("onTrimMemory：" + level + " " + getClass().getSimpleName());
        super.onTrimMemory(level);
    }

    @Override
    protected void onDestroy() {
        LogUtil.log("onDestroy：" + getClass().getSimpleName());
        super.onDestroy();
    }

    /**
     * 获取顶部的Activity
     */
    public static BaseActivity getTopActivity() {
        return sTopActivity;
    }

    /**
     * 初始化状态栏相关，
     * PS: FLAG_TRANSLUCENT_STATUS会让布局占用状态栏
     * SDK23：可以设置状态栏为浅色（SYSTEM_UI_FLAG_LIGHT_STATUS_BAR），字体就回反转为黑色。
     */
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ResourceUtil.getColor(R.color.common_titlebar_bg));
        }
    }

    /**
     * 动态切换是否全屏显示
     */
    public void setFullScreenEnable(boolean enable) {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (enable) {
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN; // 布局占用状态栏，并隐藏状态栏，不影响导航栏
        } else {
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
//        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // 全屏布局，状态栏和导航栏覆盖在布局上
        window.setAttributes(params);
    }

    public static Handler getUIHandler() {
        Handler handler = sTopActivity.getWindow().getDecorView().getHandler();
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
}
