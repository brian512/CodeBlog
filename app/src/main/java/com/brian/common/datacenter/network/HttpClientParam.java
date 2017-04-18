package com.brian.common.datacenter.network;

import java.util.HashMap;


/**
 * JDHttpClient的查询参数
 * <p>
 * PS：暂时简单实现，只支持entry为String，后续可扩展参数
 * Header[] headers;
 * String contentType;
 * HttpEntity entity;
 * String cookie;
 */
public class HttpClientParam {

    /**
     * HTTP查询方式，取值GET、POST
     */
    public static final int METHOD_NONE = 0;
    public static final int METHOD_GET = 1;
    public static final int METHOD_POST = 2;

    /**
     * 查询方式
     */
    public int method = METHOD_NONE;

    /**
     * 请求URL
     */
    public String url = null;

    public Object tag = null;

    /**
     * POST方式的内容
     */
    public String content = null;
    public String key = null;

    /**
     * POST方式的内容是否压缩
     */
    public boolean contentGzip = false; // 默认压缩 // PS : 强制设置为false，服务器目前解压有问题，客户端上报压缩会解压不了

    public String charset = null;

    /**
     * 用户自定义的请求头参数
     */
    public HashMap<String, String> customHeaders = null;

}
