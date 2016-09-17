package com.brian.csdnblog.manager;

import android.text.TextUtils;

import com.brian.csdnblog.datacenter.database.BlogInfoTable;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.Md5;

import java.util.List;

public class HistoryBlogManager {

//    private static final int MAX_CACHE_COUNT = 50;

//    private static String FILE_NAME_CACHE = "history_list";
//    private static String FILE_NAME_CACHE = "favo_list";

    private static HistoryBlogManager sInstance;

    private HistoryBlogManager() {
//        FILE_NAME_CACHE = Env.getContext().getFilesDir() + "/" + FILE_NAME_CACHE;
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

    public void saveBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        blogInfo.visitTime = System.currentTimeMillis();
        BlogInfoTable.getInstance().saveBlogAsyc(blogInfo);
    }

    public void doFavo(BlogInfo blogInfo, boolean isFavo) {
        blogInfo.isFavo = isFavo;
        BlogInfoTable.getInstance().doFavo(blogInfo);
    }

    public boolean isFavo(BlogInfo blogInfo) {
        BlogInfo info = BlogInfoTable.getInstance().query(blogInfo.blogId);
        if (info == null) {
            LogUtil.w("this blog is not exist!");
            return false;
        }
        return info.isFavo;
    }

    public void removeBlog(BlogInfo blogInfo) {
        if (blogInfo == null) {
            return;
        }
        BlogInfoTable.getInstance().delete(blogInfo);
    }

    public void clear() {
        BlogInfoTable.getInstance().clearTable();
    }

    public void clearByType(int type) {
        BlogInfoTable.getInstance().deleteBlogsByType(type);
    }

    public List<BlogInfo> getHistoryBlogList() {
        return BlogInfoTable.getInstance().getHistoryList();
    }

    public List<BlogInfo> getFavoBlogList() {
        return BlogInfoTable.getInstance().getFavoList();
    }

    public void fetchBlogContent(String url, DataFetcher.OnFetchDataListener<DataFetcher.Result<String>> listener) {
        fetchBlogContent(url, null, listener);
    }

    public void fetchBlogContent(String url, String charset, DataFetcher.OnFetchDataListener<DataFetcher.Result<String>> listener) {
        String cachePath = DataManager.getBlogCachePath(Md5.getMD5ofStr(url));
        if (FileUtil.checkFileExists(cachePath)) {
            DataFetcher.Result<String> result = new DataFetcher.Result<>();
            result.url = url;
            result.data = FileUtil.getFileContent(cachePath);
            if (!TextUtils.isEmpty(result.data)) {
                listener.onFetchFinished(result);
                return;
            }
        }
        DataFetcher.getInstance().fetchString(url, charset, listener);
    }
}
