
package com.brian.csdnblog.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;



/**
 * 博客实体类
 */
public class BlogInfo extends DataSupport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int id; // id
    public String title; // 标题
    public String link; // 文章链接
    public String dateStamp; // 博客发布时间
    public String description;// 文章摘要
    public String content; // 文章内容
    public String msg; // 消息
    public int articleType; // 博客类型，原创，翻译，转载
    public int type; // 博客类型，在TypeManager中定义
}
