package com.brian.codeblog.manager;

import com.brian.codeblog.model.event.TypeChangeEvent;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.stat.UsageStatsManager;

import org.greenrobot.eventbus.EventBus;

public class TypeManager {

    public static final int TYPE_WEB_CSDN = 0x1;
    public static final int TYPE_WEB_OSCHINA = 0x3;
    public static final int TYPE_WEB_ITEYE = 0x4;
    public static final int TYPE_WEB_INFOQ = 0x6;
    public static final int TYPE_WEB_FAVO = 0x7;
    public static final int TYPE_WEB_JCC = 0x8;
    public static final int TYPE_WEB_OSNEWS = 0xD;
    public static final int TYPE_WEB_HISTORY = 0xE;
    public static final int TYPE_WEB_OTHER = 0xF;

    // 需要与弹框列表顺序一致
    public static final int TYPE_CAT_MOBILE = 0x0;
    public static final int TYPE_CAT_WEB = TYPE_CAT_MOBILE + 1;
    public static final int TYPE_CAT_DB = TYPE_CAT_WEB + 1;
    public static final int TYPE_CAT_YUNWEI = TYPE_CAT_DB + 1;
    public static final int TYPE_CAT_CLOUD = TYPE_CAT_YUNWEI + 1;
    public static final int TYPE_CAT_FRAME = TYPE_CAT_CLOUD + 1;
    public static final int TYPE_CAT_BLOGER = 0xE;
    public static final int TYPE_CAT_DEFAULT = 0xF;

    public static int sCateType;

    static {
        initCateType();
    }

    private static void initCateType() {
        sCateType = CommonPreference.getInstance().getArticleType();
        if (sCateType > Constants.TYPES_WORD.length) {
            sCateType = 0;
        }
        UsageStatsManager.reportData(UsageStatsManager.USAGE_BLOG_CATE, Constants.TYPES_WORD[sCateType]);
    }

    public static int initType(int webType) {
        return (webType << 8) +
                (sCateType << 4) + 0;
    }

    public static void setCateType(int cateType) {
        if (sCateType != cateType) {
            sCateType = cateType;
            CommonPreference.getInstance().setArticleType(cateType);

            TypeChangeEvent event = new TypeChangeEvent();
            event.cateType = cateType;
            EventBus.getDefault().post(event);
            UsageStatsManager.reportData(UsageStatsManager.USAGE_BLOG_CATE, Constants.TYPES_WORD[cateType]);
        }
    }

    public static String getCurrCateName() {
        return Constants.TYPES_WORD[sCateType];
    }


    public static int getWebType(int type) {
        return type >> 8;
    }

    public static int getCateType(int type) {
        return (type & 0x00f0) >> 4;
    }

    public static int getLanType(int type) {
        return (type & 0x000f);
    }

    public static int updateWebType(int type, int webType) {
        return (webType << 8) +
                (getCateType(type) << 4) +
                getLanType(type);
    }

    public static int updateCateType(int type, int cateType) {
        return (getWebType(type) << 8) +
                (cateType << 4) +
                getLanType(type);
    }

    public static int updateLanType(int type, int lanType) {
        return (getWebType(type) << 8) +
                (getCateType(type) << 4) +
                lanType;
    }

    /**
     * 根据web和cate拼接
     */
    public static String getBlogName(int type) {
        String name = "";
        int webType = TypeManager.getWebType(type);
        switch (webType) {
            case TypeManager.TYPE_WEB_CSDN:
                name = "CSDN";
                break;
            case TypeManager.TYPE_WEB_INFOQ:
                name = "INFOQ";
                break;
            case TypeManager.TYPE_WEB_OSCHINA:
                name = "OSCHINA";
                break;
            case TypeManager.TYPE_WEB_ITEYE:
                name = "ITEYE";
                break;
            case TypeManager.TYPE_WEB_JCC:
                name = "JCC";
                break;
            case TypeManager.TYPE_WEB_OSNEWS:
                name = "OSNEWS";
                break;
            case TypeManager.TYPE_WEB_FAVO:
                name = "FAVO";
                break;
            case TypeManager.TYPE_WEB_HISTORY:
                name = "HISTORY";
                break;
            case TypeManager.TYPE_WEB_OTHER:
            default:
                name = "OTHER" + webType;
                break;
        }

        name += "_";

        int catType = TypeManager.getCateType(type);
        switch (catType) {
            case TypeManager.TYPE_CAT_MOBILE:
                name += "MOBILE";
                break;
            case TypeManager.TYPE_CAT_DB:
                name += "DB";
                break;
            case TypeManager.TYPE_CAT_CLOUD:
                name += "CLOUD";
                break;
            case TypeManager.TYPE_CAT_FRAME:
                name += "FRAME";
                break;
            case TypeManager.TYPE_CAT_WEB:
                name += "WEB";
                break;
            case TypeManager.TYPE_CAT_YUNWEI:
                name += "YUNWEI";
                break;
            case TypeManager.TYPE_CAT_BLOGER:
                name += "BLOGER";
                break;
            default:
                name += "default";
                break;
        }
        return name;
    }
}
