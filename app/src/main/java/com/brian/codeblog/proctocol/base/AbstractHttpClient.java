package com.brian.codeblog.proctocol.base;


import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.brian.codeblog.model.BaseType;
import com.brian.codeblog.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * http请求的基类
 * <p/>
 * 注意：
 * 1.所有的http请求相关业务类需要继承本类
 * 2.子类需定义BaseRequestParam和BaseResponseResultData
 * 3.子类负责实现convRequestParam(TParam param)和convResponseResult(JSONObject jsonObject)方法
 * 4.如果请求参数或者返回结果没有任何定制参数，可以直接传入EmptyRequestParam和EmptyResponseResultData
 *
 * @author ls
 */
public abstract class AbstractHttpClient<TParam extends BaseRequestParam, TResult extends BaseType> {
    protected static final String TAG = AbstractHttpClient.class.getSimpleName();

    private Object mTag;

    /**
     * 设置http请求参数
     */
    public abstract HttpClientParam convRequestParam(TParam param);


    /**
     * 转换http请求结果
     */
    public abstract TResult convResponseResult(String responseContent);


    /**
     * http请求结果结构
     */
    public static class ResponseResult<TResult> {

        /**
         * 结果码 0：成功
         */
        public int rtn;

        /**
         * 相关信息
         */
        public String msg;

        /**
         * session状态
         */
        public int sessionStatus;

        /**
         * 业务相关数据对象
         */
        public TResult data;

        /**
         * 解析前，http返回的ResponseContent，用作上层做缓存时保存
         */
        public String responseContent;
    }

    private IResponseCallback<TResult> mListener;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 获取推荐数据列表
     */
    public void request(TParam param, IResponseCallback<TResult> listener) {
        mListener = listener;

        final HttpClientParam clientParam = convRequestParam(param);
        try {
            if (clientParam.method == HttpClientParam.METHOD_POST) {
                setPostMangoKey(clientParam);
            }
        } catch (Exception e) {
            LogUtil.printError(e);
        }

        OkHttpRequest.getInstance().request(clientParam, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.printError(e);
                performCallback(-1, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseContent;
                if (TextUtils.isEmpty(clientParam.charset)) {
                    responseContent = response.body().string();
                } else {
                    responseContent = new String(response.body().bytes(), clientParam.charset);
                }

                LogUtil.log("url=" + call.request().url());
//                LogUtil.log("responseContent=" + responseContent);
                if (mListener != null) {
                    ResponseResult<TResult> result = processResponseResult(responseContent);
                    performCallback(response.code(), result);
                }
            }
        });
    }

    /**
     * 当采用Post的时候，默认获取content的md5值作为签名
     */
    private void setPostMangoKey(HttpClientParam clientParam) throws JSONException {
        // 这里必须先转换为JSON，然后用JSON的字符串签名，否则可能导致JSON的键值对顺序变化（不一定，依赖系统SDK实现）
        JSONObject jContent = new JSONObject(clientParam.content);
        String salt = "";
    }


    /**
     * 处理查询结果
     */
    private ResponseResult<TResult> processResponseResult(String responseContent) {
        ResponseResult<TResult> result = new ResponseResult<>();

//        try {
//            JSONObject jContent = new JSONObject(responseContent);
//            result.rtn = jContent.getInt("rtn");
//            result.msg = jContent.getString("msg");
//            result.sessionStatus = jContent.optInt("sessionStatus");
//            result.responseContent = responseContent;
//
//            if (result.rtn == 0) {
//                JSONObject jData = jContent.optJSONObject("data");
//                if (jData != null) {
//                    result.data = convResponseResult(jData);
//                }
//            }
//        } catch (JSONException e) {
//            LogUtil.printError(e);
//        } catch (JsonSyntaxException e) {
//            LogUtil.printError(e);
//        } catch (IllegalStateException e) {
//            LogUtil.printError(e);
//        } catch (Exception e) {
//            LogUtil.printError(e);
//        }

        result.data = convResponseResult(responseContent);
        return result;
    }

    /**
     * 取消请求
     */
    public void cancel() {
    }

    private void performCallback(final int responseCode, final ResponseResult<TResult> result) {
        if (mListener == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (responseCode == 200 && result != null) {
                    if (result.rtn == 0) {// && result.data != null
                        mListener.onSuccess(result.data);
                    } else {
                        if (result.data == null) {
                            result.msg += "解析出错";
                        }
                        mListener.onError(result.rtn, result.msg);
                    }
                } else {
                    // 请求出错
                    mListener.onFailure(responseCode, "");
                }
            }
        });
    }
}
