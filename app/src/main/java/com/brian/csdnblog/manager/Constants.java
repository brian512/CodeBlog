
package com.brian.csdnblog.manager;


/**
 * 常量类
 */
public class Constants {
    public static final String DEFAULT_BLOG_USER_ID = "brian512";

    public static final String URL_CSDN_BLOG_BASE = "http://blog.csdn.net";
    public static final String URL_CSDN_BLOG_HOME = "http://blog.csdn.net/type/index.html?&page=1";
    public static final String URL_CSDN_BLOG_COLUMN = "http://blog.csdn.net/type/column.html?&page=1";
    public static final String URL_CSDN_BLOG_EXPERTS = "http://blog.csdn.net/type/experts.html?&page=1";
    public static final String URL_CSDN_BLOG_HOT = "http://blog.csdn.net/type/hot.html?&page=1";

    public static final String URL_CSDN_BLOG_DEFAULT = "http://blog.csdn.net/"
            + DEFAULT_BLOG_USER_ID + "/article/list/1/?viewmode=list";
    public static final String URL_CSDN_BLOG_XIAOWU = "http://blog.csdn.net/wwj_748/article/list/1/?viewmode=list";

    public static final String URL_CSDN_LOGO = "http://a.hiphotos.baidu.com/image/pic/item/f636afc379310a556b16f08cb04543a982261093.jpg";

    public static final String SHARE_YINGYONGBAO = "http://www.wandoujia.com/apps/com.brian.csdnblog";

    public static final String URL_SEARCH = "http://so.csdn.net/so/search/s.do?p=1&t=blog&q=key_word";//&t=blog

    public static String[] TYPES_WORD = {
        "移动开发", "WEB前端", "数据库", "系统运维", "云计算", "架构设计"
    };

    /**
     * 博客类型
     */
    public class DEF_ARTICLE_TYPE {
        public static final int INT_ORIGINAL = 0; // 原创
        public static final int INT_TRANSLATE = 1; // 翻译
        public static final int INT_REPOST = 2; // 转载

        public static final String STR_ORIGINAL = "Original"; // 原创
        public static final String STR_TRANSLATE = "Translated"; // 翻译
        public static final String STR_REPOST = "Repost"; // 转载
    }

    /**
     * 正则表达式
     */
    public class DEF_STR_REGEX {

        // 文章内容页
        public static final String REGEX_DETAILS = "http://blog.csdn.net/(\\w+)/article/details/(\\d+)";
        // 个人博客首页
        public static final String REGEX_BLOG = "http://blog.csdn.net/(\\w+)[/]{0,1}";
        // 类别博客的列表
        public static final String REGEX_CATEGORY = "http://blog.csdn.net/(\\w+)/article/category/(\\d+)";
        // 博客列表
        public static final String REGEX_BLOGLIST = "http://blog.csdn.net/(\\w+)/article/list/(\\d+)";

        public static final String REGEX_LIST = "http://blog.csdn.net/(\\w+)/article/\\w+/(\\d+)";
    }
}
