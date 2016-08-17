
package com.brian.csdnblog.manager;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.LogUtil;

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    public static final long DEFAULT_SIZE_MAX = 15 * 1<<20;

    public static final String PATH_DIR_ROOT = "/data/data/" + Env.getPackageName();
    public static final String PATH_DIR_DATA = Env.getContext().getFilesDir().getAbsolutePath();
    public static final String PATH_DIR_CACHE = Env.getContext().getCacheDir().getAbsolutePath();
    public static final String PATH_DIR_DB = PATH_DIR_ROOT + "/databases";
    public static final String PATH_DIR_PREFERENCE = PATH_DIR_ROOT + "/shared_prefs";
    public static final String PATH_DIR_CACHE_PIC = PATH_DIR_CACHE;

    private static DataManager sDataManager = null;

    public static DataManager getInstance() {
        if (sDataManager == null) {
            synchronized (DataManager.class) {
                if (sDataManager == null) {
                    sDataManager = new DataManager();
                }
            }
        }
        return sDataManager;
    }

    private DataManager() {
    }

    public void onVersionCodeUpgrade() {
//        int oldVersion = SPUtil.getParam(App.sAppContext, SPUtil.KEY_VERSION, 0);
        int oldVersion = 0;
        LogUtil.v(TAG, "oldVersion=" + oldVersion);
        if (oldVersion == Env.getVersionCode()) {
            return;
        } else if (oldVersion <= 1) {
        }
//        SPUtil.setParam(App.sAppContext, SPUtil.KEY_VERSION, App.sVersionCode);
    }


    /**
     * 仅清除cache目录
     */
    public void clearCacheData() {
        FileUtil.deleteDirsAndFiles(PATH_DIR_CACHE);
    }

    /**
     * 清除cache和自定义的缓存
     */
    public void clearAllCacheData() {
        clearCacheData();
        FileUtil.deleteDirsAndFiles(PATH_DIR_CACHE_PIC);
    }
    
    /**
     * 清除files目录和数据库
     */
    public void clearData() {
        clearAllCacheData();
        FileUtil.deleteDirsAndFiles(PATH_DIR_DATA);
        FileUtil.deleteDirsAndFiles(PATH_DIR_DB);
    }

    /**
     * 清除所有数据
     */
    public void clearAllData() {
        clearData();
        FileUtil.deleteDirsAndFiles(PATH_DIR_PREFERENCE);
    }
}
