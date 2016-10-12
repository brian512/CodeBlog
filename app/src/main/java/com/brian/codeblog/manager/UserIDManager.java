package com.brian.codeblog.manager;

import com.brian.codeblog.Env;
import com.brian.codeblog.util.DeviceUtil;
import com.brian.codeblog.util.Md5;

public class UserIDManager {

    
    private static UserIDManager sInstance = null;
    
    private UserIDManager() {}
    
    public static UserIDManager getInstance() {
        if (sInstance == null) {
            synchronized (UserIDManager.class) {
                if (sInstance == null) {
                    sInstance = new UserIDManager();
                }
            }
        }
        return sInstance;
    }
    
    public String getUserID() {
        return Md5.getMD5ofStr(DeviceUtil.getIMEI(Env.getContext()));
    }
    
}
