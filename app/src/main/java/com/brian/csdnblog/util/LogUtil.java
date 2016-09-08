package com.brian.csdnblog.util;

import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author brian @date 2015年7月24日
 */
public class LogUtil {
    private static boolean mIsDebugMode = false;// 获取堆栈信息会影响性能，发布应用时记得关闭DebugMode

    private static boolean LOGV = true;
    private static boolean LOGF = true;
    private static boolean LOGD = true;
    private static boolean LOGI = true;
    private static boolean LOGW = true;
    private static boolean LOGE = true;

    public static void v(String tag, String mess) {
        if (LOGV) { Log.v(tag, buildMessageSafe(mess)); }
    }
    public static void d(String tag, String mess) {
        if (LOGD) { Log.d(tag, buildMessageSafe(mess)); }
    }
    public static void i(String tag, String mess) {
        if (LOGI) { Log.i(tag, buildMessageSafe(mess)); }
    }
    public static void w(String tag, String mess) {
        if (LOGW) { Log.w(tag, buildMessageSafe(mess)); }
    }
    public static void e(String tag, String mess) {
        if (LOGE) { Log.e(tag, buildMessageSafe(mess)); }
    }
    public static void log(String mess) {
        if (LOGD) { Log.i(getTag(), buildMessageSafe(mess)); }
    }

    /**
     * write log to file /sdcard/GameCenter.log
     */
    public static void f(String tag, String mess) {
        if (LOGF) {
            Log.v(tag, "write log to file /sdcard/GameCenter.log");
            writeFile("/sdcard/CSDN.log", tag, mess);
        }
    }

    /**
     * write log to file /sdcard/GameCenter.log
     */
    public static void f(String mess) {
        f(getTag(), mess);
    }

    /**
     * use the classname as tag
     */
    public static void v(String mess) {
        if (LOGV) { Log.v(getTag(), buildMessageSafe(mess)); }
    }
    public static void d(String mess) {
        if (LOGD) { Log.d(getTag(), buildMessageSafe(mess)); }
    }
    public static void i(String mess) {
        if (LOGI) { Log.i(getTag(), buildMessageSafe(mess)); }
    }
    public static void w(String mess) {
        if (LOGW) { Log.w(getTag(), buildMessageSafe(mess)); }
    }
    public static void e(String mess) {
        if (LOGE) { Log.e(getTag(), buildMessageSafe(mess)); }
    }

    private static final String CLASS_METHOD_LINE_FORMAT = "%s.%s() (%s :%d)";

    public static void trace(String tag) {
        if (mIsDebugMode) {
            StackTraceElement traceElement = Thread.currentThread()
                    .getStackTrace()[3];// 从堆栈信息中获取当前被调用的方法信息
            String logText = String.format(Locale.CHINA, CLASS_METHOD_LINE_FORMAT,
                    traceElement.getClassName(), traceElement.getMethodName(),
                    traceElement.getFileName(), traceElement.getLineNumber());
            LogUtil.d(tag, logText);
        }
    }

    private static String buildMessageSafe(String msg) {
        try {
            return LOGV ? buildMessage(msg) : msg;
        } catch (Exception e) {
        }
        return msg;
    }
    private static String buildMessage(String msg) {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String caller = "";
        for (int i = 3; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                caller = "(L" + trace[i].getLineNumber() + ") " + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread()
                .getId(), caller, msg==null?"":msg);
    }

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();
        String callingClass = "";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogUtil.class)) {
                callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                break;
            }
        }
        return callingClass;
    }

    private static boolean writeFile(String filePath, String tag, String content) {
        FileWriter fileWriter = null;
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd__HH.mm.ss", Locale.CHINA);
            String time = formatter.format(new Date());
            fileWriter = new FileWriter(filePath, true);
            fileWriter.write(String.format("\n<<<<<<<<<<<<<< %s <<<<<<<<<<<\n",
                    time));
            fileWriter.write(tag + "\n");
            fileWriter.write(content + "\n");
            fileWriter.write(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
            fileWriter.flush();
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
