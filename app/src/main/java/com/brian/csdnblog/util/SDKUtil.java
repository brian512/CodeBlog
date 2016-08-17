package com.brian.csdnblog.util;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * 
 * 用来封装所有和版本有关的接口
 * @author lipeilong
 *
 */
public class SDKUtil {

    /**
     * PS:因为API 16以上不推荐使用setBackgroundDrawable ，所以强烈建议使用我们自己封装的方法来设置背景
     * @param view
     * @param background
     */
    @SuppressLint("NewApi")
    public static final void setBackground(View view , Drawable background){        
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }        
    }
    
    /**
     * PS:为了方便使用，添加一个直接设置图片的接口
     * @param view
     * @param bitmap
     */
    public static final void setBackgroundBitmap(View view , Bitmap bitmap){        
        if(view == null){
            return ;
        }
        
        setBackground(view, new BitmapDrawable(bitmap));
    }
    
    public static final void setAlpha(View view , float alpha){
        if(VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH){            
            view.setAlpha(alpha);
        }
    }
    
    /**
     * 获得textView的最大限制行数
     * 如果API不支持的话，则返回传入的默认值
     * 
     * @param textView
     * @param defaultNum
     * @return
     */
    @SuppressLint("NewApi")
    public static final int getMaxLine(TextView textView,int defaultNum){
    	// PS: 官方文档是api16以上的都支持这个方法
    	//     实际上厂商定制的ROM中不一定包含这个方法（根据友盟反馈的崩溃日志）
    	
    	try {
			Method method	= TextView.class.getMethod("getMaxLines");
			if(method == null){
				return defaultNum;
			}else{
				return textView.getMaxLines();
			}
		} catch (NoSuchMethodException e) {
		    e.printStackTrace();
			return defaultNum;
		}        
    }
    
    @SuppressLint("NewApi")
    public static final void removeRule(RelativeLayout.LayoutParams layoutParams,int rule){
        if (VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.removeRule(rule);
        } else {
            layoutParams.addRule(rule, 0);
        }
    }
}
