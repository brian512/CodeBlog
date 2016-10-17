package com.brian.codeblog.proctocol;

import com.brian.codeblog.model.BaseType;
import com.brian.codeblog.proctocol.base.AbstractHttpClient;
import com.brian.codeblog.proctocol.base.BaseRequestParam;
import com.brian.codeblog.proctocol.base.HttpClientParam;

/**
 * 机器人聊天请求
 * Created by huamm on 2016/10/17 0017.
 */

public class HttpGetChatRequest extends AbstractHttpClient<HttpGetChatRequest.RequestParam, HttpGetChatRequest.ResultData> {

    public static class RequestParam extends BaseRequestParam {
    }

    public static class ResultData extends BaseType {
        public String responseMsg;
    }

    @Override
    public HttpClientParam convRequestParam(RequestParam param) {
        HttpClientParam clientParam = new HttpClientParam();
        clientParam.method = HttpClientParam.METHOD_GET;
        clientParam.url = param.url;
        return clientParam;
    }

    @Override
    public ResultData convResponseResult(String responseContent) {
        ResultData resultData = new ResultData();
        resultData.responseMsg = responseContent;
        return resultData;
    }
}
