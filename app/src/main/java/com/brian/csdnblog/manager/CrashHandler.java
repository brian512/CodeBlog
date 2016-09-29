
package com.brian.csdnblog.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.util.LogUtil;
import com.umeng.analytics.MobclickAgent;

public class CrashHandler implements UncaughtExceptionHandler {
    
    private static final String TAG = CrashHandler.class.getSimpleName();
    
    
    private OnAppCrashListener mCrashListener = null;
    
    
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 保存错误日志
        saveCatchInfo2File(ex);

        ex.printStackTrace();
        // 友盟错误统计
        MobclickAgent.reportError(Env.getContext(), ex);

        if (mCrashListener != null) {
            mCrashListener.onAppCrash();
        }
    }

    /**
     * 保存错误信息到文件中
     * 
     * @return 返回文件名称
     */
    private void saveCatchInfo2File(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String sb = writer.toString();
        LogUtil.v(sb);//打印log
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            String time = formatter.format(new Date());
            String fileName = time + ".log";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String filePath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath()
                        + File.separator
                        + Env.getPackageName()
                        + File.separator + "crash" + File.separator;
                File dir = new File(filePath);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        // 创建目录失败: 一般是因为SD卡被拔出了
                        return;
                    }
                }
                LogUtil.i(TAG, "filePath + fileName:" + filePath + fileName);
                FileOutputStream fos = new FileOutputStream(filePath + fileName);
                fos.write(sb.getBytes());
                fos.close();
                // 文件保存完了之后,在应用下次启动的时候去检查错误日志,发现新的错误日志,就发送给开发者
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "an error occured while writing file...");
            e.printStackTrace();
        }
    }
    
    public void setOnCrashListener(OnAppCrashListener listener) {
        mCrashListener = listener;
    }
    
    public interface OnAppCrashListener {
        public void onAppCrash();
    }
}
