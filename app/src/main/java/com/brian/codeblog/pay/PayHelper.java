package com.brian.codeblog.pay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import com.brian.codeblog.Config;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.common.tools.Env;
import com.brian.common.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import c.b.BP;
import c.b.PListener;

/**
 * Bmob支付封装
 * Created by huamm on 2017/4/18 0018.
 */

public class PayHelper {

    private static final String APPLICATION_ID = "2227a432dc72d00d24d59c1b6301ba82";

    private static final int PLUGIN_VERSION = 7;
    private static final String PLUGIN_NAME = "bp.db";

    private static String sOrderId;

    public static void initPay() {
        BP.init(APPLICATION_ID);
    }


    public static void pay(String title) {
        pay(title, "", Config.getPaybackPrice(), null);
    }

    public static void pay(String title, IPayListener listener) {
        pay(title, "", Config.getPaybackPrice(), listener);
    }

    /**
     * 调用支付接口
     * @param title
     * @param describtion
     * @param money 支付金额
     * @param listener 支付回调
     */
    public static void pay(String title, String describtion, final double money, final IPayListener listener) {
        boolean isWeixin = checkWeixin();
        BP.pay(title, describtion, money, !isWeixin, new PListener() {
            @Override
            public void orderId(String s) {
                LogUtil.log("orderId=" + s);
                sOrderId = s;
            }

            @Override
            public void succeed() {
                LogUtil.log("orderId=" + sOrderId);
                CommonPreference.getInstance().addPayCount((int)(money/0.1));
                if (listener != null) {
                    listener.onResult(true);
                }
            }

            @Override
            public void fail(int i, String s) {
                LogUtil.log("orderId=" + s);
                if (listener != null) {
                    listener.onResult(false);
                }
            }

            @Override
            public void unknow() {
                LogUtil.log("orderId=" + sOrderId);
                if (listener != null) {
                    listener.onResult(false);
                }
            }
        });
    }

    public static boolean shouldNotifyPay() {
        CommonPreference preference = CommonPreference.getInstance();
        if (preference.getPayCount() > 0) {
            return false;
        }
        int oneDayTime = 24 * 3600_1000;
        if ((preference.getLastPayNotifyTime() / oneDayTime) == (System.currentTimeMillis() / oneDayTime)) {
            return false;
        }
        preference.updatePayNotifyTime();
        return true;
    }

    private static boolean checkWeixin() {
        Context context = Env.getContext();
        if (!checkPackageInstalled(context, "com.tencent.mm")) {
            return false;
        } else {
            int pluginVersion = BP.getPluginVersion(context);
            if (pluginVersion < PLUGIN_VERSION) {
//                installBmobPayPlugin(context, "bp.db");
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 安装assets里的apk文件
     */
    public static void installBmobPayPlugin(Context context) {
        try {
            InputStream is = context.getAssets().open(PLUGIN_NAME);
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + PLUGIN_NAME + ".apk");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + file),
                    "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查某包名应用是否已经安装
     *
     * @param packageName 包名
     */
    private static boolean checkPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    public static void jumpZhifubao(Context context) {
        try {
            Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/FKX09796GKDI32H6B1WOF5");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            LogUtil.printError(e);
        }
    }

    public interface IPayListener {
        void onResult(boolean isOK);
    }
}
