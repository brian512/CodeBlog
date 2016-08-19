
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.brian.common.view.TitleBar;
import com.brian.common.view.ToastUtil;
import com.brian.csdnblog.Config;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.DataFetcher;
import com.brian.csdnblog.manager.DataFetcher.OnFetchDataListener;
import com.brian.csdnblog.manager.DataFetcher.Result;
import com.brian.csdnblog.manager.FavoBlogManager;
import com.brian.csdnblog.manager.HistoryBlogManager;
import com.brian.csdnblog.manager.SettingPreference;
import com.brian.csdnblog.manager.ShareManager;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.SearchResult;
import com.brian.csdnblog.parser.BlogHtmlParserFactory;
import com.brian.csdnblog.parser.IBlogHtmlParser;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.NetStatusUtil;
import com.brian.csdnblog.util.WeakRefHandler;
import com.qhad.ads.sdk.adcore.Qhad;
import com.qhad.ads.sdk.interfaces.IQhInterstitialAd;
import com.tencent.connect.share.QQShare;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.umeng.analytics.MobclickAgent;

import java.util.Stack;

public class BlogContentActivity extends BaseActivity implements OnFetchDataListener<Result<String>> {

    private static final String TAG = BlogContentActivity.class.getSimpleName();

    private TitleBar mTitleBar;
    
    private WebView mWebView = null;

    private ImageView mBtnFavo;
    
    private ProgressBar mProgressBar; // 进度条

    private ImageView mReLoadImageView; // 重新加载的图片
    
    private FrameLayout mAdLayout; // 广告
    private IQhInterstitialAd mAd;

    private IBlogHtmlParser mBlogParser = null;
    
    /**
     * 存放已打开过的链接
     */
    private Stack<String> mBlogStack = null;
    
    /**
     * 当前页面的链接，初始与mBundleBlogURL相同，后续可能会在本页面跳转到其他链接
     */
    private String mCurrentUrl = null;
    
    private BlogInfo mBlogInfo;
    
    public static final String BUNDLE_EXTRAS_BLOGINFO = "bloginfo";
    
    public static void startActvity(Activity activity, SearchResult searchResult) {
        BlogInfo blogInfo = new BlogInfo();
        blogInfo.link = searchResult.link;
        blogInfo.title = searchResult.title;
        blogInfo.description = searchResult.searchDetail;
        blogInfo.msg = searchResult.authorTime;
        startActvity(activity, blogInfo);
    }
    
    public static void startActvity(Activity activity, BlogInfo blogInfo) {
        Intent intent = new Intent();
        intent.putExtra(BUNDLE_EXTRAS_BLOGINFO, blogInfo);
        intent.setClass(activity, BlogContentActivity.class);
        activity.startActivity(intent);
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SettingPreference.getIsVertical(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_webview);

        initUI();// 初始化界面
        initListener();
        initAd();

        initBlogInfo();

        if (mBlogInfo == null) {
            finish();
            return;
        }
        mTitleBar.setTitle(mBlogInfo.title);

        if (FavoBlogManager.getInstance().isFavo(mCurrentUrl)) {
            mBtnFavo.setSelected(true);
        } else {
            mBtnFavo.setSelected(false);
        }

        mBlogStack = new Stack<>();
        // 开始请求数据
        if (TypeManager.getWebType(mBlogInfo.type) == TypeManager.TYPE_WEB_JCC) {
            DataFetcher.getInstance().fetchString(mCurrentUrl, "GB2312", this);
        } else {
            DataFetcher.getInstance().fetchString(mCurrentUrl, this);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        
        HistoryBlogManager.getInstance().addBlog(mBlogInfo);
    }

    private void initBlogInfo() {
        try {
            mBlogInfo = (BlogInfo) getIntent().getExtras().getSerializable(BUNDLE_EXTRAS_BLOGINFO);
        } catch (Exception e) {
            ToastUtil.showToast("oops，打开出错。。。");
            return;
        }
        if (mBlogInfo == null || TextUtils.isEmpty(mBlogInfo.link)) {
            return;
        }

        LogUtil.i(TAG, "currenturl:" + mBlogInfo.link);

        mBlogParser = BlogHtmlParserFactory.getBlogParser(mBlogInfo.type);
        UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_BLOG_COUNT, TypeManager.getBlogName(mBlogInfo.type));
        // 处理一下链接，可能需要补全域名
        mCurrentUrl = mBlogParser.getBlogContentUrl(mBlogInfo.link);
        mBlogInfo.link = mCurrentUrl;
    }

    private void initAd() {
        mAdLayout = (FrameLayout) findViewById(R.id.ad_group);
        String adSpaceid = Config.getAdSplashKey();
        if (!TextUtils.isEmpty(adSpaceid)) {
            mAd = Qhad.showInterstitial(this, adSpaceid, false);
        }
        showAd(true);
    }
    
    private void showAd(boolean isShow) {
        if (mAd != null && SettingPreference.getIsShowAd(Env.getContext())) {
            if (isShow) {
                mAdLayout.setVisibility(View.VISIBLE);
                mAd.showAds(this);
            } else {
                mAdLayout.setVisibility(View.GONE);
                mAd.closeAds();
            }
        }
    }
    

    private void initUI() {

        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setRightImageResource(R.drawable.ic_share);
        mBtnFavo = (ImageView) findViewById(R.id.btn_favo);
        mProgressBar        = (ProgressBar)     findViewById(R.id.blogContentPro);

        // 点击图片重新加载
        mReLoadImageView    = (ImageView)       findViewById(R.id.reLoadImage);

        mWebView            = (WebView)         findViewById(R.id.article_content);

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setAppCacheEnabled(true);// 设置启动缓存
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        if(Build.VERSION.SDK_INT >= 19) {
            mWebView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            mWebView.getSettings().setLoadsImagesAutomatically(false);
        }

        mWebView.getSettings().setBlockNetworkImage(true);// 拦截图片的加载，网页加载完成后再去除拦截

        // mWebView.getSettings().setDisplayZoomControls(true);// 设置显示缩放按钮
        // mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true); // 支持缩放

        // 方法一：
        mWebView.getSettings().setUseWideViewPort(true);//让webview读取网页设置的viewport，pc版网页
//        mWebView.getSettings().setLoadWithOverviewMode(true);

        // 方法二：
        mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);// 适应内容大小
//        mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);//适应屏幕，内容将自动缩放
    }

    private void initListener() {
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.setRightListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onClickShare();
                // 友盟统计分享事件
                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_SHARE, "article");
            }
        });
        mReLoadImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mReLoadImageView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                showAd(true);
                
                DataFetcher.getInstance().fetchString(mCurrentUrl, BlogContentActivity.this);
            }
        });
        mBtnFavo.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                boolean hasFavoed = mBtnFavo.isSelected();
                if (!hasFavoed) {
                    FavoBlogManager.getInstance().addBlog(mBlogInfo);
                } else {
                    FavoBlogManager.getInstance().removeBlog(mCurrentUrl);
                }
                mBtnFavo.setSelected(!hasFavoed);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mProgressBar.getVisibility() == View.VISIBLE) {
                return true;
            }

            if (mReLoadImageView.getVisibility() == View.VISIBLE) {
                mReLoadImageView.setVisibility(View.GONE);
            } else {
                if (mBlogStack.size() > 1) {
                    mBlogStack.pop();
                } else {
                    finish();
                    return true;
                }
            }
            if (mBlogStack.size() >= 1) {
                String html = mBlogStack.peek();
                Message msg = mHandler.obtainMessage(MSG_UPDATE);
                msg.obj = html;
                mHandler.sendMessage(msg);
                return true;
            } else {
                this.finish();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 继承WebViewClient
     */
    class MyWebViewClient extends WebViewClient {

        // 重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            LogUtil.w(TAG, "url=" + url);
            
            String blogUrl = mBlogParser.getBlogContentUrl(url);
            if (!TextUtils.isEmpty(blogUrl)) {
                
                if (blogUrl.equalsIgnoreCase(mCurrentUrl)) {
                    return true;
                }
                
                mProgressBar.setVisibility(View.VISIBLE);
                showAd(true);
                
                mCurrentUrl = blogUrl;
                DataFetcher.getInstance().fetchString(blogUrl, BlogContentActivity.this);
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            
            boolean isBlockImage = SettingPreference.getIsShowPicOnInWifi(BlogContentActivity.this);
            
            boolean shouldLoadImg = NetStatusUtil.isWifiNet(Env.getContext()) || !isBlockImage;
            mWebView.getSettings().setBlockNetworkImage(!shouldLoadImg);
            
            mWebView.getSettings().setLoadsImagesAutomatically(true);
            super.onPageFinished(view, url);
            LogUtil.i(TAG, "onPageFinished");
        }
    }

    /**
     * 分享博文给QQ好友
     */
    private void onClickShare() {
        
        if (TextUtils.isEmpty(mBlogInfo.title)) {
            mBlogInfo.title = getString(R.string.app_name);
        }
        if (TextUtils.isEmpty(mBlogInfo.description)) {
            mBlogInfo.description = getString(R.string.app_description);
        }
        
        final Bundle bundle = new Bundle();
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, mBlogInfo.title);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, mBlogInfo.description);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mCurrentUrl);
        ShareManager.getInstance().shareBlogToQQ(BlogContentActivity.this, bundle);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ShareManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this); // 统计时长
        MobclickAgent.onPageStart(this.getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Qhad.activityDestroy(this);
        
        mHandler.removeCallbacksAndMessages(null);
    }
    
    private void showErrorPage() {
        UsageStatsManager.sendUsageData(UsageStatsManager.EXP_EMPTY_BLOG, TypeManager.getBlogName(mBlogInfo.type));
        
        mWebView.setVisibility(View.INVISIBLE);
        mBtnFavo.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mReLoadImageView.setVisibility(View.VISIBLE);
    }
    
    private String content = "";
    
    private static final int MSG_UPDATE = 1;
    
    private Handler.Callback mCallback = new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE:
                    mReLoadImageView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    
                    mBtnFavo.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.VISIBLE);
                    content = (String) msg.obj;
                    mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), content,
                            "text/html", "utf-8", null);
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onFetchFinished(final Result<String> response) {
        showAd(false);// 隐藏广告
//        LogUtil.i("response=" + response.data);
        if (TextUtils.isEmpty(response.data)) {
            showErrorPage();
        } else {
            ThreadManager.getPoolProxy().execute(new Runnable() {
                @Override
                public void run() {
//                    FileUtil.writeFile("/sdcard/blog", response.data);
                    String contentHtml = mBlogParser.getBlogContent(mBlogInfo.type, response.data);
//                    LogUtil.d("contentHtml=" + contentHtml);
                    mBlogStack.push(contentHtml);
                    
                    Message msg = mHandler.obtainMessage(MSG_UPDATE);
                    msg.obj = contentHtml;
                    mHandler.sendMessage(msg);
                }
            });
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        switch (getResources().getConfiguration().orientation) {
//            case Configuration.ORIENTATION_LANDSCAPE:
//                
//                break;
//            case Configuration.ORIENTATION_PORTRAIT:
//                
//                break;
//        }
        mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), content,
                "text/html", "utf-8", null);
    }
}
