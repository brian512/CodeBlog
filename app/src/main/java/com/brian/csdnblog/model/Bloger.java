
package com.brian.csdnblog.model;

import android.graphics.Bitmap;

/**
 * 博主个人信息
 */
public class Bloger {

    public static final String BLOGER_ID = "bloger_id";
    public static final String BLOG_PAGE = "blog_page";

    public static final String BLOGER_NAME = "blogername";
    public static final String BLOGER_FACEURL = "faceurl";
    public static final String BLOGER_FACEPATH = "facePath";
    public static final String BLOGER_VISIT = "visit";
    public static final String BLOGER_CREDITS = "credits";
    public static final String BLOGER_RANK = "rank";
    public static final String BLOGER_GRADE = "grade";
    public static final String BLOGER_ORIGINAL = "original";
    public static final String BLOGER_REPOST = "repost";
    public static final String BLOGER_TRANSLATE = "translate";
    public static final String BLOGER_COMMENT = "comment";

    public String blogerID; // 博主ID
    public String faceURL; // 博主头像
    public Bitmap faceBitmap; // 博主头像
    public String facePath; // 博主头像
    public String blogerName; // 博主名称
    public String visit; // 访问
    public String credits; // 积分
    public String rank; // 排名
    public String grade; // 等级
    public String original; // 原创
    public String repost; // 转载
    public String translate; // 翻译
    public String comment; // 评论
    
    
    public static Bloger getDefaultBloger() {
        Bloger bloger = new Bloger();
        bloger.blogerID = "brian512";
        bloger.blogerName = "年少的风";
        
        bloger.visit = "访问：0次";
        bloger.credits = "积分：0";
        bloger.grade = "等级：BLOG 0";
        bloger.rank = "排名：千里之外";
        
        bloger.original = "原创：0篇";
        bloger.repost = "转载：0篇";
        bloger.translate = "译文：0篇";
        bloger.comment = "评论：0条";
        
        return bloger;
    }
    
    
}
