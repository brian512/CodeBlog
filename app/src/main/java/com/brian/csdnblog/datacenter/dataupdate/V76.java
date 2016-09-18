package com.brian.csdnblog.datacenter.dataupdate;

import android.text.TextUtils;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.manager.BlogManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.Md5;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by huamm on 2016/9/18 0018.
 * 在v75将不再使用文件进行数据存储，而改用数据库，并且数据实体也有更改
 */
public class V76 {

    public static final String FILE_NAME_CACHE_HISTORY = Env.getContext().getFilesDir() + "/history_list";
    public static final String FILE_NAME_CACHE_FAVO = Env.getContext().getFilesDir() + "/favo_list";

    public static void updateData() {
        convertData(FILE_NAME_CACHE_HISTORY, false);
        convertData(FILE_NAME_CACHE_FAVO, true);

        FileUtil.deleteFile(FILE_NAME_CACHE_HISTORY);
        FileUtil.deleteFile(FILE_NAME_CACHE_FAVO);
    }

    private static void convertData(String dataPath, boolean isFavo) {
        StringBuilder json = FileUtil.readFile(dataPath);
        if (!TextUtils.isEmpty(json)) {
            List<BlogInfoOld> blogs = new Gson().fromJson(json.toString(),
                    new TypeToken<List<BlogInfoOld>>() {} .getType());
            if (blogs != null && !blogs.isEmpty()) {
                for (BlogInfoOld info : blogs) {
                    BlogManager.getInstance().saveBlog(convertBlogInfo(info, isFavo));
                }
            }
        }
    }

    private static BlogInfo convertBlogInfo(BlogInfoOld info, boolean isFavo) {
        BlogInfo blogInfo = new BlogInfo();
        blogInfo.link = info.link;
        blogInfo.dateStamp = info.dateStamp;
        blogInfo.title = info.title;
        blogInfo.type = info.type;
        blogInfo.summary = info.description;
        blogInfo.extraMsg = info.msg;
        blogInfo.blogId = Md5.getMD5ofStr(blogInfo.link);
        blogInfo.visitTime = System.currentTimeMillis();
        blogInfo.isFavo = isFavo;
        return blogInfo;
    }

    /**
     * 博客实体类
     */
    static class BlogInfoOld {
        public int id; // id
        public String title; // 标题
        public String link; // 文章链接
        public String dateStamp; // 博客发布时间
        public String description;// 文章摘要
        public String content; // 文章内容
        public String msg; // 消息
        public int articleType; // 博客类型，原创，翻译，转载
        public int type; // 博客类型，在TypeManager中定义
    }
}
