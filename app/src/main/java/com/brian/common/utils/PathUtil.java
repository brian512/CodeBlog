package com.brian.common.utils;

import android.content.Context;

import com.brian.codeblog.Env;

import java.io.File;

/**
 * 文件路径
 * Created by huamm on 2016/10/19 0019.
 */

public class PathUtil {
    public static String getCacheDirPath() {
        Context context = Env.getContext();

        // 先尝试选择外部缓存
        File file = context.getExternalCacheDir();
        if (file != null) {
            String extCacheDir = file.getAbsolutePath();
            FileUtil.ensureFolderExists(extCacheDir);
            if (FileUtil.isDirExist(extCacheDir)) {
                return extCacheDir;
            }
        }

        // 暂尝试选择内部缓存
        String intcacheDir = context.getCacheDir().getAbsolutePath();
        FileUtil.ensureFolderExists(intcacheDir);
        return intcacheDir;
    }
}
