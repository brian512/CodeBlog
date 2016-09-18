package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.BlogManager;
import com.brian.csdnblog.manager.TypeManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 收藏、历史、新闻等页面
 * @author huamm
 */
public class BlogListActivity extends BaseActivity {

    private static final String EXTRA_KEY_TYPE = "extra_key_type";
    
    public static final int TYPE_FAVO = 0;
    public static final int TYPE_HISTORY = 1;
    public static final int TYPE_NEWS = 2;

    @BindView(R.id.title_bar) TitleBar mTitleBar;

    private BlogListFrag mListFrag;

    private int mType = TYPE_FAVO;

    public static void startActivity(Activity activity, int type) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_KEY_TYPE, type);
        intent.setClass(activity, BlogListActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloglist);
        ButterKnife.bind(this);
        
        mType = getIntent().getExtras().getInt(EXTRA_KEY_TYPE);
        
        mTitleBar.setRightImageResource(R.drawable.ic_delete);
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // 删除按钮监听
        mTitleBar.setRightListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType == TYPE_FAVO) {
                    BlogManager.getInstance().clearByType(TYPE_FAVO);
                } else if (mType == TYPE_HISTORY) {
                    BlogManager.getInstance().clear();
                }
                mListFrag.clearList();
            }
        });
        
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        mListFrag = new BlogListFrag();
        int type = TypeManager.initType(TypeManager.TYPE_WEB_FAVO);
        if (mType == TYPE_FAVO) {
            mTitleBar.setTitle("博文收藏");
            mListFrag.setPageName("FavoList");
        } else if (mType == TYPE_HISTORY) {
            type = TypeManager.initType(TypeManager.TYPE_WEB_HISTORY);
            mTitleBar.setTitle("浏览记录");
            mListFrag.setPageName("HistoryList");
        } else if (mType == TYPE_NEWS) {
            type = TypeManager.initType(TypeManager.TYPE_WEB_OSNEWS);
            mTitleBar.setTitle("新闻");
            mListFrag.setPageName("NewsList");
            mTitleBar.setRightImageVisible(View.INVISIBLE);
        }
        mListFrag.setType(type);
        trans.add(R.id.list, mListFrag, null);
        trans.commit();
    }
}
