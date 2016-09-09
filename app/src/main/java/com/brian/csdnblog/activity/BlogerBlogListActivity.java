package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.TypeManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 博主主页文章列表
 * @author huamm
 */
public class BlogerBlogListActivity extends BaseActivity {

    private static final String EXTRA_KEY_TYPE = "extra_key_type";
    private static final String EXTRA_KEY_BLOGERID = "extra_key_blogerid";

    @BindView(R.id.title_bar) TitleBar mTitleBar;

    private BlogListFrag mListFrag;

    private String mCurrBlogID;

    private int mType = TypeManager.initType(TypeManager.TYPE_WEB_CSDN);

    public static void startActivity(Activity activity, int type, String blogerID) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_KEY_TYPE, type);
        intent.putExtra(EXTRA_KEY_BLOGERID, blogerID);
        intent.setClass(activity, BlogerBlogListActivity.class);
        activity.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloglist);
        ButterKnife.bind(this);
        
        mType = getIntent().getExtras().getInt(EXTRA_KEY_TYPE);
        mType = TypeManager.updateCateType(mType, TypeManager.TYPE_CAT_BLOGER);

        mCurrBlogID = getIntent().getExtras().getString(EXTRA_KEY_BLOGERID);

        mTitleBar.setTitle(mCurrBlogID + "的博客");
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
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEY_BLOGERID, mCurrBlogID);

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        mListFrag = new BlogListFrag();
        mListFrag.setPageName("BlogerBlog");
        mListFrag.setType(mType);
        mListFrag.setArguments(bundle);
        trans.add(R.id.list, mListFrag, null);
        trans.commit();
    }
}
