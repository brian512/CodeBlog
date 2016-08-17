package com.brian.csdnblog.manager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.webkit.MimeTypeMap;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.LogUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


/**
 * 下载管理工具，采用系统系在服务
 */
public class SystemDownloaderHelper {

    private static final String TAG = SystemDownloaderHelper.class.getSimpleName();
    
    /**
     * 创建任务的参数
     */
    public static class TaskInfo {
        
        /** 下载提示 */
        public String url;
        
        /** 是否在通知栏显示 */
        public boolean showNotification;
        
        /** 通知栏显示的标题 */
        public String title;
        
        /** 通知栏显示的描述 */
        public String description;
        
        /** 保存路径，需要使用接口Context().getExternalCacheDir() 获取保存目录，只支持存在外部文件夹 */
        /** 注意：
         *  （1）由于DownloadManager遇到重名的文件，会自动将保存文件名加迭代数字，
         *       所以，此路径不一定是最终的保存路径。最终路径以最后回调接口为主
         *  （2）由于获取实际保存路径的API需要DownloadManager.COLUMN_LOCAL_FILENAME需要API11（2.4），
         *       所以暂时不使用
         *  （3）总之，下载文件目录的唯一性，需要调度者来保证
         */
        public String savePath;
        
        /** 最终下载完成，实际的保存路径 */
        //public String actualSavePath;
        
        /** 下载完成回调
         * 注意：系统API只能监听到下载完成的状态
         */
        public Runnable completeRunnable;
    }
    

    
    /**
     * Context
     */
    private Context mContext = null;

    /**
     * 下载器
     */
    private DownloadManager mDownloadManager;
    
    /**
     * 任务列表
     */
    private HashMap<Long, TaskInfo> mTaskList = new HashMap<Long, TaskInfo>(); 
    
    /**
     * 触发下载完成回调的Handler
     */
    private Handler mHandler = new Handler();
    
    
    /** 实例化 */
    private SystemDownloaderHelper(Context context){
        mContext = context;
        
        // 创建服务
        mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE); 
        
        // 绑定监听
        registerReceiver();
    }

    /**
     * 获取单例
     */
    private static SystemDownloaderHelper sInstance = null;
    public static SystemDownloaderHelper getInstance() {
        if (sInstance == null) { // 提前预判，减少锁的范围
            synchronized(TAG){
                if (sInstance == null) {
                    Context context = Env.getContext();
                    sInstance = new SystemDownloaderHelper(context);
                }
            } 
        }
        return sInstance;
    }
    

    /**
     * 创建下载任务，创建后自动运行
     * @param taskInfo
     * @return 任务ID
     */
    public long createTask(TaskInfo taskInfo) {

        // 设置下载属性
        Uri resource = Uri.parse(taskInfo.url);
        DownloadManager.Request request = new DownloadManager.Request(resource);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        //request.setAllowedOverRoaming(false); // 禁止漫游
        
        // 设置文件类型
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(taskInfo.url);
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(extension);
        request.setMimeType(mimeString);
        
        // 在通知栏中显示
        if (taskInfo.showNotification) {
            if (Build.VERSION.SDK_INT >= 11) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // 下载完成任然保存
            } else {
                request.setShowRunningNotification(true);
            }
            request.setVisibleInDownloadsUi(true);
        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN); // 下载完成任然保存
            } else {
                request.setShowRunningNotification(false);
            }
            request.setVisibleInDownloadsUi(false);
        }
        request.setTitle(taskInfo.title);
        request.setDescription(taskInfo.description);
        
        // 设置保存位置
        FileUtil.checkFilePath(taskInfo.savePath); // 确保文件路径，否则系统下载器会失败
        Uri dest = Uri.fromFile(new File(taskInfo.savePath));
        request.setDestinationUri(dest);
        //request.setDestinationInExternalPublicDir("/download/", "test.txt");
        
        // 启动下载
        long taskID = mDownloadManager.enqueue(request);
        
        // 插入列表
        mTaskList.put(taskID, taskInfo);
        
        return taskID;
    } 
    
    
    /**
     * 扩展任务状态：代表任务不存在
     */
    public static final int STATUS_NO_EXIST = -999;

    
    /**
     * 查询任务状态
     * return 任务的状态，任务不存在返回STATUS_NO_EXIST
     */
    public int getTaskStatus(long taskID) {
        
        int status = STATUS_NO_EXIST;
        
        DownloadManager.Query query = new DownloadManager.Query();   
        query.setFilterById(taskID);   
        Cursor c = mDownloadManager.query(query);   
        if(c.moveToFirst()) {   
            status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch(status) {   
            case DownloadManager.STATUS_PAUSED:
                LogUtil.log("getTaskStatus : " + taskID + ", STATUS_PAUSED");
                break;
            case DownloadManager.STATUS_PENDING:
                LogUtil.log("getTaskStatus : " + taskID + ", STATUS_PENDING");
                break;
            case DownloadManager.STATUS_RUNNING:
                LogUtil.log("getTaskStatus : " + taskID + ", STATUS_RUNNING");
                break;   
            case DownloadManager.STATUS_SUCCESSFUL :
                LogUtil.log("getTaskStatus : " + taskID + ", STATUS_SUCCESSFUL");
                break;   
            case DownloadManager.STATUS_FAILED:
                LogUtil.log("getTaskStatus : " + taskID + ", STATUS_FAILED");
                break;
            }
            
        } else {
            status = STATUS_NO_EXIST;
            LogUtil.log("getTaskStatus : " + taskID + ", STATUS_NO_EXIST!");
        }
        return status;
    }
    
    
    /**
     * 依据URL，查询任务状态
     * return 任务的状态，任务不存在返回STATUS_NO_EXIST
     */
    public int getTaskStatusByUrl(String url) {
        
        long targetTaskID = -1;
        
        // 遍历任务列表，找出对应的任务
        Iterator<Entry<Long, TaskInfo>> iter = mTaskList.entrySet().iterator();
        while (iter.hasNext()){
            Entry<Long, TaskInfo> entry = iter.next();
            long taskID       = entry.getKey();
            TaskInfo taskInfo = entry.getValue();
            if (taskInfo.url.equalsIgnoreCase(url)) {
                targetTaskID = taskID;
                break;
            }
        }
        
        // 没有找到任务则直接返回
        if (targetTaskID == -1) {
            LogUtil.log("getTaskStatusByUrl, find no task : " + url);
            return STATUS_NO_EXIST;
        } 
        // 有任务则查询相应的状态
        else {
            int status = getTaskStatus(targetTaskID);
            LogUtil.log("getTaskStatusByUrl, find task : " + url + ", status = " + status);
            return status;
        }
        
    }
    
    // delete：暂时不使用DownloadManager.COLUMN_LOCAL_FILENAME需要API11（2.4）
    /**
     * 查询任务实际的保存路径，没有则返回null
     * 说明：下载器遇到本地重名的文件，会自动改名，所以下载完成之后，要重新获取实际的下载路径
     */
    /*
    public String getTaskActualSavePath(long taskID) {
        
        String path = null;
        DownloadManager.Query query = new DownloadManager.Query();   
        query.setFilterById(taskID);   
        Cursor c = mDownloadManager.query(query);   
        if(c.moveToFirst()) {   
            path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));      
        }
        return path;
    }
    */
    
    
    
    /**
     * 下载监听完成
     * PS：目前系统只支持监听下载完成的状态
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {   
 
        @Override
        public void onReceive(Context context, Intent intent) {
            
            final long taskID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            int status = getTaskStatus(taskID);
            
            // 下载成功，执行回调
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                
                final TaskInfo taskInfo = mTaskList.get(taskID);
                if ((taskInfo != null) && (taskInfo.completeRunnable != null)) {
                    mHandler.post(taskInfo.completeRunnable);
                }
            }
            
        }
    };
    
    
    /**
     * 注册监听下载
     */
    private void registerReceiver() {
        
        // 监听下载完成
        // PS：目前系统不能监听失败、暂停的情况
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.registerReceiver(mReceiver, filter);
    }

    
    /**
     * 反注册监听下载
     */
    private void unregisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

}
