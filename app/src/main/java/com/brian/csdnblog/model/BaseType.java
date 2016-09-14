package com.brian.csdnblog.model;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by huamm on 2016/9/14 0014.
 */
public abstract class BaseType implements Serializable {

    private static Gson mGson;

    protected static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }
}
