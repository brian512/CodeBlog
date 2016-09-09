
package com.brian.csdnblog.parser;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.manager.Constants;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.SearchResult;
import com.brian.csdnblog.util.JsoupUtil;
import com.brian.csdnblog.util.LogUtil;
import com.umeng.analytics.MobclickAgent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * csdn博客网页解析类
 *
 * @author huamm
 */
public class CSDNHtmlParser implements IBlogHtmlParser {

    private static final String TAG = CSDNHtmlParser.class.getSimpleName();

    private static String[] TYPES_STR = {
            "mobile", "web", "database", "cloud", "system", "enterprise"
    };

    private static final String URL_CSDN_BLOG_BASE = "http://blog.csdn.net";
    private static final String URL_CSDN_BLOG_HOME = "http://blog.csdn.net/type/newarticle.html?&page=1";
    private static final String URL_CSDN_BLOGER_HOME = "http://blog.csdn.net/brian512/article/list/1";


    // 文章内容页
    private static final String REGEX_DETAILS = "http://blog.csdn.net/(\\w+)/article/details/(\\d+)";
    // 个人博客首页
    private static final String REGEX_BLOG = "http://blog.csdn.net/(\\w+)[/]{0,1}";
    // 类别博客的列表
    private static final String REGEX_CATEGORY = "http://blog.csdn.net/(\\w+)/article/category/(\\d+)";
    // 博客列表
    private static final String REGEX_BLOGLIST = "http://blog.csdn.net/(\\w+)/article/list/(\\d+)";

    private static final String REGEX_LIST = "http://blog.csdn.net/(\\w+)/article/\\w+/(\\d+)";


    private static CSDNHtmlParser sInstance = null;

    private CSDNHtmlParser() {
    }

    public static CSDNHtmlParser getInstance() {
        if (sInstance == null) {
            synchronized (TAG) {
                if (sInstance == null) {
                    sInstance = new CSDNHtmlParser();
                }
            }
        }
        return sInstance;
    }


    public List<BlogInfo> getBlogList(int type, String str) {
        try {
//            FileUtil.writeFile("/sdcard/bloglist", str);
            if (TypeManager.getCateType(type) == TypeManager.TYPE_CAT_BLOGER) {
                return doGetBlogerItemList(type, str);
            } else {
                return doGetHotBlogItemList(type, str);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(Env.getContext(), e);
            return null;
        }
    }


    /**
     * 使用Jsoup解析html文档
     */
    private List<BlogInfo> doGetHotBlogItemList(int type, String str) {
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        // 获取class="article_item"的所有元素
        Elements blogList = doc.getElementsByClass("blog_list");
        if (blogList == null || blogList.size() <= 0) {
            return list;
        }

        for (Element blogItem : blogList) {

            BlogInfo item = new BlogInfo();

            String title = blogItem.select("h3").text(); // 得到标题
            LogUtil.i(TAG, "title=" + title);

            String description = blogItem.getElementsByClass("blog_list_c").get(0).text();
            LogUtil.i(TAG, "description=" + description);

            String blogerID = blogItem.getElementsByClass("nickname").get(0).text();
            String msg = blogerID + "  " + blogItem.getElementsByClass("blog_list_b_r").get(0).select("label").text();
            LogUtil.i(TAG, "msg=" + msg);

            String link = blogItem.select("h3").select("a").attr("href");
            LogUtil.i(TAG, "link=" + link);

            item.type = type;
            item.title = title;
            item.articleType = Constants.DEF_ARTICLE_TYPE.INT_ORIGINAL;
            item.msg = msg;
            item.description = description;
            item.link = link;
            item.dateStamp = String.valueOf(System.currentTimeMillis());
            item.blogerID = blogerID;

            list.add(item);
        }
        return list;
    }

    /**
     * 使用Jsoup解析html文档
     */
    private List<BlogInfo> doGetBlogerItemList(int type, String str) {
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }
        // 获取文档对象
        Document doc = Jsoup.parse(str);
        // 获取class="article_item"的所有元素
        Elements blogList = doc.getElementsByClass("article_item");//.get(0).children()
        if (blogList == null || blogList.size() <= 0) {
            return list;
        }

        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();

            String title = blogItem.getElementsByClass("article_title").get(0).text(); // 得到标题
            LogUtil.i(TAG, "title=" + title);

            String description = blogItem.getElementsByClass("article_description").get(0).text();
            LogUtil.i(TAG, "description=" + description);

            String msg = blogItem.getElementsByClass("article_manage").get(0).text();
            LogUtil.i(TAG, "msg=" + msg);

            String link = blogItem.getElementsByClass("article_title").get(0).select("a").attr("href");
            LogUtil.i(TAG, "link=" + link);

            item.type = type;
            item.title = title;
            item.articleType = Constants.DEF_ARTICLE_TYPE.INT_ORIGINAL;
            item.msg = msg;
            item.description = description;
            item.link = link;
            item.dateStamp = String.valueOf(System.currentTimeMillis());

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
            MobclickAgent.reportError(Env.getContext(), e);
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
        Element detail = doc.getElementsByClass("details").get(0);
        // 将标题1换为标题2
        detail.getElementsByTag("h1").tagName("h2");

        detail.getElementsByClass("tag2box").remove();
        detail.getElementsByClass("article_manage").remove();
        detail.getElementsByClass("category").remove();
        detail.getElementsByClass("bdsharebuttonbox").remove();
        detail.getElementsByClass("similar_article").remove();
        detail.getElementsByClass("digg").remove();
        detail.getElementById("digg").remove();

        // 处理代码块-markdown
        Elements elements = detail.select("pre[class=prettyprint]");
        for (Element codeNode : elements) {
            Elements childs = codeNode.getAllElements();
            for (Element child : childs) {
                if ("code".equals(child.tagName())) {//选出code标签
                    //添加属性，使得markdown的代码与原始代码格式一致
                    child.tagName("pre");
                    child.attr("name", "code");
                    child.html(child.text());//原始的源代码标签中，html直接就是源代码text
                }
            }
//            String code = codeNode.toString().replace("\\<.*?>","");
//            codeNode.html(code);
//            LogUtil.e("codeNode=" + codeNode);
        }
        // 处理代码块-markdown
        elements = detail.getElementsByClass("codeText");
        for (Element codeNode : elements) {
            codeNode.tagName("pre");
            codeNode.attr("name", "code");
            codeNode.html(codeNode.html());//原始的源代码标签中，html直接就是源代码text
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

    public List<SearchResult> getSearchResultList(String strResult) {
        try {
            return doGetSearchResultList(strResult);
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(Env.getContext(), e);
        }
        return new ArrayList<>();
    }

    private List<SearchResult> doGetSearchResultList(String strResult) {
        List<SearchResult> resultList = new ArrayList<>();
        if (TextUtils.isEmpty(strResult)) {
            return resultList;
        }
        // 获取文档对象
        Document doc = Jsoup.parse(strResult);
        Elements results = doc.getElementsByClass("search-list");
        SearchResult temp;
        for (Element element : results) {
            temp = new SearchResult();
            temp.title = element.getElementsByTag("dt").first().text();
            String authorTime = element.getElementsByClass("author-time").first().text();
            temp.authorTime = authorTime.substring(0, authorTime.indexOf("日期"));
            temp.searchDetail = element.getElementsByClass("search-detail").first().text();
            Element link = element.getElementsByClass("search-link").first();
            if (link == null) {
                temp.link = element.getElementsByTag("dt").first().select("a").attr("href");
            } else {
                temp.link = link.select("a").attr("href");
            }
            resultList.add(temp);
        }
        return resultList;
    }

    public String getUrlByType(int blogType, int page) {
        return getUrl(blogType, "" + page);
    }

    private String getUrl(int blogType, String page) {
        int category = TypeManager.getCateType(blogType);
        if (category == TypeManager.TYPE_CAT_BLOGER) {
            return URL_CSDN_BLOGER_HOME.replace("/1", "/"+page);
        }
        if (category >= TYPES_STR.length) {
            category = 0;
        }
        return URL_CSDN_BLOG_HOME.replace("type", TYPES_STR[category]).replace("1", page);
    }

    public String getSearchUrlByKeyword(String keyword, int page) {
        return getSearchUrlByKeyword(keyword, "" + page);
    }

    private String getSearchUrlByKeyword(String keyword, String page) {
        String url = Constants.URL_SEARCH;
        if (TextUtils.isEmpty(keyword)) {
            url = url.replace("key_word", "android");
        } else {
            url = url.replace("key_word", keyword).replace("1", "" + page);
        }
        return url;
    }

    /**
     * 若该链接是博文链接，则返回链接地址，若不是则返回空
     */
    public String getBlogContentUrl(String... urls) {
        String blogUrl = "";
        String url = urls[0];
        if (url.contains("/article/details")) {// 链接为CSDN博客内容
            if (url.startsWith("/")) {
                blogUrl = URL_CSDN_BLOG_BASE + url;
            } else {
                blogUrl = url;
            }
        }
        return blogUrl;
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_CSDN_BLOG_BASE;
    }
}
