package com.brian.codeblog.manager;

import android.text.TextUtils;

import com.brian.codeblog.datacenter.DataManager;
import com.brian.codeblog.datacenter.database.BlogInfoTable;
import com.brian.codeblog.model.BlogInfo;
import com.brian.common.utils.FileUtil;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.Md5;

import java.util.List;

public class BlogManager {

    private static final int PAGE_COUNT = 15;

    private static BlogManager sInstance;

    private BlogManager() {
    }

    public static BlogManager getInstance() {
        if (sInstance == null) {
            synchronized (BlogManager.class) {
                if (sInstance == null) {
                    sInstance = new BlogManager();
                }
            }
        }
        return sInstance;
    }

    public void saveBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        if (TextUtils.isEmpty(blogInfo.blogId)) {
            blogInfo.blogId = Md5.getMD5ofStr(blogInfo.link);
        }
        blogInfo.visitTime = System.currentTimeMillis();
        BlogInfoTable.getInstance().saveBlog(blogInfo);
    }

    public boolean doFavo(BlogInfo blogInfo, boolean isFavo) {
        if (blogInfo == null) {
            return false;
        }
        blogInfo.isFavo = isFavo;
        UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_FAVO, TypeManager.getBlogName(blogInfo.type));
        return BlogInfoTable.getInstance().doFavo(blogInfo);
    }

    public boolean isFavo(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return false;
        }
        BlogInfo info = BlogInfoTable.getInstance().query(blogInfo.blogId);
        if (info == null) {
            LogUtil.w("this blog is not exist!");
            return false;
        }
        return info.isFavo;
    }

    public boolean updateBlogCachePath(BlogInfo blogInfo) {
        if (blogInfo == null || TextUtils.isEmpty(blogInfo.localPath)) {
            return false;
        }
        return BlogInfoTable.getInstance().updateBlogCachePath(blogInfo);
    }

    public void removeBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        FileUtil.deleteFile(DataManager.getBlogCachePath(Md5.getMD5ofStr(blogInfo.blogId)));
        BlogInfoTable.getInstance().delete(blogInfo);
    }

    public void clear() {
        BlogInfoTable.getInstance().clearTable();
    }

    public void clearByType(int type) {
        BlogInfoTable.getInstance().deleteBlogsByType(type);
    }

    public void clearHistoryList() {
        BlogInfoTable.getInstance().deleteHistoryBlogs();
    }

    public void clearFavoList() {
        BlogInfoTable.getInstance().deleteFavoBlogs();
    }

    public List<BlogInfo> getHistoryBlogList(int page) {
        return BlogInfoTable.getInstance().getHistoryList(PAGE_COUNT*page, PAGE_COUNT);
    }

    public List<BlogInfo> getFavoBlogList(int page) {
        return BlogInfoTable.getInstance().getFavoList(PAGE_COUNT*page, PAGE_COUNT);
    }
}
