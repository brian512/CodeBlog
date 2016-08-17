
package com.brian.csdnblog.util;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * 输入法先关工具类
 */
public class InputMethodUtil {

    public static final String TAG = InputMethodUtil.class.getSimpleName();

    private static boolean sLastVisiable = false;

    /**
     * 隐藏软键盘
     * 
     * @param context
     * @param view
     */
    public static void hiddenInput(Context context, View view) {
        if (context instanceof Context && view instanceof View) {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘 （PS：这里使用切换/显示的方式）
     * 
     * @param context
     */
    public static void showInput(Context context, View view) {
        if (context instanceof Context) {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * 切换 显示/隐藏 软键盘
     * 
     * @param context
     */
    public static void toggleInput(Context context) {
        if (context instanceof Context) {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 查询输入法面板是否已弹出
     * 
     * @param context
     * @return
     */
    public static boolean isInputActive(Context context) {
        if (context instanceof Context) {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            return inputMethodManager.isActive();
        }
        return false;
    }

    /**
     * 监听软键盘状态 注意： 1、需要监听键盘收起事件的Activity，不能声明
     * android:windowSoftInputMode="adjustNothing"， 否则会onGlobalLayout的时候
     * displayHight永远不会变化，导致无法触发回调
     * 2、因为只有当前在显示的Activity的decorView才会触发onGlobalLayout
     * ，所以在多个Activity同时绑定监听，也只会有当前的Activity 会触发回调
     * 
     * @param activity
     * @param listener
     */
    public static void addOnSoftKeyBoardVisibleListener(Activity activity, final OnSoftKeyBoardVisibleListener listener) {

        final View decorView = activity.getWindow().getDecorView();
        sLastVisiable = false;
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHight = rect.bottom - rect.top;
                int hight = decorView.getHeight();
                boolean visible = (double) displayHight / hight < 0.8;

                if (visible != sLastVisiable) {
                    listener.onSoftKeyBoardVisible(visible);
                }
                sLastVisiable = visible;
            }
        });
    }

    public static void showOnSoftKeyBoardHeight(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            Field rect1 = InputMethodManager.class.getField("mTmpCursorRect");
            Field rect2Field = InputMethodManager.class.getField("mCursorRect");

            rect1.setAccessible(true);
            Rect curRootView = (Rect) rect1.get(inputMethodManager);

            rect2Field.setAccessible(true);
            Rect rect2 = (Rect) rect2Field.get(inputMethodManager);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public interface OnSoftKeyBoardVisibleListener {
        void onSoftKeyBoardVisible(boolean visible);
    }

}
