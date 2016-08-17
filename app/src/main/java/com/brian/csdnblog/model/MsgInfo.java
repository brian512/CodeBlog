package com.brian.csdnblog.model;

import org.litepal.crud.DataSupport;

public class MsgInfo extends DataSupport {

    public static final int SELF = 0;
    public static final int ROBOT = 1;
    public static final int SYSTEM = 100;
    
    public int id;
    public String text;
    
    public MsgInfo(int id, String text) {
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
