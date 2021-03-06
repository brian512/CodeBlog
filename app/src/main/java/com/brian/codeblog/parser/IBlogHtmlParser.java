package com.brian.codeblog.parser;

import com.brian.codeblog.model.BlogInfo;

import java.util.List;

public interface IBlogHtmlParser {
    
    /**
     * 从网页源码中解析博客列表
     */
    List<BlogInfo> getBlogList(int type, String strHtml);
    
    /**
     * 从网页源码中解析出博客正文
     */
    String getBlogContent(int type, String strHtml);

    String getBlogTitle(int type, String strHtml);

    String getBlogContentUrl(String... strs);
    
    /**
     * 根据类型来获取请求链接，
     * @param type 网站类型，以及技术类型
     * @param page 页码
     */
    String getUrlByType(int type, int page);

    String getBlogerUrl(String homeUrl, int page);

    String getBlogBaseUrl();
}
