package com.brian.csdnblog.manager;

import android.os.Handler;
import android.text.TextUtils;

import com.brian.csdnblog.util.LogUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataFetcher {
    private static final String TAG = DataFetcher.class.getSimpleName();

    private OkHttpClient mOkHttpClient;

    private Handler mHandler;

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
        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler();
    }

    public interface OnFetchDataListener<T> {
        void onFetchFinished(T result);
    }

    /**
     * callback in ui thread
     */
    public void fetchString(final String url, final OnFetchDataListener<Result<String>> listener) {
        fetchString(url, null, listener);
    }

    /**
     * callback in ui thread
     */
    public void fetchString(final String url, final String charset, final OnFetchDataListener<Result<String>> listener) {
        LogUtil.w(TAG, "url=" + url);
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            final Result<String> result = new Result<>(url, "");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFetchFinished(result);
                }
            });
            return;
        }
        Request.Builder builder = new Request.Builder().url(url).get();
        Request okRequest = builder.build();
        Call call = mOkHttpClient.newCall(okRequest);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final Result<String> result = new Result<>(url, "");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFetchFinished(result);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtil.d("requesturl=" + url);
                final Result<String> result = new Result<>();
                result.url = url;
                if (!TextUtils.isEmpty(charset)) {
                    result.data = new String(response.body().bytes(), charset);
                } else {
                    result.data = response.body().string();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFetchFinished(result);
                    }
                });
            }
        });
    }


    public static class Result<T> {
        public String url;
        public T data;

        public Result() {
        }

        public Result(String url, T data) {
            this.url = url;
            this.data = data;
        }
    }
}
