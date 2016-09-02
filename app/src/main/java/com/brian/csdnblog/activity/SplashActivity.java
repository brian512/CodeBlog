package com.brian.csdnblog.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.R;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.PermissionUtil;
import com.brian.csdnblog.util.PreferenceUtil;
import com.brian.csdnblog.util.UIUtil;
import com.qhad.ads.sdk.adcore.Qhad;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (isFirstLaunch()) {
            createShortCut();// 创建桌面快捷方式

            updateVersionCode();// 更新版本号
        }

        // 请求权限
        boolean permission = PermissionUtil.checkInitPermission(BaseActivity.getTopActivity());
        if (permission) {
            jumpMainActivity();
        }

        // 打开调试模式
        MobclickAgent.setDebugMode(Config.isDebug);
        Qhad.setLogSwitch(this, false);
    }

    private void jumpMainActivity() {
        int delayDuration = 2000;
        if (!isFirstLaunch()) {
            delayDuration = 500;
        }
        // 延迟进入
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainTabActivity.class));
                finish();
            }
        }, delayDuration);
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
     *
     * @return
     */
    private boolean isFirstLaunch() {
        boolean isFirstLaunch = true;
        int versionCode = PreferenceUtil.getInt(SplashActivity.this, PreferenceUtil.pre_key_version_code, -1);

        LogUtil.i(TAG, "version_code=" + versionCode);

        if (versionCode < UIUtil.getVersionCode(SplashActivity.this)) {
            isFirstLaunch = true;
        } else {
            isFirstLaunch = false;
        }

        return isFirstLaunch;
    }

    /**
     * 更新preference中的版本号
     */
    private void updateVersionCode() {
        int versionCode = UIUtil.getVersionCode(SplashActivity.this);

        LogUtil.i(TAG, "versionCode=" + versionCode);

        PreferenceUtil.setInt(SplashActivity.this, PreferenceUtil.pre_key_version_code, versionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE_INIT:
                // 如果读取手机状态和写SDCARD被授权
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    jumpMainActivity();
                }else {
                    PermissionUtil.showPermissionDetail(this, "读写权限不可少", true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

}
