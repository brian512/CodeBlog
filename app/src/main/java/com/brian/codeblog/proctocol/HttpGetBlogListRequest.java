package com.brian.codeblog.proctocol;

import com.brian.codeblog.model.BaseType;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.parser.BlogHtmlParserFactory;
import com.brian.codeblog.parser.IBlogHtmlParser;
import com.brian.common.datacenter.network.AbstractHttpClient;
import com.brian.common.datacenter.network.BaseRequestParam;
import com.brian.common.datacenter.network.HttpClientParam;

import java.util.List;

/**
 * 请求博客列表
 * Created by huamm on 2016/10/17 0017.
 */

public class HttpGetBlogListRequest extends AbstractHttpClient<HttpGetBlogListRequest.RequestParam, HttpGetBlogListRequest.ResultData> {

    private int type = -1;

    public static class RequestParam extends BaseRequestParam {
        public int type;
    }


    public static class ResultData extends BaseType {
        public List<BlogInfo> blogInfoList;
    }

    @Override
    public HttpClientParam convRequestParam(RequestParam param) {
        HttpClientParam clientParam = new HttpClientParam();
        clientParam.method = HttpClientParam.METHOD_GET;
        clientParam.url = param.url;
        this.type = param.type;
        return clientParam;
    }

    @Override
    public ResultData convResponseResult(String responseContent) {
        if (type == -1) {
            throw new RuntimeException("set RequestParam.type!");
        }
        IBlogHtmlParser parser = BlogHtmlParserFactory.getBlogParser(type);
        ResultData resultData = new ResultData();
        resultData.blogInfoList = parser.getBlogList(type, responseContent);
        return resultData;
    }
}
