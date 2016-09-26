
package com.brian.csdnblog.model;

import java.io.Serializable;

public class SearchResult implements Serializable {
    public String title = "";
    public String authorTime = "";
    public String searchDetail = "";
    public String link = "";
    public String blogerJson = "";
    public String blogerID = "";
    public int type;

    @Override
    public String toString() {
        return "title:" + title + "\nauthorTime" + authorTime
                + "\nsearchDetail" + searchDetail + "\nlink" + link;
    }
}