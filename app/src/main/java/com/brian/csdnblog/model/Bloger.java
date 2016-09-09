
package com.brian.csdnblog.model;

/**
 * 博主个人信息
 */
public class Bloger {
    public int blogerType;
    public String blogerID; // 博主ID
    public String headUrl; // 博主头像
    public String nickName; // 博主名称


    public static Bloger getDefaultBloger() {
        Bloger bloger = new Bloger();
        bloger.blogerID = "brian512";
        bloger.nickName = "年少的风";
        return bloger;
    }
}
