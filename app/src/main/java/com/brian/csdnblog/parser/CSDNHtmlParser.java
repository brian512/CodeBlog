
package com.brian.csdnblog.parser;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.model.SearchResult;
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
 * csdn博客网页解析类
 *
 * @author huamm
 */
public class CSDNHtmlParser implements IBlogHtmlParser {
    private static final String TAG = CSDNHtmlParser.class.getSimpleName();

    private static final int TYPE = TypeManager.initType(TypeManager.TYPE_WEB_CSDN);

    private static String[] TYPES_STR = {
            "mobile", "web", "database", "cloud", "system", "enterprise"
    };

    private static final String URL_CSDN_BLOG_BASE = "http://blog.csdn.net";
    private static final String URL_CSDN_BLOG_HOME = "http://blog.csdn.net/type/newarticle.html?&page=1";
    private static final String URL_BLOGER_HOME_PAGE_SUFF = "article/list/1";

    public static final String URL_SEARCH = "http://so.csdn.net/so/search/s.do?p=1&t=blog&q=key_word";//&t=blog


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
            item.title = blogItem.select("h3").text(); // 得到标题
            item.summary = blogItem.getElementsByClass("blog_list_c").get(0).text();
            String nickName = blogItem.getElementsByClass("nickname").get(0).text();
            item.extraMsg = nickName + "  " + blogItem.getElementsByClass("blog_list_b_r").get(0).select("label").text();
            item.link = blogItem.select("h3").select("a").attr("href");
            item.blogId = Md5.getMD5ofStr(item.link);
            item.type = type;
            item.dateStamp = String.valueOf(System.currentTimeMillis());

            String homePageUrl = blogItem.getElementsByClass("nickname").get(0).select("a").attr("href");
            if (!TextUtils.isEmpty(homePageUrl)) {
                Bloger bloger = new Bloger();
                bloger.blogerType = TYPE;
                bloger.nickName = nickName;
                bloger.headUrl = blogItem.getElementsByClass("head").get(0).select("img").attr("src");
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
            blogList = doc.getElementsByClass("skin_list").get(0).children();
            if (blogList == null || blogList.size() <= 0) {
                return list;
            }
            for (Element blogItem : blogList) {
                BlogInfo item = new BlogInfo();
                item.title = blogItem.getElementsByClass("list_c_t").get(0).text(); // 得到标题
                item.summary = blogItem.getElementsByClass("list_c_c").get(0).text();
                item.extraMsg = blogItem.getElementsByClass("list_c_b_l").get(0).text();
                item.link = blogItem.getElementsByClass("list_c_t").get(0).select("a").attr("href");
                if (item.link.startsWith("/")) {
                    item.link = URL_CSDN_BLOG_BASE + item.link;
                }
                item.blogId = Md5.getMD5ofStr(item.link);
                item.type = type;
                item.dateStamp = String.valueOf(System.currentTimeMillis());

                list.add(item);
            }
        } else {
            for (Element blogItem : blogList) {
                BlogInfo item = new BlogInfo();
                item.title = blogItem.getElementsByClass("article_title").get(0).text(); // 得到标题
                item.summary = blogItem.getElementsByClass("article_description").get(0).text();
                item.extraMsg = blogItem.getElementsByClass("article_manage").get(0).text();
                item.link = blogItem.getElementsByClass("article_title").get(0).select("a").attr("href");
                if (item.link.startsWith("/")) {
                    item.link = URL_CSDN_BLOG_BASE + item.link;
                }
                item.blogId = Md5.getMD5ofStr(item.link);
                item.type = type;
                item.dateStamp = String.valueOf(System.currentTimeMillis());

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
        Elements details = doc.getElementsByClass("details");
        if (details == null || details.isEmpty()) {
            details = doc.getElementsByClass("markdown_views");
        }
        Element detail =details.get(0);

        // 将标题1换为标题2
        detail.getElementsByTag("h1").tagName("h2");

        detail.getElementsByClass("tag2box").remove();
        detail.getElementsByClass("article_manage").remove();
        detail.getElementsByClass("category").remove();
        detail.getElementsByClass("bdsharebuttonbox").remove();
        detail.getElementsByClass("similar_article").remove();
        detail.getElementsByClass("digg").remove();

        // 处理代码块-markdown
        Elements elements = detail.select("pre");
        for (Element codeNode : elements) {
            //添加属性，使得markdown的代码与原始代码格式一致
            codeNode.tagName("pre");
            codeNode.attr("name", "code");
            codeNode.html(codeNode.text());//原始的源代码标签中，html直接就是源代码text
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

            String homePageUrl = element.getElementsByClass("author-time").get(0).select("a").attr("href");
            if (!TextUtils.isEmpty(homePageUrl)) {
                Bloger bloger = new Bloger();
                bloger.blogerType = TYPE;
                bloger.nickName = element.getElementsByClass("author-time").get(0).select("a").text();
//                bloger.headUrl = element.getElementsByClass("head").get(0).select("img").attr("src");
                bloger.homePageUrl = homePageUrl.replace("my.csdn.net", "blog.csdn.net");
                bloger.blogerID = Bloger.getBlogerId(bloger.homePageUrl);

//                BlogerTable.getInstance().insert(bloger);//保存用户信息
                temp.blogerJson = bloger.toJson();
                temp.blogerID = bloger.blogerID;
            }

            resultList.add(temp);
        }
        return resultList;
    }

    public String getUrlByType(int blogType, int page) {
        return getUrl(blogType, "" + page);
    }

    @Override
    public String getBlogerUrl(String homeUrl, int page) {
        if (!homeUrl.endsWith("/")) {
            homeUrl += "/";
        }
        homeUrl += URL_BLOGER_HOME_PAGE_SUFF;
        return homeUrl.replace("/1", "/"+page);
    }

    private String getUrl(int blogType, String page) {
        int category = TypeManager.getCateType(blogType);
        if (category >= TYPES_STR.length) {
            category = 0;
        }
        return URL_CSDN_BLOG_HOME.replace("type", TYPES_STR[category]).replace("1", page);
    }

    public String getSearchUrlByKeyword(String keyword, int page) {
        return getSearchUrlByKeyword(keyword, "" + page);
    }

    private String getSearchUrlByKeyword(String keyword, String page) {
        String url = URL_SEARCH;
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
        String blogUrl = urls[0];
        if (blogUrl.contains("/article/details")) {// 链接为CSDN博客内容
            if (blogUrl.startsWith("/")) {
                blogUrl = URL_CSDN_BLOG_BASE + blogUrl;
            }
        }
        return blogUrl;
    }

    @Override
    public String getBlogBaseUrl() {
        return URL_CSDN_BLOG_BASE;
    }
}
