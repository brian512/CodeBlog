package com.brian.common.tools;

import com.brian.common.utils.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Gson辅助类
 * Created by huamm on 2016/11/1 0001.
 */

public class GsonHelper<T> {

    private static Gson sGson = new Gson();

    public String convert2String(List<T> list) {
        try {
            return sGson.toJson(list);
        } catch (Exception e) {
            LogUtil.printError(e);
            return "";
        }
    }

    public List<T> fromJson(String json) {
        try {
            return sGson.fromJson(json, new TypeToken<ArrayList<T>>(){}.getType());
        } catch (Exception e) {
            LogUtil.printError(e);
            return null;
        }
    }
}
