package com.brian.csdnblog.manager;

import android.content.Context;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.util.LogUtil;
import com.duowan.mobile.netroid.Listener;
import com.duowan.mobile.netroid.RequestQueue;
import com.duowan.mobile.netroid.Volley;
import com.duowan.mobile.netroid.VolleyError;
import com.duowan.mobile.netroid.request.StringRequest;

import java.util.concurrent.TimeUnit;

public class DataFetcher {
    private static final String TAG = DataFetcher.class.getSimpleName();
    private Context mContext = null;
    private RequestQueue mQueue = null;
    
    private static DataFetcher sDataFetcher = null;
    
    public static DataFetcher getInstance() {
        if (sDataFetcher == null) {
            synchronized (DataFetcher.class) {
                if (sDataFetcher == null) {
                    sDataFetcher = new DataFetcher();
                }
            }
        }
        return sDataFetcher;
    }
    
    private DataFetcher() {
        this(Env.getContext());
    }
    private DataFetcher(Context context) {
        mContext = context.getApplicationContext();
        mQueue = Volley.newRequestQueue(mContext, 20*1024*1024);
    }
    
    public interface OnFetchDataListener<T> {
        public void onFetchFinished(T result);
    }
    
    /**
     * callback in ui thread
     */
    public void fetchString(final String url, final OnFetchDataListener<Result<String>> listener) {
        long expireTime = TimeUnit.DAYS.toMillis(7);// 七天过期
        fetchString(url, expireTime, listener);
    }
    
    /**
     * callback in ui thread
     */
    public void fetchString(final String url, String charset, final OnFetchDataListener<Result<String>> listener) {
        long expireTime = TimeUnit.DAYS.toMillis(7);// 七天过期
        fetchString(url, expireTime, false, charset, listener);
    }
    
    /**
     * callback in ui thread
     */
    public void fetchString(final String url, boolean isForceUpdate, final OnFetchDataListener<Result<String>> listener) {
        long expireTime = TimeUnit.DAYS.toMillis(7);// 七天过期
        fetchString(url, expireTime, isForceUpdate, null, listener);
    }
    
    public void fetchString(final String url, long expireTime, final OnFetchDataListener<Result<String>> listener) {
        fetchString(url, expireTime, false, null, listener);
    }
    /**
     * callback in ui thread
     */
    public void fetchString(final String url, long expireTime, boolean isForceUpdate, String charset, final OnFetchDataListener<Result<String>> listener) {
        LogUtil.w(TAG, "url=" + url);
        StringRequest request = new StringRequest(url, new Listener<String>() {
            
            @Override
            public void onSuccess(String response) {
                LogUtil.d("requesturl=" + url);
//                LogUtil.d("response=" + response);
                Result<String> result = new Result<>(url, response);
                listener.onFetchFinished(result);
            }
            
            @Override
            public void onError(VolleyError error) {
                super.onError(error);
                Result<String> result = new Result<>(url, "");
                listener.onFetchFinished(result);
            }
        });

        request.setDefaultCharset(charset);
        request.setCacheExpireTime(expireTime);
        request.setForceUpdate(isForceUpdate);
        mQueue.add(request);
    }
    
    
    public static class Result<T> {
        public String url;
        public T data;
        
        public Result(String url, T data) {
            this.url = url;
            this.data = data;
        }
        
    }
}
