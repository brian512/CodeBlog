package com.brian.common.view;

import android.content.Context;
import android.widget.Toast;

import com.brian.csdnblog.Env;

public class ToastUtil extends Toast {

    public ToastUtil(Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();
    }
    
    public static void showToast(CharSequence text) {
        Toast.makeText(Env.getContext(), text, Toast.LENGTH_SHORT).show();
    }
    
    public static void showToast(int resID) {
        Toast.makeText(Env.getContext(), resID, Toast.LENGTH_SHORT).show();
    }
}
