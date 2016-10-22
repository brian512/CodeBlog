package com.brian.codeblog.parser;

import android.text.TextUtils;

import com.brian.codeblog.Env;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.manager.BlogerManager;
import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.Bloger;
import com.brian.common.utils.JsoupUtil;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.Md5;
import com.umeng.analytics.MobclickAgent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 博客园网页解析类
 * @author huamm
 * http://www.iteye.com/blogs
 */
public class ITEyeHtmlParser implements IBlogHtmlParser {
    private static final String TAG = InfoQHtmlParser.class.getSimpleName();

    private static String[] TYPES_STR = {
        "mobile", // 移动开发
        "web", // 前端开发
        "database", // 数据库
        "internet", // 云计算
        "os", // 系统运维
        "architecture" // 企业开发
    };
    
    private static final String URL_BLOG_BASE = "http://www.iteye.com/blogs/";
    
    private static final String URL_BLOG_LIST = "http://www.iteye.com/blogs/category/mobile?page=1";

    private static final String URL_BLOGER_HOME_PAGE_SUFF = "?page=2";

    private static ITEyeHtmlParser sInstance = null;
    
    private ITEyeHtmlParser() {}
    
    public static ITEyeHtmlParser getInstance() {
        if (sInstance == null) {
            synchronized(TAG) {
                if (sInstance == null) {
                    sInstance = new ITEyeHtmlParser();
                }
            }
        }
        return sInstance;
    }
    
    
    @Override
    public List<BlogInfo> getBlogList(int type, String strHtml) {
        try {
            if (TypeManager.getCateType(type) == TypeManager.TYPE_CAT_BLOGER) {
                return doGetBlogerItemList(type, strHtml);
            } else {
                return doGetBlogList(type, strHtml);
            }
        } catch (Exception e) {
            LogUtil.printError(e);
            MobclickAgent.reportError(Env.getContext(), e);
            return null;
        }
    }


    private List<BlogInfo> doGetBlogerItemList(int type, String str) {
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
//        LogUtil.d("str=" + str);
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        // 获取class="article_item"的所有元素
        Elements blogList = doc.getElementsByClass("blog_main");
        if (blogList == null) {
            return list;
        }
        Bloger bloger = BlogerManager.getsInstance().getCurrBloger();
        String host = getBlogBaseUrl();
        if (!TextUtils.isEmpty(bloger.homePageUrl)) {
            host = bloger.homePageUrl.substring(0, bloger.homePageUrl.indexOf(".com")+4);
        }

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.select("h3").select("a").text(); // 得到标题
            item.link = blogItem.select("h3").select("a").attr("href");
            if (item.link.startsWith("/")) {
                item.link = host + item.link;
            }
            item.summary = blogItem.getElementsByClass("blog_content").first().text();
            item.blogId = Md5.getMD5ofStr(item.link);

            Element msgElement = blogItem.getElementsByClass("blog_bottom").get(0);
            msgElement.getElementsByClass("comment").remove();
            msgElement.getElementsByClass("view").remove();
            msgElement.getElementsByClass("digged").remove();
            item.extraMsg = msgElement.text();
            item.type = type;

            list.add(item);
        }
        return list;
    }
    
    private List<BlogInfo> doGetBlogList(int type, String str) {
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
//        LogUtil.d("str=" + str);
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        // 获取class="article_item"的所有元素
        Element blogs = doc.getElementById("index_main");
        if (blogs == null) {
            return list;
        }
        Elements blogList = blogs.getElementsByClass("content");

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.select("h3").select("a").text(); // 得到标题
            item.link = blogItem.select("h3").select("a").attr("href");
            if (item.link.startsWith("/")) {
                item.link = getBlogBaseUrl() + item.link;
            }
            item.summary = blogItem.getElementsByIndexEquals(1).text();
            item.blogId = Md5.getMD5ofStr(item.link);

            Element msgElement = blogItem.getElementsByClass("blog_info").get(0);
            msgElement.getElementsByClass("comment").remove();
            msgElement.getElementsByClass("view").remove();
            msgElement.getElementsByClass("digged").remove();
            item.extraMsg = msgElement.text();
            item.type = type;

            String homePageUrl = blogItem.getElementsByClass("blog_info").get(0).select("a").attr("href");
            if (!TextUtils.isEmpty(homePageUrl)) {
                Bloger bloger = new Bloger();
                bloger.blogerType = type;
                bloger.nickName = blogItem.getElementsByClass("blog_info").get(0).select("a").text();
                bloger.headUrl = blogItem.getElementsByClass("logo").get(0).select("img").attr("src");
                bloger.homePageUrl = homePageUrl;
                bloger.blogerID = Bloger.getBlogerId(bloger.homePageUrl);

//                BlogerTable.getInstance().insert(bloger);//保存用户信息
                item.blogerJson = bloger.toJson();
                item.blogerID = bloger.blogerID;
            }

            list.add(item);
        }
        return list;
    }

    public String getBlogContent(int type, String contentSrc) {
        try {
            return doGetBlogContent(contentSrc);
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(Env.getContext(), e);
            return "";
        }
    }

    @Override
    public String getBlogTitle(int type, String strHtml) {
        try {
            Document doc = Jsoup.parse(strHtml);
            return doc.getElementsByTag("h2").text();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * 从网页数据中截取博客正文部分
     */
    private String doGetBlogContent(String contentSrc) {
        
        // 获取文档内容
        Document doc = Jsoup.parse(contentSrc);
        LogUtil.d("doc1=" + doc);
        Element detail = doc.getElementsByClass("blog_main").get(0);
        detail.getElementById("bottoms").remove();
        detail.getElementsByClass("blog_nav").remove();
        detail.getElementsByClass("news_tag").remove();
        detail.getElementsByClass("blog_categories").remove();
        detail.getElementsByClass("blog_bottom").remove();
        detail.getElementsByClass("boutique-curr-box").remove();
        detail.getElementsByClass("blog_comment").remove();
        detail.getElementsByTag("iframe").remove();
        LogUtil.d("detai=" + detail);
        
        // 处理代码块-markdown
        Elements elements = detail.getElementsByClass("dp-highlighter");
        for (Element codeNode : elements) {
            codeNode.tagName("pre");
            codeNode.attr("name", "code");
            codeNode.html(codeNode.text());//原始的源代码标签中，html直接就是源代码text
        }
        // 处理代码块
        Elements codeElements = detail.select("pre[name=code]");
        for (Element codeNode : codeElements) {
            codeNode.attr("class", "brush: java; gutter: false;");
        }
        
        // 缩放图片
        Elements elementImgs = detail.getElementsByTag("img");
        for (Element img : elementImgs) {
            img.attr("width", "auto");
            img.attr("style", "max-width:100%;");
        }
        
        return JsoupUtil.sHtmlFormat.replace(JsoupUtil.CONTENT_HOLDER, detail.html());
    }

    @Override
    /**
     * 若该链接是博文链接，则返回链接地址，若不是则返回空
     * @param urls
     * @return
     */
     public String getBlogContentUrl(String... urls) {
         String blogUrl;
         String url = urls[0];
         if (url.startsWith("/")) {
             blogUrl = URL_BLOG_BASE + url;
         } else {
             blogUrl = url;
         }
         return blogUrl;
     }

    @Override
    public String getUrlByType(int type, int page) {
        int category = CommonPreference.getInstance().getArticleType();
        if (category >= TYPES_STR.length) {
            category = 0;
        }
        return URL_BLOG_LIST.replace("mobile", TYPES_STR[category]).replace("page=1", "page="+page);
    }

    @Override
    public String getBlogerUrl(String homeUrl, int page) {
        if (!homeUrl.endsWith("/")) {
            homeUrl += "/";
        }
        homeUrl += URL_BLOGER_HOME_PAGE_SUFF;
        return homeUrl.replace("page=1", "page="+page);
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_BLOG_BASE;
    }
}
