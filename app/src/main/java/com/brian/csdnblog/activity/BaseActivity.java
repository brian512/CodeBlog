package com.brian.csdnblog.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.brian.csdnblog.RefWatcherHelper;
import com.brian.csdnblog.util.LogUtil;
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
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        LogUtil.log("onPause：" + getClass().getSimpleName());
        MobclickAgent.onPause(this);
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
        LogUtil.log("onTrimMemory：" + getClass().getSimpleName());
        super.onTrimMemory(level);
    }

    @Override
    protected void onDestroy() {
        LogUtil.log("onDestroy：" + getClass().getSimpleName());
        super.onDestroy();
        RefWatcherHelper.watch(this);
    }

    /**
     * 获取顶部的Activity
     */
    public static BaseActivity getTopActivity() {
        return sTopActivity;
    }


    private boolean isStatusBarTranslate = false;
    /**
     * 若状态栏的状态为FLAG_TRANSLUCENT_STATUS，则需要在titlebar添加一个填充view
     * @return
     */
    public boolean isStatusBarTranslate() {
        return isStatusBarTranslate;
    }

    private boolean mIsFullScreen = false;

    /**
     * 设置全屏需要在调用super.onCreate(arg0);之前设置setIsFullScreen(true);否则在Android 6.0下非全屏的activity会出错;
     * @param isFullScreen
     */
    public void setIsFullScreen(boolean isFullScreen) {
        mIsFullScreen = isFullScreen;
    }

    /**
     * 初始化状态栏相关，
     * PS: 设置全屏需要在调用super.onCreate(arg0);之前设置setIsFullScreen(true);否则在Android 6.0下非全屏的activity会出错;
     * SDK19：可以设置状态栏透明，但是半透明的SYSTEM_BAR_BACKGROUNDS会不好看；
     * SDK21：可以设置状态栏颜色，并且可以清除SYSTEM_BAR_BACKGROUNDS，但是不能设置状态栏字体颜色（默认的白色字体在浅色背景下看不清楚）；
     * SDK23：可以设置状态栏为浅色（SYSTEM_UI_FLAG_LIGHT_STATUS_BAR），字体就回反转为黑色。
     * 为兼容目前效果，仅在SDK23才显示沉浸式。
     */
    protected void initStatusBar() {
        Window win = getWindow();
        if (mIsFullScreen) {
            win.requestFeature(Window.FEATURE_NO_TITLE);
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持屏幕高亮
        } else {
            //KITKAT也能满足，只是SYSTEM_UI_FLAG_LIGHT_STATUS_BAR（状态栏字体颜色反转）只有在6.0才有效
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
                // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
                win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                // 部分机型的statusbar会有半透明的黑色背景
                win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                win.setStatusBarColor(Color.TRANSPARENT);// SDK21

                isStatusBarTranslate = true;
            }
        }
    }
}
