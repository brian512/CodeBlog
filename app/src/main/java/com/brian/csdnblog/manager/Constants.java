
package com.brian.csdnblog.manager;


/**
 * 常量类
 */
public class Constants {

    public static final String APPID = "1dd13ec61e646b27";
    public static final String APPSECTET = "445a3e3f010f8cb3";

    public static final String URL_CSDN_LOGO = "http://a.hiphotos.baidu.com/image/pic/item/f636afc379310a556b16f08cb04543a982261093.jpg";

    public static final String SHARE_YINGYONGBAO = "http://www.wandoujia.com/apps/com.brian.csdnblog";

    public static String[] TYPES_WORD = {
        "移动开发", "WEB前端", "数据库", "系统运维", "云计算", "架构设计"
    };

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
