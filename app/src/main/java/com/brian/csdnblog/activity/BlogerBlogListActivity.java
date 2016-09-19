package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.BlogerManager;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.model.Bloger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 博主主页文章列表
 * @author huamm
 */
public class BlogerBlogListActivity extends BaseActivity {

    public static final String EXTRA_KEY_TYPE = "extra_key_type";
    public static final String EXTRA_KEY_BLOGER = "extra_key_bloger";

    @BindView(R.id.title_bar) TitleBar mTitleBar;

    private BlogListFrag mListFrag;

    private int mType = TypeManager.initType(TypeManager.TYPE_WEB_CSDN);

    public static void startActivity(Activity activity, int type, Bloger bloger) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_KEY_TYPE, type);
        intent.putExtra(EXTRA_KEY_BLOGER, bloger);
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
        getIntent().getExtras().putInt(EXTRA_KEY_TYPE, mType);

        Bloger bloger = (Bloger) getIntent().getSerializableExtra(EXTRA_KEY_BLOGER);

        if (bloger == null) {
            finish();
            return;
        }
        if (!TextUtils.isEmpty(bloger.nickName)) {
            mTitleBar.setTitle(bloger.nickName + "的博客");
        }
        BlogerManager.getsInstance().setCurrBloger(bloger);
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitleBar.setRightImageVisible(View.INVISIBLE);

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        mListFrag = new BlogListFrag();
        mListFrag.setPageName("BlogerBlog");
        mListFrag.setType(mType);
        mListFrag.setBloger(bloger);
        trans.add(R.id.list, mListFrag, null);
        trans.commit();
    }
}
