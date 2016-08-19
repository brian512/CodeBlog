
package com.brian.csdnblog.parser;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.Constants;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.model.SearchResult;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.JsoupUtil;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.PreferenceUtil;
import com.brian.csdnblog.util.ResourceUtil;
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
    private static final String URL_CSDN_BLOG_HOME = "http://blog.csdn.net/type/index.html?&page=1";


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
            FileUtil.writeFile("/sdcard/bloglist", str);
            return doGetHotBlogItemList(type, str);
        } catch (Exception e) {
            MobclickAgent.reportError(Env.getContext(), e);
            return null;
        }
    }


    /**
     * 使用Jsoup解析html文档
     *
     * @param str
     * @return
     */
    private List<BlogInfo> doGetHotBlogItemList(int type, String str) {
        List<BlogInfo> list = new ArrayList<BlogInfo>();
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

            String msg = blogItem.getElementsByClass("nickname").get(0).text() + "  " + blogItem.getElementsByClass("blog_list_b_r").get(0).select("label").text();
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

    /**
     * 获得博主个人资料
     *
     * @param str
     * @return
     */
    public Bloger getBlogerInfo(String str) {

        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            Bloger bloger = new Bloger();

            // 获取文档内容
            Document doc = Jsoup.parse(str);
            Elements profiles = doc.getElementsByClass("panel");

            Element profile = null;
            for (Element element : profiles) {
                if (element.select("ul.panel_head").text()
                        .equals(ResourceUtil.getString(Env.getContext(), R.string.str_profile))) {
                    profile = element;
                    break;
                }
            }
            // 未找到个人资料框
            if (profile == null) {
                return null;
            }

            Element profileBody = profile.select("ul.panel_body.profile").get(0);

            Element userface = profileBody.getElementById("blog_userface");
            String userfaceLink = userface.select("a").select("img").attr("src"); // 得到头像链接
            String temp = userface.select("a").attr("href");
            bloger.blogerID = temp.substring(temp.lastIndexOf("/") + 1);
            String username = userface.getElementsByTag("a").get(1).text(); // 用户名

            Element blog_rank = profileBody.getElementById("blog_rank");
            Element blog_statistics = profileBody.getElementById("blog_statistics");

            // 获取积分排名信息
            Elements rankLi = blog_rank.select("li");
            bloger.visit = rankLi.get(0).text();
            bloger.credits = rankLi.get(1).text();
            bloger.grade = getUserGrade(rankLi.get(2));
            bloger.rank = rankLi.get(3).text();

            // 获取博文信息
            Elements blogLi = blog_statistics.select("li");
            bloger.original = blogLi.get(0).text();
            bloger.repost = blogLi.get(1).text();
            bloger.translate = blogLi.get(2).text();
            bloger.comment = blogLi.get(3).text();

            bloger.faceURL = userfaceLink;
            bloger.blogerName = username;
            return bloger;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过等级图片链接 获取用户等级
     *
     * @param rank
     * @return
     */
    private String getUserGrade(Element rank) {
        String gradeLink = rank.select("span").select("img").attr("src");
        String grade = gradeLink.substring(gradeLink.indexOf("/blog")).substring(5, 6);
        return "等级：BLOG " + grade;
    }

    public List<SearchResult> getSearchResultList(String strResult) {
        try {
            return doGetSearchResultList(strResult);
        } catch (Exception e) {
            MobclickAgent.reportError(Env.getContext(), e);
        }
        return new ArrayList<SearchResult>();
    }

    private List<SearchResult> doGetSearchResultList(String strResult) {
        List<SearchResult> resultList = new ArrayList<SearchResult>();
        if (TextUtils.isEmpty(strResult)) {
            return resultList;
        }
        // 获取文档对象
        Document doc = Jsoup.parse(strResult);
        Elements results = doc.getElementsByClass("search-list");
        SearchResult temp = null;
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


    public List<BlogInfo> getCachedBlogInfos(int type) {
        String cachedStr = FileUtil.getFileContent(Env.getContext().getFilesDir() + "/cache_" + type);
        if (TextUtils.isEmpty(cachedStr)) {
            return new ArrayList<BlogInfo>();
        } else {
            return getBlogList(type, cachedStr);
        }
    }

    public String getUrlByType(int blogType, int page) {
        return getUrl(blogType, "" + page);
    }

    private String getUrl(int blogType, String page) {
        int category = PreferenceUtil.getInt(Env.getContext(), PreferenceUtil.pre_key_article_type, 0);
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
     *
     * @param urls
     * @return
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
