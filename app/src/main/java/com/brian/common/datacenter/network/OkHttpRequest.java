package com.brian.common.datacenter.network;

import com.brian.common.utils.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;

/**
 * Created by huamm on 2016/9/21 0021.
 */

public class OkHttpRequest {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private OkHttpClient mHttpClient;

    private static OkHttpRequest sInstance;
    private OkHttpRequest() {
        initHttpClient();
    }

    public static OkHttpRequest getInstance() {
        if (sInstance == null) {
            synchronized (OkHttpRequest.class) {
                if (sInstance == null) {
                    sInstance = new OkHttpRequest();
                }
            }
        }
        return sInstance;
    }

    private void initHttpClient() {
        // 任务线程池默认配置：
        ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
        Dispatcher dispatcher = new Dispatcher(executorService);
        // 请求队列最多64，单个域名最大并发量为5
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(5);

        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(/** The maximum number of idle connections for each address. */ 5, 5 , TimeUnit.MINUTES)) // 连接池配置
                .retryOnConnectionFailure(true) // 允许失败重连
                .followRedirects(true) // 允许重定向，最多重定向20次
                .build();
    }

    public void request(HttpClientParam param, Callback callback) {
        Request request = null;
        switch (param.method) {
            case HttpClientParam.METHOD_GET: {
                LogUtil.log("get_url=" + param.url);
                request = new Request.Builder()
                        .get() // GET请求
                        .url(param.url)
                        .tag(param.tag)
                        .build();
                break;
            }
            case HttpClientParam.METHOD_POST: {
                RequestBody requestBodyPost = RequestBody.create(MEDIA_TYPE_JSON, param.content);
                request = new Request.Builder()
                        .post(requestBodyPost) // POST请求
                        .url(param.url)
                        .tag(param.tag)
                        .build();
                break;
            }
            default:
                break;
        }

        Call call = mHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void cancel(String url) {
        for(Call call : mHttpClient.dispatcher().queuedCalls()) {
            if(call.request().url().toString().equals(url)) {
                call.cancel();
                return;
            }
        }
    }

    public static void cancelAll(Object tag) {
        for(Call call : sInstance.mHttpClient.dispatcher().queuedCalls()) {
            if(call.request().tag().equals(tag)) {
                call.cancel();
            }
        }
    }
}
