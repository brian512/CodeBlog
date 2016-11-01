package com.brian.common.tools;

import android.support.v7.app.AppCompatDelegate;

import com.brian.codeblog.activity.BaseActivity;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.common.view.CommonDialogFragment;

/**
 * 夜间模式辅助类
 * Created by huamm on 2016/10/31 0031.
 */

public class DayNightHelper {

    private boolean mIsDayNightEnabled = false;
    private boolean mIsModeChanged = false;

    private static class SingletonHolder {
        private static final DayNightHelper INSTANCE = new DayNightHelper();
    }
    private DayNightHelper() {
    }
    public static final DayNightHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public boolean isDayNightEnabled() {
        return mIsDayNightEnabled;
    }

    public void initDayNightMode() {
        mIsDayNightEnabled = SettingPreference.getInstance().getNightModeEnable();
        AppCompatDelegate.setDefaultNightMode(mIsDayNightEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void setDayNightMode(boolean enable) {
        if (mIsDayNightEnabled == enable) {
            return;
        }
        mIsModeChanged = true;
        mIsDayNightEnabled = enable;
        AppCompatDelegate.setDefaultNightMode(enable ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        final BaseActivity activity = BaseActivity.getTopActivity();
        CommonDialogFragment.create(activity.getSupportFragmentManager())
                .setContentText("主页面下次启动时才会生效")
                .setPositiveBtnText("确定")
                .show();
    }

    public boolean hasModeChanged() {
        return mIsModeChanged;
    }
}
