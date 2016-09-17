
package com.brian.csdnblog.model;

/**
 * 博主个人信息
 */
public class Bloger extends BaseType {
    public int blogerType;
    public String blogerID; // 博主ID
    public String headUrl; // 博主头像
    public String nickName; // 博主名称
    public String homePageUrl; // 主页链接
    public long dateStampFollow; // 关注时间
    public int blogCount; // 博文总数
    public String bio; // 个人描述

    public boolean isFollowed = false;

    public String toJson() {
        return getGson().toJson(this);
    }

    public static Bloger fromJson(String blogJson) {
        return getGson().fromJson(blogJson, Bloger.class);
    }
}
