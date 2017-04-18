package com.brian.codeblog.datacenter.preference;

import com.brian.codeblog.model.Bloger;
import com.brian.common.datacenter.preference.BasePreference;

/**
 * Created by Brian on 2016/9/11 0011.
 */
public class CommonPreference extends BasePreference {

    private static final String KEY_INDICATOR_POSITION = "pre_key_indicator_position";
    private static final String KEY_RECOVERY_LAST_STATUS = "pre_key_recoveryLastStatus";
    private static final String KEY_ARTICLE_TYPE = "pre_key_article_type";
    private static final String KEY_VERSION_CODE = "pre_key_version_code";

    private static final String KEY_BLOG_COUNT = "pre_key_blog_count";
    private static final String KEY_BLOG_TIME = "pre_key_blog_time";

    private static final String KEY_PAY_COUNT = "pre_key_pay_count";

    private static final String KEY_BLOGER_JSON = "pre_key_curr_bloger_json";

    private static final String KEY_AD_SWITCHER = "pre_key_ad_switcher";

    private static CommonPreference sInstance;
    private CommonPreference(){
        initPreference(""); // 采用默认的preference
    }

    public static CommonPreference getInstance() {
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

    public String getCurrBlogerJson() {
        return getString(KEY_BLOGER_JSON, getDefaultBloger());
    }

    public void setCurrBlogerJson(String blogerJson) {
        putString(KEY_BLOGER_JSON, blogerJson);
    }

    public int getBlogReadCount() {
        return getInt(KEY_BLOG_COUNT, 0);
    }

    public void addBlogReadCount() {
        putInt(KEY_BLOG_COUNT, getBlogReadCount() + 1);
    }

    public int getBlogReadTime() {
        return getInt(KEY_BLOG_TIME, 0);
    }

    public void addBlogReadTime(long deltaTime) {
        putInt(KEY_BLOG_TIME, getBlogReadTime() + (int)deltaTime);
    }

    public int getPayCount() {
        return getInt(KEY_PAY_COUNT, 0);
    }

    public void addPayCount(int count) {
        int total = getPayCount() + count;
        if (total < 0) {
            total = 0;
        }
        putInt(KEY_PAY_COUNT, total);
    }

    public boolean getAdIsOpened() {
        return getBoolean(KEY_AD_SWITCHER, false);
    }

    public void setAdIsOpened(boolean isOpened) {
        putBoolean(KEY_AD_SWITCHER, isOpened);
    }

    private String getDefaultBloger() {
        Bloger bloger = new Bloger();
        bloger.homePageUrl = "http://blog.csdn.net/brian512";
        bloger.blogerID = Bloger.getBlogerId(bloger.homePageUrl);
        bloger.blogerType = 256;
        bloger.nickName = "brian512";
        bloger.headUrl = "http://avatar.csdn.net/8/D/9/1_brian512.jpg";
        bloger.bio = "踏踏实实做好一件事，拒绝酱油！";
        return bloger.toJson();
    }

}
