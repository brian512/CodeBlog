package com.brian.csdnblog.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.brian.csdnblog.Config;
import com.brian.csdnblog.R;
import com.brian.csdnblog.datacenter.preference.CommonPreference;
import com.brian.csdnblog.manager.Constants;
import com.brian.csdnblog.manager.DataManager;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.PermissionUtil;
import com.brian.csdnblog.util.UIUtil;
import com.qhad.ads.sdk.adcore.Qhad;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @BindView(R.id.ad_group) FrameLayout mADContainer;

    private boolean mCanJump = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setIsFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 打开调试模式
        MobclickAgent.setDebugMode(Config.isDebug);
        initAD();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);

        if (isFirstLaunch()) {
            createShortCut();// 创建桌面快捷方式

            DataManager.getInstance().onVersionCodeUpgrade();

            updateVersionCode();// 更新版本号
        }

        // 请求权限
        boolean permission = PermissionUtil.checkInitPermission(BaseActivity.getTopActivity());
        if (permission) {
            jumpMainActivityDeLay(2000);
        }
    }

    private void initAD() {
        Qhad.setLogSwitch(this, false);
        SplashAD splashAD = new SplashAD(this, mADContainer, Constants.APPID, Constants.SplashPosID, new SplashADListener() {
            @Override
            public void onADDismissed() {
                LogUtil.d("TX_AD");
                jumpMainActivityDeLay(0); // 广告隐藏后立即跳转
            }
            @Override
            public void onNoAD(int errorCode) {
                LogUtil.d("TX_AD errorCode=" + errorCode);
                jumpMainActivityDeLay(2000); // 没有广告就延迟跳转
            }
            @Override
            public void onADPresent() {
                LogUtil.d("TX_AD");
            }
            @Override
            public void onADClicked() {
                LogUtil.d("TX_AD");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCanJump = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCanJump) {
            jumpMainActivityDeLay(1000);
        }
        mCanJump = true;
    }

    private void jumpMainActivityDeLay(int delay) {
        if (!mCanJump) {
            mCanJump = true;
            return;
        }
        // 延迟进入
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainTabActivity.class));
                finish();
            }
        }, delay);
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
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE_INIT:
                // 如果读取手机状态和写SDCARD被授权
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    jumpMainActivityDeLay(0);
                }else {
                    PermissionUtil.showPermissionDetail(this, "读写权限不可少", true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
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
