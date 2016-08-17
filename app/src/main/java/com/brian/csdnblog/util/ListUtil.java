package com.brian.csdnblog.util;

import java.util.List;

public class ListUtil {

    public static boolean isListEmpty(List<?> list) {
        if (list == null || list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
    
}
