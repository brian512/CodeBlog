
package com.brian.codeblog.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.brian.codeblog.Env;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.manager.AdHelper;
import com.brian.codeblog.manager.BlogManager;
import com.brian.codeblog.manager.BlogerManager;
import com.brian.codeblog.manager.DataManager;
import com.brian.codeblog.manager.ShareManager;
import com.brian.codeblog.manager.ThreadManager;
import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.manager.UsageStatsManager;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.Bloger;
import com.brian.codeblog.model.SearchResult;
import com.brian.codeblog.parser.BlogHtmlParserFactory;
import com.brian.codeblog.parser.IBlogHtmlParser;
import com.brian.codeblog.proctocol.HttpGetBlogContentRequest;
import com.brian.codeblog.proctocol.base.IResponseCallback;
import com.brian.codeblog.util.FileUtil;
import com.brian.codeblog.util.LogUtil;
import com.brian.codeblog.util.NetStatusUtil;
import com.brian.codeblog.util.ToastUtil;
import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.tencent.connect.share.QQShare;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import tj.zl.op.normal.common.ErrorCode;
import tj.zl.op.normal.spot.SpotListener;
import tj.zl.op.normal.spot.SpotManager;

public class BlogContentActivity extends BaseActivity {

    private static final String TAG = BlogContentActivity.class.getSimpleName();

    @BindView(R.id.title_bar) TitleBar mTitleBar;
    @BindView(R.id.article_content) WebView mWebView;
    @BindView(R.id.blogContentPro) ProgressBar mProgressBar; // 进度条
    @BindView(R.id.reLoadImage) ImageView mReLoadImageView; // 重新加载的图片
    @BindView(R.id.ad_group) FrameLayout mAdLayout; // 广告
    private View mAdView;
    private PopupMenu mPopupMenu;

    private IBlogHtmlParser mBlogParser = null;

    private HttpGetBlogContentRequest mHttpClient;

    private boolean mHasFavoed;

    private long mStartTime;
    
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
        mStartTime = System.currentTimeMillis();

        initUI();// 初始化界面
        initListener();
        initAd();

        initBlogInfo();

        if (mBlogInfo == null) {
            finish();
            return;
        }
        initPopupMenu();

        mTitleBar.setTitle(mBlogInfo.title);
        mCurrentTitle = mBlogInfo.title;

        mProgressBar.setVisibility(View.VISIBLE);

        mHttpClient = new HttpGetBlogContentRequest();
        getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                loadData();
                BlogManager.getInstance().saveBlog(mBlogInfo);
            }
        });
    }

    private void loadData() {
        HttpGetBlogContentRequest.RequestParam param = new HttpGetBlogContentRequest.RequestParam();
        param.url = mCurrentUrl;
        param.type = mBlogInfo.type;
        if (TypeManager.getWebType(mBlogInfo.type) == TypeManager.TYPE_WEB_JCC) {
            param.charset = "GB2312";
        }
        mHttpClient.request(param, new IResponseCallback<HttpGetBlogContentRequest.ResultData>() {
            @Override
            public void onSuccess(final HttpGetBlogContentRequest.ResultData resultData) {
                LogUtil.d("resultData=" + resultData.blogContent);
                if (TextUtils.isEmpty(resultData.blogContent)) {
                    showErrorPage();
                    return;
                }
                if (mCurrentUrl.equalsIgnoreCase(mBlogInfo.link)) {
                    saveBlog(resultData.blogContent);
                }

                toggleAdShow(false);// 隐藏广告
                mReLoadImageView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);

                CommonPreference.getInstance().addBlogReadCount();// 阅读数+1
                mBlogStack.push(resultData.blogContent);

                mHasFavoed = BlogManager.getInstance().isFavo(mBlogInfo);
                if (mHasFavoed) {
                    mPopupMenu.getMenu().getItem(1).setTitle("取消收藏");
                } else {
                    mPopupMenu.getMenu().getItem(1).setTitle("收藏");
                }
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), resultData.blogContent,
                        "text/html", "utf-8", null);

                String title = mBlogParser.getBlogTitle(mBlogInfo.type, resultData.blogContent);
                if (!TextUtils.isEmpty(title)) {
                    mCurrentTitle = title;
                    mTitleBar.setTitle(mCurrentTitle);
                }
            }

            @Override
            public void onError(int rtn, String msg) {
                LogUtil.d("resultData=" + msg);
                showErrorPage();
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                LogUtil.d("errorCode=" + errorCode);
                showErrorPage();
            }
        });
    }

    private void initBlogInfo() {
        try {
            mBlogInfo = (BlogInfo) getIntent().getExtras().getSerializable(BUNDLE_EXTRAS_BLOGINFO);
        } catch (Exception e) {
            ToastUtil.showMsg("oops，打开出错。。。");
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

    private void saveBlog(final String blogContent) {
        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                String cachePath = DataManager.getBlogCachePath(mBlogInfo.blogId);
                mBlogInfo.localPath = cachePath;
                FileUtil.writeFile(cachePath, blogContent);
                BlogManager.getInstance().saveBlog(mBlogInfo);
            }
        });
    }

    private void initAd() {
        SpotManager.getInstance(Env.getContext()).setImageType(SpotManager.IMAGE_TYPE_VERTICAL);
        // 获取原生插屏控件
        mAdView = SpotManager.getInstance(Env.getContext()).getNativeSpot(Env.getContext(), new SpotListener() {

                    @Override
                    public void onShowSuccess() {
                        Log.d(TAG, "插屏展示成功");
                    }

                    @Override
                    public void onShowFailed(int errorCode) {
                        Log.d(TAG, "插屏展示失败" + errorCode);
                        switch (errorCode) {
                            case ErrorCode.NON_NETWORK:
                                LogUtil.e("YoumiSdk网络异常");
                                break;
                            case ErrorCode.NON_AD:
                                LogUtil.e("YoumiSdk暂无广告");
                                break;
                            case ErrorCode.RESOURCE_NOT_READY:
                                LogUtil.e(TAG, "YoumiSdk资源还没准备好");
                                break;
                            case ErrorCode.SHOW_INTERVAL_LIMITED:
                                LogUtil.e(TAG, "YoumiSdk展示间隔限制");
                                break;
                            case ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE:
                                LogUtil.e(TAG, "YoumiSdk控件处在不可见状态");
                                break;
                        }
                    }

                    @Override
                    public void onSpotClosed() {
                        LogUtil.d(TAG, "YoumiSdk插屏被关闭");
                    }

                    @Override
                    public void onSpotClicked(boolean isWebPage) {
                        LogUtil.d(TAG, "YoumiSdk插屏被点击");
                    }
                });
        toggleAdShow(true);
    }

    private void toggleAdShow(boolean isShow) {
        if (mAdView != null && isShow && SettingPreference.getInstance().getAdsEnable() && AdHelper.isAdCanShow) {
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            try {
                mAdLayout.removeAllViews();
            } catch (Exception e) {
                LogUtil.printError(e);
            }
            if (mAdView.getParent() != null) {
                ((ViewGroup)mAdView.getParent()).removeView(mAdView);
            }
            // 添加原生插屏控件到容器中
            mAdLayout.addView(mAdView, layoutParams);
            if (mAdLayout.getVisibility() != View.VISIBLE) {
                mAdLayout.setVisibility(View.VISIBLE);
            }
        } else {
            mAdLayout.setVisibility(View.GONE);
        }
    }
    

    private void initUI() {
        mTitleBar.setRightImageResource(R.drawable.ic_menu);
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

    private void initPopupMenu() {
        mPopupMenu = new PopupMenu(this, mTitleBar.getRightButton());
        // 通过代码添加菜单项
        Menu menu = mPopupMenu.getMenu();
        menu.add(Menu.NONE, Menu.FIRST, 0, "分享");
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "收藏");
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "博主列表");

        if (BlogerManager.getsInstance().getCurrBloger().blogerID.equalsIgnoreCase(mBlogInfo.blogerID)) {
            mPopupMenu.getMenu().getItem(2).setVisible(false);
        }

        // 监听事件
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case Menu.FIRST:
                        UsageStatsManager.sendUsageData(UsageStatsManager.MENU_CONTENT_LIST, "分享");
                        onClickShare();
                        break;
                    case Menu.FIRST + 1:
                        UsageStatsManager.sendUsageData(UsageStatsManager.MENU_CONTENT_LIST, "收藏");
                        mHasFavoed = !mHasFavoed;
                        BlogManager.getInstance().doFavo(mBlogInfo, mHasFavoed);
                        if (mHasFavoed) {
                            mPopupMenu.getMenu().getItem(1).setTitle("取消收藏");
                        } else {
                            mPopupMenu.getMenu().getItem(1).setTitle("收藏");
                        }
                        break;
                    case Menu.FIRST + 2:
                        UsageStatsManager.sendUsageData(UsageStatsManager.MENU_CONTENT_LIST, "博主");
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
                mPopupMenu.show();
            }
        });
        mReLoadImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mReLoadImageView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                toggleAdShow(true);
                
                loadData();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果有需要，可以点击后退关闭插播广告。
            if (mAdLayout != null && mAdLayout.getVisibility() == View.VISIBLE) {
                mAdLayout.setVisibility(View.GONE);
                return true;
            }

            if (mBlogStack.size() > 1) {
                mBlogStack.pop();//把当前的博客移除
                String html = mBlogStack.peek();
                mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), html,
                        "text/html", "utf-8", null);
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
                loadData();
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
    protected void onPause() {
        super.onPause();
        // 插播广告
        SpotManager.getInstance(this).onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 插播广告
        SpotManager.getInstance(this).onStop();
    }

    @Override
    protected void onDestroy() {
        CommonPreference.getInstance().addBlogReadTime(System.currentTimeMillis() - mStartTime);
        super.onDestroy();

        mWebView.stopLoading();
        mWebView.onPause();
        mWebView.destroy();
    }
    
    private void showErrorPage() {
        UsageStatsManager.sendUsageData(UsageStatsManager.EXP_EMPTY_BLOG, TypeManager.getBlogName(mBlogInfo.type));
        
        mWebView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mReLoadImageView.setVisibility(View.VISIBLE);
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
