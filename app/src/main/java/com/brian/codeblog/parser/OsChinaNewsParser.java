package com.brian.codeblog.parser;

import android.text.TextUtils;

import com.brian.codeblog.Env;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.util.JsoupUtil;
import com.brian.codeblog.util.LogUtil;
import com.brian.codeblog.util.Md5;
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
 * http://www.oschina.net/news/list?show=industry&p=1
 */
public class OsChinaNewsParser implements IBlogHtmlParser {
    private static final String TAG = InfoQHtmlParser.class.getSimpleName();
    
    private static final String URL_BLOG_BASE = "http://www.oschina.net/";
    
    private static final String URL_BLOG_LIST = "http://www.oschina.net/news/list?show=industry&p=1";
    
    private static OsChinaNewsParser sInstance = null;
    
    private OsChinaNewsParser() {}
    
    public static OsChinaNewsParser getInstance() {
        if (sInstance == null) {
            synchronized(TAG) {
                if (sInstance == null) {
                    sInstance = new OsChinaNewsParser();
                }
            }
        }
        return sInstance;
    }
    
    
    @Override
    public List<BlogInfo> getBlogList(int type, String strHtml) {
        try {
            return doGetNewsList(type, strHtml);
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(Env.getContext(), e);
            return null;
        }
    }
    
    private List<BlogInfo> doGetNewsList(int type, String str) {
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
//        LogUtil.d("str=" + str);
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        Element blogs = doc.getElementById("RecentNewsList");
        if (blogs == null) {
            return list;
        }
        Elements blogList = blogs.getElementsByClass("List").get(0).getElementsByTag("li");

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.select("h2").select("a").text(); // 得到标题
            item.summary = blogItem.getElementsByClass("detail").text();
            item.extraMsg = blogItem.getElementsByClass("date").text();
            item.link = blogItem.select("h2").select("a").attr("href");
            item.blogId = Md5.getMD5ofStr(item.link);

            item.type = type;

            if (item.link.startsWith("/") || item.link.contains("my.oschina.net")) {
                list.add(item);
            }
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
        Element detail = getNewsEntity(doc, "NewsEntity", false);
        if (detail == null) {
            detail = getNewsEntity(doc, "blog-content", false);
        }
        if (detail == null) {
            detail = getNewsEntity(doc, "BlogEntity", false);
        }
        if (detail == null) {
            detail = getNewsEntity(doc, "OSC_Content", true);
        }
        detail.getElementsByTag("h1").tagName("h2");
        
        detail.getElementsByIndexEquals(1).remove();
        
        detail.getElementsByClass("BlogStat").remove();
        detail.getElementsByClass("status-tags").remove();
        detail.getElementsByClass("BlogTags").remove();
        detail.getElementsByClass("BlogAnchor").remove();
        detail.getElementsByClass("BlogShare").remove();
        detail.getElementsByClass("NewsToolbar").remove();
        detail.getElementsByClass("translater").remove();
        detail.getElementsByClass("vote").remove();
        detail.getElementsByClass("Bottom").remove();
        detail.getElementsByClass("NewsLinks").remove();
        detail.getElementsByClass("copyright").remove();
        detail.getElementsByClass("NewsPrevAndNext").remove();
        detail.getElementsByClass("RelatedNews").remove();
        detail.getElementsByClass("tvote").remove();
        LogUtil.d("detai=" + detail);
        
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
    
    private Element getNewsEntity(Document doc, String name, boolean isID) {
        Element detail = null;
        try {
            if (isID) {
                detail = doc.getElementById(name);
            } else {
                detail = doc.getElementsByClass(name).get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
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
         return blogUrl + "?fromerr=3WWGWBvP";
     }

    @Override
    public String getUrlByType(int type, int page) {
        return URL_BLOG_LIST.replace("p=1", "p="+page);
    }

    @Override
    public String getBlogerUrl(String homeUrl, int page) {
        return null;
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_BLOG_BASE;
    }

}
