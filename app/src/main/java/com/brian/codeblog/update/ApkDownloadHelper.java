package com.brian.codeblog.update;

import android.content.Intent;
import android.net.Uri;

import com.brian.common.tools.Env;
import com.brian.codeblog.model.UpdateInfo;
import com.brian.common.utils.FileUtil;
import com.brian.common.utils.ResourceUtil;
import com.brian.csdnblog.R;

import java.io.File;

/**
 * Created by Brian on 2016/7/10 0010.
 */
public class ApkDownloadHelper {

    private long mDownloadID;

    public void startDownload(UpdateInfo updateInfo) {

        String appName  = ResourceUtil.getString(R.string.app_name);
        String savePath = getSavePath(appName, updateInfo.versionName); // 坑：路径要保证不冲突，否则系统下载器会偷偷改掉保存文件名!!!

        // 创建下载任务
        final SystemDownloaderHelper.TaskInfo taskInfo = new SystemDownloaderHelper.TaskInfo();
        taskInfo.url = updateInfo.apkUrl;
        taskInfo.showNotification = true;
        taskInfo.title = appName + "升级包";
        taskInfo.description = ""; // ?
        taskInfo.savePath = savePath;
        taskInfo.completeRunnable = new Runnable() {
            @Override
            public void run() {
                // 下载完成，安装应用
                installApk(taskInfo.savePath);
            }
        };

        try {
            SystemDownloaderHelper.getInstance().createTask(taskInfo);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取保存文件路径
     */
    private static String getSavePath(String appName, String version) {

        // 文件名：加入随机数，避免与之前的下载文件重名
        String filename = String.format("%s_%s.apk", appName, version, System.currentTimeMillis());

        // PS：必须使用 getContext().getExternalCacheDir()，否则系统下载器不支持
        String dir = Env.getContext().getExternalCacheDir() + "/update";
        String path = dir + "/" +  filename;

        // 保证目录存在，否则系统下载器会失败
        FileUtil.ensureFolderExists(dir);

        return path;
    }

    /**
     * 安装APK应用
//     * @param context 原则上必须是Activity是context，现在使用FLAG_ACTIVITY_NEW_TASK，就无所谓了
     * @param apkPath
     * @return
     */
    private static void installApk(final String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // PS：加这行，保证程序不管是否在前台，安装应用都能弹出来
        Env.getContext().startActivity(intent);
    }
}
