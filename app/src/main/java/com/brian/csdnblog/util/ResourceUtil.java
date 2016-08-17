package com.brian.csdnblog.util;

import android.graphics.drawable.Drawable;

import com.brian.csdnblog.Env;


/**
 * 获取系统资源的工具类
 * @author ls
 *
 */
public class ResourceUtil {
    
    /**
     * 根据资源ID返回字符串
     * 
     * @param resourceID
     * @return
     */
    public static final String getString(int resid){
        return Env.getContext().getString(resid);
    }
    
    /**
     * 获取颜色
     */
    public static final int getColor(int resid){
        return Env.getContext().getResources().getColor(resid);
    }   
    
    /**
     * 获取图片
     */
    public static final Drawable getDrawable(int resid){
        return Env.getContext().getResources().getDrawable(resid);
    }
    
    /**
     * 获取dimems大小（单位为像素pix）
     * @param resid
     * @return
     */
    public static final int getDimens(int resid){
        return (int)Env.getContext().getResources().getDimension(resid);
    }
    
    /**
     * 根据资源id返回字符串数组
     * @param resid
     * @return
     */
    public static final String[] getStringArray(int resid){
        return Env.getContext().getResources().getStringArray(resid);
    }
    
    /**
     * 获取资源中的整数数据
     * @param resid
     * @return
     */
    public static final int getInteger(int resid){
        return Env.getContext().getResources().getInteger(resid);
    }
    
    /**
     * 根据资源名获取资源id
     * @param name 资源名
     * @return
     */
    public static final int getDrawableResId(String name){
    	return Env.getContext().getResources().getIdentifier(name, "drawable",Env.getContext().getPackageName());
    }
    
}
