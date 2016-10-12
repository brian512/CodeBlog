package com.brian.codeblog.model;

public class ChatMsgInfo extends BaseType {

    public static final int SELF = 0;
    public static final int ROBOT = 1;
    public static final int SYSTEM = 100;
    
    public int id;
    public String text;
    
    public ChatMsgInfo(int id, String text) {
        this.id = id;
        this.text = text;
    }
    
    @Override
    public String toString() {
        if (id == SELF) {
            return "SELF:" + text;
        } else if (id == ROBOT) {
            return "ROBOT:" + text;
        }
        return id + ":" + text;
    }
}
