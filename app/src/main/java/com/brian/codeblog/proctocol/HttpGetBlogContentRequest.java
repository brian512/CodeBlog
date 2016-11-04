package com.brian.codeblog.proctocol;

import android.text.TextUtils;

import com.brian.codeblog.Config;
import com.brian.codeblog.model.BaseType;
import com.brian.codeblog.parser.BlogHtmlParserFactory;
import com.brian.codeblog.parser.IBlogHtmlParser;
import com.brian.codeblog.proctocol.base.AbstractHttpClient;
import com.brian.codeblog.proctocol.base.BaseRequestParam;
import com.brian.codeblog.proctocol.base.HttpClientParam;
import com.brian.common.utils.FileUtil;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.PathUtil;
import com.brian.common.utils.ToastUtil;

/**
 * 请求博客正文
 * Created by huamm on 2016/10/17 0017.
 */
public class HttpGetBlogContentRequest extends AbstractHttpClient<HttpGetBlogContentRequest.RequestParam, HttpGetBlogContentRequest.ResultData> {

    private int type = -1;

    public static class RequestParam extends BaseRequestParam {
        public int type;
        public String charset;
    }

    public static class ResultData extends BaseType {
        public String blogContent;
    }

    @Override
    public HttpClientParam convRequestParam(RequestParam param) {
        HttpClientParam clientParam = new HttpClientParam();
        clientParam.method = HttpClientParam.METHOD_GET;
        clientParam.url = param.url;
        this.type = param.type;
        clientParam.charset = param.charset;
        return clientParam;
    }

    @Override
    public ResultData convResponseResult(String responseContent) {
        if (type == -1) {
            throw new RuntimeException("set RequestParam.type!");
        }
        IBlogHtmlParser parser = BlogHtmlParserFactory.getBlogParser(type);
        ResultData resultData = new ResultData();
        resultData.blogContent = parser.getBlogContent(type, responseContent);
        if (TextUtils.isEmpty(resultData.blogContent)) {
            resultData.blogContent = responseContent;
        }
        if (Config.DEBUG_ENABLE) {
            String filePath = PathUtil.getCacheDirPath() + "/blog_" + System.currentTimeMillis() + ".html";
            FileUtil.writeFile(filePath, resultData.blogContent);
            LogUtil.log("filePath=" + filePath);
            ToastUtil.showMsg("filePath=" + filePath);
        }
        return resultData;
    }
}
