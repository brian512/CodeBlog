
package com.brian.codeblog.util;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupUtil {

    private static final String TAG = JsoupUtil.class.getSimpleName();

    // 链接样式文件，代码块高亮的处理
    private final static String CSS_CODE = 
            "<script type=\"text/javascript\" src=\"file:///android_asset/shCore.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/shBrushCpp.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/shBrushXml.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/shBrushJScript.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/shBrushJava.js\"></script>"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shThemeDefault.css\">"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore.css\">"
            + "<script type=\"text/javascript\">SyntaxHighlighter.all();</script>";

    private static final String CSS_IMAGE = "<script type='text/javascript'>" + 
            "window.onload = function(){\n" + 
            "var $img = document.getElementsByTagName('img');\n" + 
            "for(var p in  $img){\n" + 
               " $img[p].style.width = '100%';\n" + 
                "$img[p].style.height ='auto'\n" + 
            "}\n" + 
            "}" + 
            "</script>";
    
    public static final String CONTENT_HOLDER = "CONTENT_HOLDER";
    
    public static String sHtmlFormat = 
            "<html> \n"
            + "<head> \n"
            + "<style type=\"text/css\"> \n"
            + "body {font-size:15px;}\n"
            + "</style> \n"
            + CSS_IMAGE + CSS_CODE
            + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" />"
            + "\n</head>\n"
            + "<body>\n"
            + CONTENT_HOLDER
            + "\n</body>"
            + "\n</html>";

    public static void removeUselessTag(Element detail, List<RemoveItem> removeList) {
        detail.select("script").remove(); // 删除每个匹配元素的DOM。
        for (RemoveItem item : removeList) {
            if (item.type == RemoveItem.TYPE_CLASS) {
                if (null != detail.getElementsByClass(item.tag)) {
                    detail.getElementsByClass(item.tag).remove();
                }
            } else if (item.type == RemoveItem.TYPE_ID) {
                if (null != detail.getElementById(item.tag)) {
                    detail.getElementById(item.tag).remove();
                }
            }
        }
    }
    
    public static String removeScripts(String html) {
        // 获取文档内容
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("main_con");
        if (elements.size() <= 0) {
            elements = doc.getElementsByClass("main_center");
        }
        if (elements.size() <= 0) {
            elements = doc.getElementsByClass("main");
        }

        if (elements.size() > 0) {
            Element element = elements.get(0);
            element.select("script").remove();
            html = element.html();
        }

        return html;
    }

    /**
     * 半角转换为全角 全角---指一个字符占用两个标准字符位置。 半角---指一字符占用一个标准的字符位置。
     * 
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    static class RemoveItem {
        static final int TYPE_ID = 1;
        static final int TYPE_CLASS = 2;
        public RemoveItem(int type, String tag) {
            this.type = type;
            this.tag = tag;
        }
        int type;
        String tag;
    }
}
