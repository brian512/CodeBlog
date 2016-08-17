package com.brian.csdnblog.parser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.manager.Constants;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.JsoupUtil;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.PreferenceUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 博客园网页解析类
 * @author huamm
 * {@link http://www.jcodecraeer.com/plus/list.php?tid=16&PageNo=2}
 */
public class JccHtmlParser implements IBlogHtmlParser {
    private static final String TAG = InfoQHtmlParser.class.getSimpleName();

    private static String[] TYPES_STR = {
        "tid=16", // 移动开发
        "tid=5", // 前端开发
        "tid=14", // 数据库
        "tid=4", // 云计算
        "tid=15", // 系统运维
        "tid=6" // 企业开发
    };
    
    private static final String URL_BLOG_BASE = "http://www.jcodecraeer.com/";
    
    private static final String URL_BLOG_LIST = "http://www.jcodecraeer.com/plus/list.php?tid=16&PageNo=1";
    
    private static JccHtmlParser sInstance = null;
    
    private JccHtmlParser() {}
    
    public static JccHtmlParser getInstance() {
        if (sInstance == null) {
            synchronized(TAG) {
                if (sInstance == null) {
                    sInstance = new JccHtmlParser();
                }
            }
        }
        return sInstance;
    }
    
    
    @Override
    public List<BlogInfo> getBlogList(int type, String strHtml) {
        try {
            return doGetBlogList(type, strHtml);
        } catch (Exception e) {
            MobclickAgent.reportError(Env.getContext(), e);
            return null;
        }
    }
    
    private List<BlogInfo> doGetBlogList(int type, String str) {
        List<BlogInfo> list = new ArrayList<BlogInfo>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
//        LogUtil.d("str=" + str);
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        // 获取class="article_item"的所有元素
        Element blogs = doc.getElementsByClass("archive-list").get(0);
        if (blogs == null) {
            return list;
        }
        Elements blogList = blogs.getElementsByClass("archive-item");

        for (Element blogItem : blogList) {

            BlogInfo item = new BlogInfo();

            String title = blogItem.select("h3").select("a").text(); // 得到标题
            
            String link = blogItem.select("h3").select("a").attr("href");

            String description = blogItem.getElementsByTag("p").text();

            String msg = blogItem.getElementsByClass("list-user").get(0).text() 
                    + "  " + blogItem.getElementsByClass("glyphicon-class").text();

            item.type = type;
            item.title = title;
            item.link = link;
            item.articleType = Constants.DEF_ARTICLE_TYPE.INT_ORIGINAL;
            item.msg = msg;
            item.description = description;

            list.add(item);
        }
        return list;
    }

    public String getBlogContent(int type, String contentSrc) {
        try {
            return doGetBlogContent(contentSrc);
        } catch (Exception e) {
            MobclickAgent.reportError(Env.getContext(), e);
            return "";
        }
    }
    
    /**
     * 从网页数据中截取博客正文部分
     * 
     * @param contentSrc
     * @return
     */
    private String doGetBlogContent(String contentSrc) {
        
        // 获取文档内容
        Document doc = Jsoup.parse(contentSrc);
        LogUtil.d("doc1=" + doc);
        Element title = doc.getElementsByTag("h1").get(0);
        title.tagName("h1");
        
        Element detail = doc.getElementsByClass("arc_body").get(0);
//        detail.getElementsByTag("h1").tagName("h2");

        detail.getElementsByIndexEquals(0).remove();
        
        detail.getElementsByClass("jiathis_style").remove();
        detail.getElementsByClass("runtimead").remove();
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
        
        return JsoupUtil.sHtmlFormat.replace(JsoupUtil.CONTENT_HOLDER, "<h1>"+title.text()+"</h1>"+detail.html());
    }

    @Override
    /**
     * 若该链接是博文链接，则返回链接地址，若不是则返回空
     * @param urls
     * @return
     */
     public String getBlogContentUrl(String... urls) {
         String blogUrl = "";
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
        int category = PreferenceUtil.getInt(Env.getContext(), PreferenceUtil.pre_key_article_type, 0);
        if (category >= TYPES_STR.length) {
            category = 0;
        }
        return URL_BLOG_LIST.replace("tid=16", TYPES_STR[category]).replace("PageNo=1", "PageNo="+page);
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_BLOG_BASE;
    }

}
