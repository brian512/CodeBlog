
package com.brian.codeblog.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.brian.codeblog.ad.AdMobHelper;
import com.brian.codeblog.datacenter.DataManager;
import com.brian.codeblog.datacenter.preference.CommonPreference;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.manager.BlogManager;
import com.brian.codeblog.manager.BlogerManager;
import com.brian.codeblog.manager.ShareManager;
import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.Bloger;
import com.brian.codeblog.model.SearchResult;
import com.brian.codeblog.parser.BlogHtmlParserFactory;
import com.brian.codeblog.parser.IBlogHtmlParser;
import com.brian.codeblog.pay.PayHelper;
import com.brian.codeblog.proctocol.HttpGetBlogContentRequest;
import com.brian.codeblog.stat.UsageStatsManager;
import com.brian.common.datacenter.network.IResponseCallback;
import com.brian.common.tools.DayNightHelper;
import com.brian.common.tools.Env;
import com.brian.common.tools.ThreadManager;
import com.brian.common.utils.FileUtil;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.NetStatusUtil;
import com.brian.common.utils.ResourceUtil;
import com.brian.common.utils.ToastUtil;
import com.brian.common.view.TitleBar;
import com.brian.csdnblog.R;
import com.tencent.connect.share.QQShare;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BlogContentActivity extends BaseActivity {

    private static final String TAG = BlogContentActivity.class.getSimpleName();

    @BindView(R.id.title_bar) TitleBar mTitleBar;
    @BindView(R.id.article_content) WebView mWebView;
    @BindView(R.id.blogContentPro) ProgressBar mProgressBar; // 进度条
    @BindView(R.id.reLoadImage) ImageView mReLoadImageView; // 重新加载的图片
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
        if (CommonPreference.getInstance().getPayCount() <= 0) {
            // 有打赏则不显示广告
            toggleAdShow(true);
        }

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
//                LogUtil.d("resultData=" + resultData.blogContent);
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
                try {
                    mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), resultData.blogContent,
                            "text/html", "utf-8", null);

                    String title = mBlogParser.getBlogTitle(mBlogInfo.type, resultData.blogContent);
                    if (!TextUtils.isEmpty(title)) {
                        mCurrentTitle = title;
                        mTitleBar.setTitle(mCurrentTitle);
                    }
                } catch (Exception e) {
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
        UsageStatsManager.reportData(UsageStatsManager.USAGE_BLOG_COUNT, TypeManager.getBlogName(mBlogInfo.type));
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

    private void toggleAdShow(boolean isShow) {
        if (isShow && SettingPreference.getInstance().getAdsEnable()) {
            AdMobHelper.show();
        } else {
            AdMobHelper.hide();
        }
    }
    

    private void initUI() {
        mTitleBar.setRightImageResource(R.drawable.ic_menu);
        // 点击图片重新加载
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebView.setBackgroundColor(ResourceUtil.getColor(R.color.common_bg));

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
                        UsageStatsManager.reportData(UsageStatsManager.MENU_CONTENT_LIST, "分享");
                        onClickShare();
                        break;
                    case Menu.FIRST + 1:
                        UsageStatsManager.reportData(UsageStatsManager.MENU_CONTENT_LIST, "收藏");
                        mHasFavoed = !mHasFavoed;
                        BlogManager.getInstance().doFavo(mBlogInfo, mHasFavoed);
                        if (mHasFavoed) {
                            mPopupMenu.getMenu().getItem(1).setTitle("取消收藏");
                        } else {
                            mPopupMenu.getMenu().getItem(1).setTitle("收藏");
                        }
                        break;
                    case Menu.FIRST + 2:
                        UsageStatsManager.reportData(UsageStatsManager.MENU_CONTENT_LIST, "博主");
                        if (!TextUtils.isEmpty(mBlogInfo.blogerID)) {
                            LogUtil.log(mBlogInfo.blogerJson);
                            Bloger bloger = Bloger.fromJson(mBlogInfo.blogerJson);
                            if (bloger != null) {
                                BlogerBlogListActivity.startActivity(BlogContentActivity.this, mBlogInfo.type, bloger);
                                UsageStatsManager.reportData(UsageStatsManager.USAGE_BLOGER_ENTR, "bloglist");
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
            handleBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void handleBack() {
        if (mBlogStack.size() > 1) {
            mBlogStack.pop();//把当前的博客移除
            String html = mBlogStack.peek();
            mWebView.loadDataWithBaseURL(mBlogParser.getBlogBaseUrl(), html,
                    "text/html", "utf-8", null);
        } else {
            mWebView.setWebViewClient(null);
            mWebView.setWebChromeClient(null);
            mWebView.loadData("<html></html>", "text/html", "utf-8");
            finish();
        }
    }

    /**
     * 继承WebViewClient
     */
    private class MyWebViewClient extends WebViewClient {

        // 重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtil.w("url=" + url);

            //使用这个parser就不能在不同的博客间跳转
            // TODO 根据域名判断博客类型
            String blogUrl = mBlogParser.getBlogContentUrl(url);
            if (!TextUtils.isEmpty(blogUrl) && blogUrl.startsWith("http")) {
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
            if (DayNightHelper.getInstance().isDayNightEnabled()) {
                String js = "document.body.style.backgroundColor=\"#333\";document.body.style.color=\"white\";";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(js, null);
                } else {
                    mWebView.loadUrl("javascript:" + js);
                }
                mWebView.setBackgroundColor(Color.TRANSPARENT);
            }

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
    }

    @Override
    protected void onStop() {
        long readTime = System.currentTimeMillis() - mStartTime;
        CommonPreference.getInstance().addBlogReadTime(readTime);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        long readTime = System.currentTimeMillis() - mStartTime;
        if (readTime > 60_000) {
            if (PayHelper.shouldNotifyPay()) {
                PayHelper.pay("学习到新知识，支持一下", new PayHelper.IPayListener() {
                    @Override
                    public void onResult(boolean isOK) {
                        if (!isOK) {
                            ToastUtil.showMsg("打赏未完成");
                        }
                    }
                });
            } else {
                CommonPreference.getInstance().addPayCount(-1);
            }
        }

        mWebView.stopLoading();
        mWebView.onPause();
        mWebView.destroy();
        super.onDestroy();
    }
    
    private void showErrorPage() {
        UsageStatsManager.reportData(UsageStatsManager.EXP_EMPTY_BLOG, TypeManager.getBlogName(mBlogInfo.type));
        
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
