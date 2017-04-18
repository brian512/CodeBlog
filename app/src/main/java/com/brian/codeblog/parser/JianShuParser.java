package com.brian.codeblog.parser;

import android.text.TextUtils;

import com.brian.common.tools.Env;
import com.brian.codeblog.model.BlogInfo;
import com.brian.common.utils.JsoupUtil;
import com.brian.common.utils.Md5;
import com.umeng.analytics.MobclickAgent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huamm on 2016/9/12 0012.
 */
public class JianShuParser implements IBlogHtmlParser {

    private static final String URL_BLOG_BASE = "http://www.jianshu.com/";

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
        List<BlogInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(str)) {
            return list;
        }

        Document doc = Jsoup.parse(str);
        Element blogs = doc.getElementsByClass("article-list").get(0);
        if (blogs == null) {
            return list;
        }
        Elements blogList = blogs.getAllElements();
        for (Element blogItem : blogList) {
            BlogInfo item = new BlogInfo();
            item.title = blogItem.select("h4").select("a").text(); // 得到标题
            item.link = blogItem.select("h4").select("a").attr("href");
            item.summary = blogItem.getElementsByClass("list-footer").get(0).text();
            item.extraMsg = blogItem.getElementsByClass("list-user").get(0).text();
            item.blogId = Md5.getMD5ofStr(item.link);

            item.type = type;

            list.add(item);
        }

        return  list;
    }


    @Override
    public String getBlogContent(int type, String strHtml) {
        try {
            return doGetBlogContent(strHtml);
        } catch (Exception e) {
            e.printStackTrace();
            MobclickAgent.reportError(Env.getContext(), e);
            return "";
        }
    }

    private String doGetBlogContent(String contentSrc) {
        // 获取文档内容
        Document doc = Jsoup.parse(contentSrc);
        Element detail = doc.getElementsByClass("preview").get(0);
        detail.getElementsByIndexEquals(0).remove();
        detail.getElementsByClass("meta-top").remove();
        detail.getElementsByClass("article-share").remove();

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
    public String getBlogTitle(int type, String strHtml) {
        try {
            Document doc = Jsoup.parse(strHtml);
            return doc.getElementsByTag("h1").text();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String getBlogContentUrl(String... strs) {
        return null;
    }

    @Override
    public String getUrlByType(int type, int page) {
        return null;
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
