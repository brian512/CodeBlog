package com.brian.csdnblog.manager;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.FileUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FavoBlogManager {

    private static String FILE_NAME_CACHE = "favo_list";
    
    private static FavoBlogManager sInstance;
    
    private List<BlogInfo> mFavoBlogs;
    
    private FavoBlogManager() {
        FILE_NAME_CACHE = Env.getContext().getFilesDir() + "/" + FILE_NAME_CACHE;
        initFavoBlogList();
    }
    
    public static FavoBlogManager getInstance() {
        if (sInstance == null) {
            synchronized (FavoBlogManager.class) {
                if (sInstance == null) {
                    sInstance = new FavoBlogManager();
                }
            }
        }
        return sInstance;
    }
    
    private void initFavoBlogList() {
        try {
            StringBuilder json = FileUtil.readFile(FILE_NAME_CACHE);
            if (!TextUtils.isEmpty(json)) {
                mFavoBlogs = new Gson().fromJson(json.toString(),
                        new TypeToken<List<BlogInfo>>() {} .getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mFavoBlogs == null) {
            mFavoBlogs = new ArrayList<BlogInfo>();
        }
    }
    
    public void addBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        for (BlogInfo info : mFavoBlogs) {
            if (info.link.equalsIgnoreCase(blogInfo.link)) {
                mFavoBlogs.remove(info);
                break;
            }
        }
        UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_FAVO, TypeManager.getBlogName(blogInfo.type));
        mFavoBlogs.add(0, blogInfo);
        saveList();
    }
    
    public void removeBlog(String blogUrl) {
        if (mFavoBlogs == null || mFavoBlogs.isEmpty()) {
            return;
        }
        for (int i = 0; i < mFavoBlogs.size(); i++) {
            if (mFavoBlogs.get(i).link.equals(blogUrl)) {
                mFavoBlogs.remove(i);
                return;
            }
        }
        saveList();
    }
    
    public void clear() {
        if (mFavoBlogs == null || mFavoBlogs.isEmpty()) {
            return;
        }
        mFavoBlogs.clear();
        saveList();
    }
    
    public List<BlogInfo> getFavoBlogList() {
        return mFavoBlogs;
    }
    
    private void saveList() {
        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                FileUtil.writeFile(FILE_NAME_CACHE, gson.toJson(mFavoBlogs), false);
            }
        });
    }
    
    public boolean isFavo(String blogUrl) {
        if (mFavoBlogs == null) {
            initFavoBlogList();
        }
        for (int i = 0; i < mFavoBlogs.size(); i++) {
            if (mFavoBlogs.get(i).link.equals(blogUrl)) {
                return true;
            }
        }
        return false;
    }
}
