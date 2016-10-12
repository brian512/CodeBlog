package com.brian.codeblog.model;

/**
 * 阅读记录表，后期可增加图表显示
 * Created by huamm on 2016/9/26 0026.
 */
public class ReadInfo {
    /**
     * 当天时间戳，取0点为基准
     */
    public int date;

    /**
     * 每一天的阅读博文数量
     */
    public int count;

    /**
     * 每天阅读总时长
     */
    public int timeSecond;


    /**
     * 只保留天数
     */
    public static int getDate(long timeStamp) {
        return (int)(timeStamp / (3600_000 * 24));
    }
}
