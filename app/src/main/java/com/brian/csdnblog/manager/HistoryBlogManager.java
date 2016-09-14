package com.brian.csdnblog.manager;

import com.brian.csdnblog.datacenter.database.BlogInfoTable;
import com.brian.csdnblog.model.BlogInfo;

import java.util.ArrayList;
import java.util.List;

public class HistoryBlogManager {
    
//    private static final int MAX_CACHE_COUNT = 50;

//    private static String FILE_NAME_CACHE = "history_list";
    
    private static HistoryBlogManager sInstance;
    
    private List<BlogInfo> mHistoryBlogs;
    
    private HistoryBlogManager() {
//        FILE_NAME_CACHE = Env.getContext().getFilesDir() + "/" + FILE_NAME_CACHE;
        initHistoryBlogList();
    }
    
    public static HistoryBlogManager getInstance() {
        if (sInstance == null) {
            synchronized (HistoryBlogManager.class) {
                if (sInstance == null) {
                    sInstance = new HistoryBlogManager();
                }
            }
        }
        return sInstance;
    }
    
    private void initHistoryBlogList() {
        try {
            mHistoryBlogs = BlogInfoTable.getInstance().queryList(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mHistoryBlogs == null || mHistoryBlogs.isEmpty()) {
            mHistoryBlogs = new ArrayList<>();
        }
    }
    
    public void addBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        mHistoryBlogs.add(0, blogInfo);
        BlogInfoTable.getInstance().saveAsyc(blogInfo);
    }
    
    public void removeBlog(BlogInfo blogInfo) {
        if (mHistoryBlogs == null || mHistoryBlogs.isEmpty()) {
            return;
        }
        BlogInfoTable.getInstance().delete(blogInfo);
    }
    
    public void clear() {
        if (mHistoryBlogs == null || mHistoryBlogs.isEmpty()) {
            return;
        }
        mHistoryBlogs.clear();
        BlogInfoTable.getInstance().clearTable();
    }
    
    public List<BlogInfo> getHistoryBlogList() {
        return mHistoryBlogs;
    }
}
