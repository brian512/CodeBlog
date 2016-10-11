package com.brian.csdnblog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.datacenter.preference.CommonPreference;
import com.brian.csdnblog.manager.AdHelper;
import com.brian.csdnblog.manager.DataManager;
import com.brian.csdnblog.manager.PushManager;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.PermissionUtil;
import com.brian.csdnblog.util.UIUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import tj.zl.op.normal.common.ErrorCode;
import tj.zl.op.normal.spot.SplashViewSettings;
import tj.zl.op.normal.spot.SpotListener;
import tj.zl.op.normal.spot.SpotManager;

public class SplashActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private FrameLayout mADContainer;

    private boolean mHasInited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullScreenEnable(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 打开调试模式
        MobclickAgent.setDebugMode(Config.isDebug);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mHasInited) {
            mHasInited = true;
            getUIHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    delayInitTask();

                    // 请求权限
                    if (PermissionUtil.checkInitPermission(BaseActivity.getTopActivity())) {
                        initAD();
                        jumpMainActivityDeLay(2000); // 防止卡在广告页
                    }
                }
            }, 50);
        }
    }

    private void delayInitTask() {
        PushManager.getInstance().initPushMsg(Env.getContext());
        if (isFirstLaunch()) {
            createShortCut();// 创建桌面快捷方式

            DataManager.getInstance().onVersionCodeUpgrade();

            updateVersionCode();// 更新版本号
        }
    }

    private void initAD() {
        AdHelper.initAd(this.getApplicationContext());
        if (!AdHelper.isAdCanShow) {
            jumpMainActivityDeLay(1000);
            return;
        }
        SplashViewSettings splashViewSettings = new SplashViewSettings();
        splashViewSettings.setTargetClass(MainTabActivity.class);
        mADContainer = (FrameLayout) findViewById(R.id.splash_container);
        // 使用默认布局参数
        splashViewSettings.setSplashViewContainer(mADContainer);
        SpotManager.getInstance(this).showSplash(this, splashViewSettings, new SpotListener() {
                    @Override
                    public void onShowSuccess() {
                        LogUtil.d(TAG, "YoumiSdk 开屏展示成功");
                        mADContainer.setVisibility(View.VISIBLE);
                        mADContainer.startAnimation(AnimationUtils.loadAnimation(Env.getContext(), R.anim.anim_splash_enter));
                    }

                    @Override
                    public void onShowFailed(int errorCode) {
                        jumpMainActivityDeLay(1000);
                        LogUtil.e("YoumiSdk onShowFailed" + errorCode);
                        switch (errorCode) {
                            case ErrorCode.NON_NETWORK:
                                LogUtil.e(TAG, "YoumiSdk无网络");
                                break;
                            case ErrorCode.NON_AD:
                                LogUtil.e(TAG, "YoumiSdk无广告");
                                break;
                            case ErrorCode.RESOURCE_NOT_READY:
                                LogUtil.e(TAG, "YoumiSdk资源还没准备好");
                                break;
                            case ErrorCode.SHOW_INTERVAL_LIMITED:
                                LogUtil.e(TAG, "YoumiSdk展示间隔限制");
                                break;
                            case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
                                LogUtil.e(TAG, "YoumiSdk控件处在不可见状态");
                                break;
                        }
                    }

                    @Override
                    public void onSpotClosed() {
                        LogUtil.d("YoumiSdk onSpotClosed");
                    }

                    @Override
                    public void onSpotClicked(boolean isWebPage) {
                        LogUtil.d("YoumiSdk onSpotClicked" + isWebPage);
                    }
                });
    }

    private Runnable mJumpTask = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(SplashActivity.this, MainTabActivity.class);
            startActivity(intent);
            finish();
        }
    };
    private void jumpMainActivityDeLay(int delay) {
        // 延迟进入
        BaseActivity.getUIHandler().postDelayed(mJumpTask, delay);
    }

    /**
     * 创建桌面快捷方式
     */
    private void createShortCut() {
        // 创建快捷方式的Intent
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        // 快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(
                getApplicationContext(), R.drawable.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        // 点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
                getApplicationContext(), this.getClass()));
        // 发送广播。OK
        sendBroadcast(shortcutintent);
    }

    /**
     * 通过当前的版本号与preference记录的版本号进行比对
     */
    private boolean isFirstLaunch() {
        int versionCode = CommonPreference.getInstance().getVersionCode();
        LogUtil.i(TAG, "version_code=" + versionCode);

        boolean isFirstLaunch;
        if (versionCode < UIUtil.getVersionCode(getApplication())) {
            isFirstLaunch = true;
        } else {
            isFirstLaunch = false;
        }

        LogUtil.log("isFirstLaunch:" + isFirstLaunch);
        return isFirstLaunch;
    }

    /**
     * 更新preference中的版本号
     */
    private void updateVersionCode() {
        int versionCode = UIUtil.getVersionCode(SplashActivity.this);

        LogUtil.i(TAG, "versionCode=" + versionCode);
        CommonPreference.getInstance().setVersionCode(versionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // 请求权限
        if (PermissionUtil.checkInitPermission(BaseActivity.getTopActivity())) {
            initAD();
            jumpMainActivityDeLay(2000); // 防止卡在广告页
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE_INIT) {
            PermissionUtil.checkInitPermission(BaseActivity.getTopActivity());
        }
    }

    /** 开屏页最好禁止用户对返回按钮的控制 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
