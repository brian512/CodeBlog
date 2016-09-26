
package com.brian.csdnblog.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.brian.csdnblog.activity.BlogListFrag;
import com.brian.csdnblog.manager.TypeManager;

/**
 * 主页面Tab适配器
 */
public class MainTabAdapter extends FragmentPagerAdapter {
    
    private BlogListFrag[] mFragments = null;

    public MainTabAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new BlogListFrag[mTabTitles.length];
    }
    
    private String[] mTabTitles = new String[]{
            "InfoQ", "ITEye", "CSDN", "泡网", "OsChina"
    };
    // 获取项
    @Override
    public BlogListFrag getItem(int position) {
        BlogListFrag fragment = mFragments[position];
        
        if (fragment != null) {
            return fragment;
        }
        int type = TypeManager.initType(TypeManager.TYPE_WEB_CSDN);
        fragment = new BlogListFrag();
        
        switch (position) {
            case 0:
                type = TypeManager.initType(TypeManager.TYPE_WEB_INFOQ);
                fragment.setPageName("Page_InfoQ");
                break;
            case 1:
                type = TypeManager.initType(TypeManager.TYPE_WEB_ITEYE);
                fragment.setPageName("Page_ITEye");
                break;
            case 2:
                type = TypeManager.initType(TypeManager.TYPE_WEB_CSDN);
                fragment.setPageName("Page_CSDN");
                break;
            case 3:
                type = TypeManager.initType(TypeManager.TYPE_WEB_JCC);
                fragment.setPageName("Page_JCC");
                break;
            case 4:
                type = TypeManager.initType(TypeManager.TYPE_WEB_OSCHINA);
                fragment.setPageName("Page_OsChina");
                break;
        }
        
        fragment.setType(type);
        mFragments[position] = fragment;
        
        return fragment;
    }

    @Override
    public String getPageTitle(int position) {
        // 返回页面标题
        return mTabTitles[position % mTabTitles.length];
    }

    @Override
    public int getCount() {
        // 页面个数
        return mTabTitles.length;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 避免视图被销毁
//        super.destroyItem(container, position, object);
    }
}
