package com.brian.codeblog.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.brian.codeblog.Config;
import com.brian.codeblog.datacenter.DataManager;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.manager.AdHelper;
import com.brian.common.tools.Env;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.NetStatusUtil;
import com.brian.common.utils.PermissionUtil;
import com.brian.common.utils.ToastUtil;
import com.brian.common.utils.UIUtil;
import com.brian.csdnblog.R;
import com.umeng.analytics.MobclickAgent;

import net.youmi.android.normal.common.ErrorCode;
import net.youmi.android.normal.spot.SplashViewSettings;
import net.youmi.android.normal.spot.SpotListener;
import net.youmi.android.normal.spot.SpotManager;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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
        MobclickAgent.setDebugMode(Config.DEBUG_ENABLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 请求权限
        if (PermissionUtil.checkInitPermission(BaseActivity.getTopActivity())) {
            doTaskAfterPermission();
        }
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
                }
            }, 200);
        }
    }

    private void delayInitTask() {
        if (isFirstLaunch()) {
            createShortCut();// 创建桌面快捷方式

            DataManager.getInstance().onVersionCodeUpgrade();

            updateVersionCode();// 更新版本号
        }
    }

    private void initAd() {
        Context context = this.getApplicationContext();
        AdHelper.initAd(context);
        if (!AdHelper.isAdCanShow || !NetStatusUtil.isWifiNet(context) || !SettingPreference.getInstance().getAdsEnable()) {
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

    @AfterPermissionGranted(PermissionUtil.PERMISSION_REQUEST_CODE_INIT)
    private void doTaskAfterPermission() {
        if (CommonPreference.getInstance().getPayCount() <= 0) {
            // 有打赏则不显示广告
            initAd();
        }
        jumpMainActivityDeLay(2000); // 防止卡在广告页
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
        // 所有权限都被授予后，会调用注解@AfterPermissionGranted(PERMISSION_REQUEST_CODE_INIT)
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE_INIT) {
            // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
            // This will display a dialog directing them to enable the permission in app settings.
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
//                PermissionUtil.showPermissionDetail(this, "应用必须权限", true);
                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(localIntent);
                ToastUtil.showMsg("必要的权限被禁止，请到应用管理授予权限！", true);
            } else {
                PermissionUtil.checkInitPermission(this);
            }
        }
    }

    /** 开屏页最好禁止用户对返回按钮的控制 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }
}
