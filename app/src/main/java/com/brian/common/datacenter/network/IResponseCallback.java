package com.brian.common.datacenter.network;

import com.brian.codeblog.model.BaseType;

/**
 * Created by huamm on 2016/9/20 0020.
 */

public interface IResponseCallback<TResult extends BaseType> {
    /**
     * 获取数据成功，rtn == 0;
     */
    void onSuccess(TResult result);

    /**
     * 获取数据失败，rtn != 0
     */
    void onError(int rtn, String msg);

    /**
     * 请求失败，网络原因或服务器原因
     */
    void onFailure(int errorCode, String msg);
}