package com.brian.csdnblog.datacenter.preference;

/**
 * Created by Brian on 2016/9/11 0011.
 */
public class CommonPreference extends BasePreference {

    private static final String KEY_INDICATOR_POSITION = "pre_key_indicator_position";
    private static final String KEY_RECOVERY_LAST_STATUS = "pre_key_recoveryLastStatus";
    private static final String KEY_ARTICLE_TYPE = "pre_key_article_type";
    private static final String KEY_VERSION_CODE = "pre_key_version_code";

    private static CommonPreference sInstance;
    private CommonPreference(){}

    public static final CommonPreference getInstance() {
        if (sInstance == null) {
            synchronized (CommonPreference.class) {
                if (sInstance == null) {
                    sInstance = new CommonPreference();
                }
            }
        }
        return sInstance;
    }

    public void setIndicatorPosition(int position) {
        putInt(KEY_INDICATOR_POSITION, position);
    }

    public void setRecoveryLastStatus(boolean isNeedRecovery) {
        putBoolean(KEY_RECOVERY_LAST_STATUS, isNeedRecovery);
    }

    public void  setArticleType(int articleType) {
        putInt(KEY_ARTICLE_TYPE, articleType);
    }

    public void setVersionCode(int versionCode) {
        putInt(KEY_VERSION_CODE, versionCode);
    }

    public int getIndicatorPosition() {
        return getInt(KEY_INDICATOR_POSITION, 0);
    }

    public int getIndicatorPosition(int defaultValue) {
        return getInt(KEY_INDICATOR_POSITION, defaultValue);
    }

    public boolean isNeedRecoveryLastStatus() {
        return getBoolean(KEY_RECOVERY_LAST_STATUS, true);
    }

    public int getArticleType() {
        return getInt(KEY_ARTICLE_TYPE, 0);
    }

    public int getVersionCode() {
        return getInt(KEY_VERSION_CODE, 0);
    }

}
