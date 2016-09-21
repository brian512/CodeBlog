
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.brian.common.view.TitleBar;
import com.brian.csdnblog.Config;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.datacenter.preference.SettingPreference;
import com.brian.csdnblog.manager.BlogManager;
import com.brian.csdnblog.manager.DataFetcher;
import com.brian.csdnblog.manager.DataFetcher.OnFetchDataListener;
import com.brian.csdnblog.manager.DataFetcher.Result;
import com.brian.csdnblog.manager.DataManager;
import com.brian.csdnblog.manager.ShareManager;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.model.SearchResult;
import com.brian.csdnblog.parser.BlogHtmlParserFactory;
import com.brian.csdnblog.parser.IBlogHtmlParser;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.NetStatusUtil;
import com.brian.csdnblog.util.ToastUtil;
import com.brian.csdnblog.util.WeakRefHandler;
import com.qhad.ads.sdk.adcore.Qhad;
import com.qhad.ads.sdk.interfaces.IQhInterstitialAd;
import com.tencent.connect.share.QQShare;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BlogContentActivity extends BaseActivity implements OnFetchDataListener<Result<String>> {

    private static final String TAG = BlogContentActivity.class.getSimpleName();

    @BindView(R.id.title_bar) TitleBar mTitleBar;
    @BindView(R.id.article_content) WebView mWebView;
    @BindView(R.id.btn_favo) ImageView mBtnFavo;
    @BindView(R.id.blogContentPro) ProgressBar mProgressBar; // 进度条
    @BindView(R.id.reLoadImage) ImageView mReLoadImageView; // 重新加载的图片
    @BindView(R.id.ad_group) FrameLayout mAdLayout; // 广告

    private IQhInterstitialAd mAd;

    private IBlogHtmlParser mBlogParser = null;
    
    /**
     * 存放已打开过的链接
     */
    private Stack<String> mBlogStack = new Stack<>();
    
    /**
     * 当前页面的链接，初始与mBundleBlogURL相同，后续可能会在本页面跳转到其他链接
     */
    private String mCurrentUrl = null;
    private String mCurrentTitle = null;

    private BlogInfo mBlogInfo;

    public static final String BUNDLE_EXTRAS_BLOGINFO = "bloginfo";
    
    public static void startActvity(Activity activity, SearchResult searchResult) {
        BlogInfo blogInfo = new BlogInfo();
        blogInfo.link = searchResult.link;
        blogInfo.title = searchResult.title;
        blogInfo.summary = searchResult.searchDetail;
        blogInfo.extraMsg = searchResult.authorTime;
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
//        if (SettingPreference.getIsVertical(this)) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_webview);
        ButterKnife.bind(this);

        initUI();// 初始化界面
        initPopupMenu();
        initListener();
        initAd();

        initBlogInfo();

        if (mBlogInfo == null) {
            finish();
            return;
        }
        mTitleBar.setTitle(mBlogInfo.title);
        mCurrentTitle = mBlogInfo.title;

        // 开始请求数据
        if (TypeManager.getWebType(mBlogInfo.type) == TypeManager.TYPE_WEB_JCC) {
            BlogManager.getInstance().fetchBlogContent(mCurrentUrl, "GB2312", this);
        } else {
            BlogManager.getInstance().fetchBlogContent(mCurrentUrl, this);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        
        BlogManager.getInstance().saveBlog(mBlogInfo);
    }

    private void initBlogInfo() {
        try {
            mBlogInfo = (BlogInfo) getIntent().getExtras().getSerializable(BUNDLE_EXTRAS_BLOGINFO);
        } catch (Exception e) {
            ToastUtil.showMsgS("oops，打开出错。。。");
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
        toggleAdShow(true);
    }
    
    private void toggleAdShow(boolean isShow) {
        if (mAd != null && SettingPreference.getInstance().getAdsEnable()) {
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
        mTitleBar.setRightImageResource(R.drawable.ic_share);
        // 点击图片重新加载
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setAppCacheEnabled(true);// 设置启动缓存
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        final String cachePath = getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(cachePath);
        webSettings.setAppCacheMaxSize(5*1024*1024);
        webSettings.setDomStorageEnabled(true);

        if(Build.VERSION.SDK_INT >= 19) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }

        webSettings.setBlockNetworkImage(true);// 拦截图片的加载，网页加载完成后再去除拦截

        // webSettings.setDisplayZoomControls(true);// 设置显示缩放按钮
        // webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true); // 支持缩放

        // 方法一：
        webSettings.setUseWideViewPort(true);//让webview读取网页设置的viewport，pc版网页
//        webSettings.setLoadWithOverviewMode(true);

        // 方法二：
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);// 适应内容大小
//        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);//适应屏幕，内容将自动缩放
    }

    PopupMenu popupMenu;
    private void initPopupMenu() {
        popupMenu = new PopupMenu(this, mTitleBar.getRightButton());
        Menu menu = popupMenu.getMenu();
        // 通过代码添加菜单项
        menu.add(Menu.NONE, Menu.FIRST, 0, "分享");
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "收藏");
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "博主列表");

        // 监听事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case Menu.FIRST + 0:
                        onClickShare();
                        break;
                    case Menu.FIRST + 1:
                        boolean hasFavoed = mBtnFavo.isSelected();
                        BlogManager.getInstance().doFavo(mBlogInfo, !hasFavoed);
                        mBtnFavo.setSelected(!hasFavoed);
                        break;
                    case Menu.FIRST + 2:
                        if (!TextUtils.isEmpty(mBlogInfo.blogerID)) {
                            LogUtil.log(mBlogInfo.blogerJson);
                            Bloger bloger = Bloger.fromJson(mBlogInfo.blogerJson);
                            if (bloger != null) {
                                BlogerBlogListActivity.startActivity(BlogContentActivity.this, mBlogInfo.type, bloger);
                                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_BLOGER_ENTR, "bloglist");
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
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
//                onClickShare();
                popupMenu.show();
            }
        });
        mReLoadImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mReLoadImageView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                toggleAdShow(true);
                
                DataFetcher.getInstance().fetchString(mCurrentUrl, BlogContentActivity.this);
            }
        });
        mBtnFavo.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                boolean hasFavoed = mBtnFavo.isSelected();
                BlogManager.getInstance().doFavo(mBlogInfo, !hasFavoed);
                mBtnFavo.setSelected(!hasFavoed);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mReLoadImageView.getVisibility() == View.VISIBLE) {
                mReLoadImageView.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                return true;
            }
            if (mBlogStack.size() > 1) {
                mBlogStack.pop();//把当前的博客移除
                String html = mBlogStack.peek();
                Message msg = mHandler.obtainMessage(MSG_UPDATE);
                msg.obj = html;
                mHandler.removeMessages(MSG_UPDATE);
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
            LogUtil.w("url=" + url);

            //使用这个parser就不能在不同的博客间跳转
            // TODO 根据域名判断博客类型
            String blogUrl = mBlogParser.getBlogContentUrl(url);
            if (!TextUtils.isEmpty(blogUrl)) {
                if (blogUrl.equalsIgnoreCase(mCurrentUrl)) {
                    return true;
                }
                
                mProgressBar.setVisibility(View.VISIBLE);
                toggleAdShow(true);
                
                mCurrentUrl = blogUrl;
                BlogManager.getInstance().fetchBlogContent(blogUrl, BlogContentActivity.this);
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            boolean isBlockImage = SettingPreference.getInstance().getLoadImgOnlyInWifiEnable();
            
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
        if (TextUtils.isEmpty(mBlogInfo.summary)) {
            mBlogInfo.summary = getString(R.string.app_description);
        }
        
        final Bundle bundle = new Bundle();
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, mBlogInfo.title);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, mBlogInfo.summary);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mCurrentUrl);
        ShareManager.getInstance().shareBlogToQQ(BlogContentActivity.this, bundle);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ShareManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Qhad.activityDestroy(this);

        mWebView.stopLoading();
        mWebView.onPause();
        mWebView.destroy();

        mHandler.removeCallbacksAndMessages(null);
    }
    
    private void showErrorPage() {
        UsageStatsManager.sendUsageData(UsageStatsManager.EXP_EMPTY_BLOG, TypeManager.getBlogName(mBlogInfo.type));
        
        mWebView.setVisibility(View.INVISIBLE);
        mBtnFavo.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mReLoadImageView.setVisibility(View.VISIBLE);
    }
    
    private static final int MSG_UPDATE = 1;
    private Handler.Callback mCallback = new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE:
                    mReLoadImageView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    
                    mBtnFavo.setVisibility(View.VISIBLE);
                    mBtnFavo.setSelected(BlogManager.getInstance().isFavo(mBlogInfo));
                    mWebView.setVisibility(View.VISIBLE);
                    String content = (String) msg.obj;
                    mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), content,
                            "text/html", "utf-8", null);

                    mCurrentTitle = mBlogParser.getBlogTitle(mBlogInfo.type, content);
                    if (!TextUtils.isEmpty(mCurrentTitle)) {
                        mTitleBar.setTitle(mCurrentTitle);
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onFetchFinished(final Result<String> response) {
        toggleAdShow(false);// 隐藏广告
        LogUtil.i("response=" + response.data);
        if (TextUtils.isEmpty(response.data)) {
            showErrorPage();
        } else {
            ThreadManager.getPoolProxy().execute(new Runnable() {
                @Override
                public void run() {
                    if (Config.isDebug) {
                        FileUtil.writeFile("/sdcard/blogBefore", response.data);
                    }
                    String contentHtml = mBlogParser.getBlogContent(mBlogInfo.type, response.data);
                    if (TextUtils.isEmpty(contentHtml)) { // 解析失败则直接显示原网页
                        contentHtml = response.data;
                    }
                    if (response.url.equalsIgnoreCase(mBlogInfo.link)) {
                        String cachePath = DataManager.getBlogCachePath(mBlogInfo.blogId);
                        mBlogInfo.localPath = cachePath;
                        FileUtil.writeFile(cachePath, contentHtml);
                        BlogManager.getInstance().saveBlog(mBlogInfo);
                    }

                    mBlogStack.push(contentHtml);

                    Message msg = mHandler.obtainMessage(MSG_UPDATE);
                    msg.obj = contentHtml;
                    mHandler.sendMessage(msg);
                } // end run
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
        mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), mBlogStack.peek(),
                "text/html", "utf-8", null);
    }
}
