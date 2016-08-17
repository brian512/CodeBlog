package com.brian.csdnblog.manager;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HistoryBlogManager {
    
    private static final int MAX_CACHE_COUNT = 50;

    private static String FILE_NAME_CACHE = "history_list";
    
    private static HistoryBlogManager sInstance;
    
    private List<BlogInfo> mHistoryBlogs;
    
    private HistoryBlogManager() {
        FILE_NAME_CACHE = Env.getContext().getFilesDir() + "/" + FILE_NAME_CACHE;
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
            StringBuilder json = FileUtil.readFile(FILE_NAME_CACHE);
            if (!TextUtils.isEmpty(json)) {
                mHistoryBlogs = new Gson().fromJson(json.toString(),
                        new TypeToken<List<BlogInfo>>() {} .getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mHistoryBlogs == null || mHistoryBlogs.isEmpty()) {
            mHistoryBlogs = new ArrayList<BlogInfo>();
        }
    }
    
    public void addBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        for (BlogInfo info : mHistoryBlogs) {
            if (info.link.equalsIgnoreCase(blogInfo.link)) {
                mHistoryBlogs.remove(info);
                break;
            }
        }
        mHistoryBlogs.add(0, blogInfo);
        if (mHistoryBlogs.size() > MAX_CACHE_COUNT) {
            mHistoryBlogs = mHistoryBlogs.subList(0, MAX_CACHE_COUNT);
        }
        saveList();
    }
    
    public void removeBlog(String blogUrl) {
        if (mHistoryBlogs == null || mHistoryBlogs.isEmpty()) {
            return;
        }
        for (int i = 0; i < mHistoryBlogs.size(); i++) {
            if (mHistoryBlogs.get(i).link.equals(blogUrl)) {
                mHistoryBlogs.remove(i);
                return;
            }
        }
        saveList();
    }
    
    public void clear() {
        if (mHistoryBlogs == null || mHistoryBlogs.isEmpty()) {
            return;
        }
        mHistoryBlogs.clear();
        saveList();
    }
    
    public List<BlogInfo> getHistoryBlogList() {
        return mHistoryBlogs;
    }
    
    private void saveList() {
        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                FileUtil.writeFile(FILE_NAME_CACHE, gson.toJson(mHistoryBlogs), false);
            }
        });
    }
}
