/**
 * Copyright (C) 2014 Togic Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brian.csdnblog.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

/**
 * @author jar @date 2014年5月15日
 */
@SuppressWarnings("deprecation")
public class SdCardSize {
    
    /**
     * 获得SD卡总大小
     * 
     * @return
     */
    public static long getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     * 
     * @return
     */
    public static long getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     * 获得机身内存总大小
     * 
     * @return
     */
    public static long getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    /**
     * 获得机身可用内存
     * 
     * @return
     */
    public static long getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     * 判断SDCardSize是否充足
     * 
     * @return
     */
    public static boolean checkSDCardSizeIsEnough(long size) {
        // notify storge is not full
        long sdcardAvailableSize = SdCardSize.getSDAvailableSize();
        if (size > sdcardAvailableSize) {
            return false;
        }
        return true;
    }

    public static boolean isSDCardAvailable(int contentLength) {
        if (isSDCardMounted()) {
            String sdcardPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (FileUtil.isDirectoryWritable(sdcardPath)
                    && getUsableSpace(sdcardPath) >= contentLength) {
                return true;
            }
        }
        return false;
    }

    public static final boolean isSDCardMounted() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isDataDirAvailable(Context ctx, int contentLength) {
        String path = ctx.getFilesDir().getAbsolutePath();
        if (FileUtil.isDirectoryWritable(path)
                && getUsableSpace(path) >= contentLength) {
            return true;
        }
        return false;
    }

    public static final long getUsableSpace(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }
        try {
            String dirPath = "";
            if (filePath.endsWith(File.separator)) {
                dirPath = filePath;
            } else {
                dirPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
            }
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            StatFs sf = new StatFs(dirPath);
            return (long) sf.getAvailableBlocks() * sf.getBlockSize();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static final long getTotalSpace(String filePath) {
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                return 0;
            }
            StatFs sf = new StatFs(filePath);
            return (long) sf.getBlockCount() * sf.getBlockSize();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static final long getSDCardTotalSpace() {
        if (isSDCardMounted()) {
            String sdcardPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            return getTotalSpace(sdcardPath);
        }
        return 0;
    }

    public static List<String> getMountedStorage() {
        String prefix_udisk = "/dev/block/vold/";
        String mountsFile = "/proc/mounts";
        List<String> list = new ArrayList<String>();
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            list.add(Environment.getExternalStorageDirectory().getPath());
        }

        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(mountsFile));
            while ((line = br.readLine()) != null) {
                if (line.startsWith(prefix_udisk)) {
                    String[] strs = line.split(" ");
                    if (strs != null && strs.length > 2) {
                        list.add(strs[1]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.closeStream(br);
        }
        return list;
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return new File(context.getExternalFilesDir(null), "cache");
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName()
                + "/files/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath()
                + cacheDir);
    }

    public static File getInternalCacheDir(Context context) {
        return new File(context.getFilesDir(), "cache");
    }

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File getDiskCacheDir(Context context) {
        return getDiskCacheDir(context, "livetv");
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = context.getCacheDir().getPath();
        File cacheDir = new File(cachePath + File.separator + uniqueName);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        return cacheDir;
    }

    public static File getExternalCacheDir(Context context, String uniqueName) {
        File customerCacheDir = getExternalCacheDir(context);
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                || !isExternalStorageRemovable() && customerCacheDir != null
                && customerCacheDir.canWrite()) {
            return new File(customerCacheDir.getPath() + File.separator
                    + uniqueName);
        }
        return null;
    }

    public static File getInternalCacheDir(Context context, String uniqueName) {
        return new File(getInternalCacheDir(context).getPath() + File.separator
                + uniqueName);
    }
    
    /**
     * Get the memory class of this device (approx. per-app memory limit)
     * 
     * @param context
     * @return
     */
    public static int getMemoryClass(Context context) {
        return ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }
}
