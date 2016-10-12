package com.brian.codeblog.parser;

import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.util.LogUtil;

public class BlogHtmlParserFactory {

    /**
     * 根据type获取网页解析器，根据左边第一位进行判断
     * @param type 四位数的16进制整数，
     * 左起第一位：网站类型，csdn、cnblog等
     * 左起第二位：tab分类，首页、推荐、热门等
     * 左起第三位：技术模块，移动开发、web前端等
     * 左起第四位：语言，Android、ios等
     * @return
     */
    public static IBlogHtmlParser getBlogParser(int type) {
        LogUtil.log("type=" + type);
        IBlogHtmlParser parser = null;
        int webType = TypeManager.getWebType(type);
        switch (webType) {
            case TypeManager.TYPE_WEB_CSDN:
                parser = CSDNHtmlParser.getInstance();
                break;
            case TypeManager.TYPE_WEB_INFOQ:
                parser = InfoQHtmlParser.getInstance();
                break;
            case TypeManager.TYPE_WEB_OSCHINA:
                parser = OsChinaHtmlParser.getInstance();
                break;
            case TypeManager.TYPE_WEB_ITEYE:
                parser = ITEyeHtmlParser.getInstance();
                break;
            case TypeManager.TYPE_WEB_JCC:
                parser = JccHtmlParser.getInstance();
                break;
            case TypeManager.TYPE_WEB_OSNEWS:
                parser = OsChinaNewsParser.getInstance();
                break;
            default:
                parser = CSDNHtmlParser.getInstance();
                break;
        }
        return parser;
    }
}
