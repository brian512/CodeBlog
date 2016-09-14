package com.brian.csdnblog.parser;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.datacenter.preference.CommonPreference;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.JsoupUtil;
import com.brian.csdnblog.util.Md5;
import com.umeng.analytics.MobclickAgent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * infoq网页解析类
 * @author huamm
 */
public class InfoQHtmlParser implements IBlogHtmlParser {
    private static final String TAG = InfoQHtmlParser.class.getSimpleName();

    private static String[] TYPES_STR = {
        "mobile", // 移动开发
        "architect", // 前端开发
        "bigdata", // 数据库
        "cloud-computing", // 云计算
        "operation", // 系统运维
        "architecture-design" // 企业开发
    };
    
    private static final String URL_BLOG_BASE = "http://www.infoq.com/";
    
    private static final String URL_BLOG_LIST = "http://www.infoq.com/cn/mobile/articles/0";
    
    private static InfoQHtmlParser sInstance = null;
    
    private InfoQHtmlParser() {}
    
    public static InfoQHtmlParser getInstance() {
        if (sInstance == null) {
            synchronized(TAG) {
                if (sInstance == null) {
                    sInstance = new InfoQHtmlParser();
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
            e.printStackTrace();
            MobclickAgent.reportError(Env.getContext(), e);
            return null;
        }
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
        Elements blogList = doc.getElementsByClass("news_type2");
        if (blogList == null || blogList.size() <= 0) {
            return list;
        }

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.select("h2").text(); // 得到标题
            item.summary = blogItem.getElementsByTag("p").text();
            item.extraMsg = blogItem.getElementsByClass("author").text();
            item.link = URL_BLOG_BASE + blogItem.select("h2").select("a").attr("href");
            item.blogId = Md5.getMD5ofStr(item.link);

            item.type = type;

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
        
        // 获取class="details"的元素
        Element detail = doc.getElementById("content");
        detail.getElementsByTag("h1").tagName("h2");
        Element titleElement = detail.getElementsByClass("title_canvas").get(0);
        titleElement.getElementsByIndexEquals(1).remove();
        
        detail.getElementsByClass("author_general").remove();
        detail.getElementsByClass("sh_t").remove();
        detail.getElementsByClass("comment_here").remove();
        detail.getElementsByClass("comments").remove();
        detail.getElementsByClass("all_comments").remove();
        detail.getElementsByClass("random_links").remove();
        detail.getElementsByClass("article_page_right").remove();
        detail.getElementsByClass("related_sponsors").remove();
        detail.getElementsByClass("help_links").remove();
        detail.getElementsByClass("newsletter").remove();
        detail.getElementById("contentRatingWidget").remove();
        detail.getElementById("overlay_comments").remove();
        detail.getElementById("replyPopup").remove();
        detail.getElementById("editCommentPopup").remove();
        detail.getElementById("messagePopup").remove();
        detail.getElementById("noOfComments").remove();
        detail.getElementById("responseContent").remove();
        
        // 处理代码块-markdown
        Elements elements = detail.select("pre");
        for (Element codeNode : elements) {
//            Elements childs = codeNode.getAllElements();
//            for (Element child : childs) {
//                if ("code".equals(child.tagName())) {//选出code标签
//                }
//            }
            //添加属性，使得markdown的代码与原始代码格式一致
            codeNode.tagName("pre");
            codeNode.attr("name", "code");
            codeNode.html(codeNode.text());//原始的源代码标签中，html直接就是源代码text
        }
        // 处理代码块-markdown
        elements = detail.select("code");
        for (Element codeNode : elements) {
            //添加属性，使得markdown的代码与原始代码格式一致
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
        return URL_BLOG_LIST.replace("mobile", TYPES_STR[category]).replace("/0", "/"+(page-1)*12);
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_BLOG_BASE;
    }

}
