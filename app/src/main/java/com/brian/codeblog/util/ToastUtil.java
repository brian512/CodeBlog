package com.brian.codeblog.util;

import android.widget.Toast;

import com.brian.codeblog.Env;

/**
 * Created by huamm on 2016/9/12 0012.
 */
public class ToastUtil {

    private static Toast sToast;

    public static void showMsgS(CharSequence msg) {
        if (sToast == null) {
            sToast = Toast.makeText(Env.getContext(), "", Toast.LENGTH_SHORT);
        }
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.show();
    }

    public static void showMsgL(CharSequence msg) {
        if (sToast == null) {
            sToast = Toast.makeText(Env.getContext(), "", Toast.LENGTH_LONG);
        }
        sToast.setText(msg);
        sToast.setDuration(Toast.LENGTH_LONG);
        sToast.show();
    }

    public static void showMsgS(int resID) {
        if (sToast == null) {
            sToast = Toast.makeText(Env.getContext(), "", Toast.LENGTH_SHORT);
        }
        sToast.setText(resID);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.show();
    }

    public static void showMsgL(int resID) {
        if (sToast == null) {
            sToast = Toast.makeText(Env.getContext(), "", Toast.LENGTH_LONG);
        }
        sToast.setText(resID);
        sToast.setDuration(Toast.LENGTH_LONG);
        sToast.show();
    }
}
