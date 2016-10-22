package com.brian.codeblog.parser;

import android.text.TextUtils;

import com.brian.codeblog.Env;
import com.brian.codeblog.datacenter.preference.CommonPreference;
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
 */
public class OsChinaHtmlParser implements IBlogHtmlParser {
    private static final String TAG = InfoQHtmlParser.class.getSimpleName();

    private static String[] TYPES_STR = {
        "428602", // 移动开发
        "428612", // 前端开发
        "428610", // 数据库
        "428639", // 云计算
        "428613", // 系统运维
        "428611" // 企业开发
    };
    
    private static final String URL_BLOG_BASE = "http://www.oschina.net/";
    
    private static final String URL_BLOG_LIST = "http://www.oschina.net/blog?type=428602&p=1";

    private static final String URL_BLOGER_HOME_PAGE_SUFF = "blog?sort=time&p=1";

    private static OsChinaHtmlParser sInstance = null;
    
    private OsChinaHtmlParser() {}
    
    public static OsChinaHtmlParser getInstance() {
        if (sInstance == null) {
            synchronized(TAG) {
                if (sInstance == null) {
                    sInstance = new OsChinaHtmlParser();
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
            e.printStackTrace();
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
        Element blogs = doc.getElementById("list");
        if (blogs == null) {
            return list;
        }
        Elements blogList = blogs.getElementsByClass("list-item");

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.getElementsByClass("title").get(0).select("a").text(); // 得到标题
            item.link = blogItem.getElementsByClass("title").get(0).select("a").attr("href");
            item.summary = "";
            item.extraMsg = blogItem.getElementsByClass("time").get(0).text();
            item.blogId = Md5.getMD5ofStr(item.link);

            item.type = type;

            list.add(item);
        }
        return list;
    }

    private List<BlogInfo> doGetBlogList(int type, String str) {
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            LogUtil.e("str is empty");
            return list;
        }
//        LogUtil.d("str=" + str);
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        // 获取class="article_item"的所有元素
        Element blogs = doc.getElementById("topsOfToday");
        if (blogs == null) {
            LogUtil.e("RecentBlogs=null");
            return list;
        }
        Elements blogList = blogs.getElementsByClass("item");

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.select("header").get(0).select("a").text(); // 得到标题
            item.summary = blogItem.select("section").get(0).text();
            item.extraMsg = blogItem.select("footer").get(0).text();
            item.link = blogItem.select("header").get(0).select("a").attr("href");
            item.blogId = Md5.getMD5ofStr(item.link);

            item.type = type;

            String homePageUrl = "";
            try {
                homePageUrl = blogItem.getElementsByClass("box-fl").get(0).select("a").attr("href");
            } catch (Exception e) {
                LogUtil.printError(e);
            }
            if (!TextUtils.isEmpty(homePageUrl)) {
                Bloger bloger = new Bloger();
                bloger.blogerType = type;
                bloger.nickName = blogItem.select("footer").get(0).getElementsByIndexEquals(0).text();
                bloger.headUrl = blogItem.getElementsByClass("box-fl").get(0).select("img").attr("src");
                if (TextUtils.isEmpty(bloger.headUrl)) {
                    bloger.headUrl = blogItem.getElementsByClass("box-fl").get(0).select("img").attr("data-delay");
                }
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
            LogUtil.printError(e);
            MobclickAgent.reportError(Env.getContext(), e);
            return "";
        }
    }

    @Override
    public String getBlogTitle(int type, String strHtml) {
        try {
            Document doc = Jsoup.parse(strHtml);
            return doc.getElementsByTag("h1").text();
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
        Element detail = null;
        try {
            detail = doc.getElementsByClass("blog-content").get(0);
        } catch (Exception e) {
        }
        if (detail == null) {
            try {
                detail = doc.getElementsByClass("BlogContent").get(0);
            } catch (Exception e) {
            }
        }
        if (detail == null) {
            return contentSrc;
        }


        detail.getElementsByClass("title").get(0).tagName("h1");

        detail.getElementsByClass("back-list").remove();
        detail.getElementsByClass("user-info").remove();
        detail.getElementsByClass("data-info").remove();
        detail.getElementsByClass("operate").remove();
        detail.getElementsByClass("user-card").remove();
        detail.getElementsByClass("related-info").remove();
        detail.getElementsByClass("inline_reply_editor").remove();
        detail.getElementsByClass("SpaceList").remove();

        // 处理代码块-markdown
        Elements elements = detail.select("pre");
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
//         return blogUrl + "?fromerr=3WWGWBvP";
     }

    @Override
    public String getUrlByType(int type, int page) {
        int category = CommonPreference.getInstance().getArticleType();
        if (category >= TYPES_STR.length) {
            category = 0;
        }
        return URL_BLOG_LIST.replace("428602", TYPES_STR[category]).replace("p=1", "p="+page);
    }

    @Override
    public String getBlogerUrl(String homeUrl, int page) {
        if (!homeUrl.endsWith("/")) {
            homeUrl += "/";
        }
        homeUrl += URL_BLOGER_HOME_PAGE_SUFF;
        return homeUrl.replace("p=1", "p="+page);
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_BLOG_BASE;
    }

}
