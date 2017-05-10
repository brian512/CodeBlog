
package com.brian.codeblog.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.brian.codeblog.Config;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.manager.BlogManager;
import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.Bloger;
import com.brian.codeblog.model.event.TypeChangeEvent;
import com.brian.codeblog.parser.BlogHtmlParserFactory;
import com.brian.codeblog.parser.IBlogHtmlParser;
import com.brian.codeblog.proctocol.HttpGetBlogListRequest;
import com.brian.codeblog.stat.UsageStatsManager;
import com.brian.common.datacenter.network.IResponseCallback;
import com.brian.common.tools.CommonAdapter;
import com.brian.common.tools.Env;
import com.brian.common.tools.ThreadManager;
import com.brian.common.utils.FileUtil;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.ResourceUtil;
import com.brian.common.view.RefreshLayout;
import com.brian.csdnblog.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment页面
 */
public class BlogListFrag extends Fragment {

    private static final String TAG = BlogListFrag.class.getSimpleName();

    private View mRootLy;
    @BindView(R.id.swipe_container) RefreshLayout mRefreshLayout;
    @BindView(R.id.blogListView) ListView mBlogListView;// 博客列表
    @BindView(R.id.no_blog) View mNoBlogView; // 无数据时显示
    private View mFooterLayout;

    private AdView mAdView;

    private boolean mRefreshable = true;
    
    private String mPageName = "BlogListFrag";
    
    private CommonAdapter<BlogInfo> mAdapter;// 列表适配器

    private Bloger mBloger;

    private int mCurrentPage = 1;

    private boolean mIsEnd = false;// 标记是否已经加载完所有数据

    private int mType = -1;
    
    private boolean hasInitedData = false;
    
    private IBlogHtmlParser mBlogParser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 设置博客类型
     */
    public void setType(int type) {
        mType = type;

        mBlogParser = BlogHtmlParserFactory.getBlogParser(mType);
        BaseActivity.getUIHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }, 200);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateView: " + mType);
        initUI(inflater);// 初始化组件
        return mRootLy;
    }

    private void initUI(LayoutInflater inflater) {
        mRootLy = inflater.inflate(R.layout.frag_bloglist, null);
        ButterKnife.bind(this, mRootLy);

        mAdapter = new CommonAdapter<BlogInfo>(Env.getContext(), null, R.layout.item_list_blog) {
            private ForegroundColorSpan mColorSpanName = new ForegroundColorSpan(ResourceUtil.getColor(R.color.light_blue));
            @Override
            public void convert(ViewHolder holder, final BlogInfo item) {
                holder.setText(R.id.title, item.title);
                holder.setText(R.id.description, item.summary);
                TextView nameView = holder.getView(R.id.msg);

                Bloger bloger = Bloger.fromJson(item.blogerJson);
                if (bloger != null && !TextUtils.isEmpty(bloger.nickName) && !TextUtils.isEmpty(item.extraMsg)) {
                    SpannableStringBuilder builder = new SpannableStringBuilder(item.extraMsg);
                    int indexStart = item.extraMsg.indexOf(bloger.nickName);
                    if (indexStart < 0) {
                        nameView.setText(item.extraMsg);
                    } else {
                        builder.setSpan(mColorSpanName, indexStart, indexStart + bloger.nickName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        nameView.setText(builder);
                    }
                } else {
                    nameView.setText(item.extraMsg);
                }

                nameView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(item.blogerID)) {
                            LogUtil.log(item.blogerJson);
                            Bloger bloger = Bloger.fromJson(item.blogerJson);
                            if (bloger != null) {
                                BlogerBlogListActivity.startActivity(getActivity(), mType, bloger);
                                UsageStatsManager.reportData(UsageStatsManager.USAGE_BLOGER_ENTR, "bloglist");
                            }
                        }
                    }
                });
                holder.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BlogContentActivity.startActvity(getActivity(), item);
                    }
                });
            }
            
            private int lastPosition = -1;
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.anim_up_from_bottom : R.anim.anim_down_from_top);
                view.startAnimation(animation);
                lastPosition = position;
                return view;
            }
            
            @Override
            public void notifyDataSetChanged() {
                lastPosition = -1;
                super.notifyDataSetChanged();
            }
        };

        mRefreshLayout.setChildView(mBlogListView);
        mRefreshLayout.setColorSchemeResources(R.color.blue,
                R.color.green_yellow,
                R.color.red,
                R.color.yellow);

        if (SettingPreference.getInstance().getAdsEnable()) {
            if (mAdView == null) {
                mAdView = (AdView)inflater.inflate(R.layout.view_bloglist_ad, null);
                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        LogUtil.log("onAdLoaded");
                        mBlogListView.addHeaderView(mAdView);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);
                        if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                            LogUtil.log("没有广告填充：" + errorCode);
                        } else {
                            LogUtil.log("errorCode=" + errorCode);
                        }
                    }
                });
            }
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        mFooterLayout = inflater.inflate(R.layout.loading_footer, null);
        mFooterLayout.setVisibility(View.GONE);
        mBlogListView.addFooterView(mFooterLayout);
        mBlogListView.setAdapter(mAdapter);// 设置适配器

        mNoBlogView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh();
            }
        });

        if (TypeManager.getWebType(mType) == TypeManager.TYPE_WEB_FAVO) {
            mAdapter.initListWithDatas(BlogManager.getInstance().getFavoBlogList(0));
            mRefreshable = false;
        } else if (TypeManager.getWebType(mType) == TypeManager.TYPE_WEB_HISTORY) {
            mAdapter.initListWithDatas(BlogManager.getInstance().getHistoryBlogList(0));
            mRefreshable = false;
        } else {
            mRefreshable = true;
        }
        mRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRefreshable) {
                    loadData(true);
                } else {
                    mRefreshLayout.setRefreshing(false);
                }
            }
        });

        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                if (mRefreshable) {
                    loadData(false);
                } else {
                    if (!mIsEnd) {
                        List<BlogInfo> list = null;
                        if (TypeManager.getWebType(mType) == TypeManager.TYPE_WEB_FAVO) {
                            list = BlogManager.getInstance().getFavoBlogList(mCurrentPage);
                        } else if (TypeManager.getWebType(mType) == TypeManager.TYPE_WEB_HISTORY) {
                            list = BlogManager.getInstance().getHistoryBlogList(mCurrentPage);
                        }
                        if (list == null || list.isEmpty()) {
                            mIsEnd = true;
                        } else {
                            mCurrentPage++;
                            mAdapter.addDatas(list);
                        }
                    }
                    mRefreshLayout.setLoading(false);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mRefreshLayout != null) {
            startRefresh();
        }
    }

    // 初始化
    private void initData() {
        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                LogUtil.log("mType=" + Integer.toHexString(mType));
                try {
                    String cachedStr = FileUtil.getFileContent(Env.getContext().getFilesDir() + "/cache_" + mType);
                    final List<BlogInfo> list = new Gson().fromJson(cachedStr, new TypeToken<List<BlogInfo>>() {} .getType());
                    if (list != null && !list.isEmpty()) {
                        BaseActivity.getUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.initListWithDatas(list);
                                mCurrentPage = 1;
                                hasInitedData = true;
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadData(final boolean isRefresh) {
        if (mBlogParser == null) {
            return; // 设置type后，还会调用一次
        }
        mNoBlogView.setVisibility(View.GONE);
        String url = getUrl(isRefresh);
        LogUtil.i(TAG, "refreshUrl=" + url);

        HttpGetBlogListRequest.RequestParam param = new HttpGetBlogListRequest.RequestParam();
        param.url = url;
        param.type = mType;
        new HttpGetBlogListRequest().request(param, new IResponseCallback<HttpGetBlogListRequest.ResultData>() {
            @Override
            public void onSuccess(final HttpGetBlogListRequest.ResultData resultData) {
                LogUtil.d("resultData=" + resultData.blogInfoList);
                if (isRefresh) {
                    mRefreshLayout.setRefreshing(false);
                } else {
                    mRefreshLayout.setLoading(false);
                }

                if (resultData.blogInfoList == null || resultData.blogInfoList.isEmpty()) {
                    LogUtil.e("empty response");
                    if (mAdapter.isEmpty()) {
                        showNoBlog();
                    }
                } else {
                    if (isRefresh) {
                        mAdapter.initListWithDatas(resultData.blogInfoList);
                        mBlogListView.setVisibility(View.VISIBLE);
                        mBlogListView.smoothScrollToPosition(0);
                        mNoBlogView.setVisibility(View.GONE);
                        mCurrentPage = 1;
                        hasInitedData = true;

                        if (isRefresh) {
                            ThreadManager.getPoolProxy().execute(new Runnable() {
                                @Override
                                public void run() {
                                    FileUtil.writeFile(Env.getContext().getFilesDir() + "/cache_" + mType, new Gson().toJson(resultData.blogInfoList));
                                }
                            });
                        }
                    } else {
                        mFooterLayout.setVisibility(View.GONE);
                        mCurrentPage++;
                        mAdapter.addDatas(resultData.blogInfoList);
                    }
                }
            }

            @Override
            public void onError(int rtn, String msg) {
                LogUtil.d("resultData=" + msg);
                if (mAdapter.isEmpty()) {
                    showNoBlog();
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                LogUtil.d("errorCode=" + errorCode);
                if (mAdapter.isEmpty()) {
                    showNoBlog();
                }
            }
        });

    }

    private String getUrl(boolean isRefresh) {
        String url;
        if (isRefresh) {
            if (mBloger != null) {
                url = mBlogParser.getBlogerUrl(mBloger.homePageUrl, 1);
            } else {
                url = mBlogParser.getUrlByType(mType, 1);
            }
        } else {
            if (mBloger != null) {
                url = mBlogParser.getBlogerUrl(mBloger.homePageUrl, mCurrentPage + 1);
            } else {
                url = mBlogParser.getUrlByType(mType, mCurrentPage + 1);
            }
            mFooterLayout.setVisibility(View.VISIBLE);
        }
        return url;
    }

    private void showNoBlog() {
        UsageStatsManager.reportData(UsageStatsManager.EXP_EMPTY_LIST, TypeManager.getBlogName(mType));
        
        mBlogListView.setVisibility(View.GONE);
        mNoBlogView.setVisibility(View.VISIBLE);
    }
    

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtil.log("" + isVisibleToUser);
        if (isVisibleToUser) {
            MobclickAgent.onPageStart(mPageName);
            if (mAdapter != null && (mAdapter.getDatas() == null || mAdapter.getDatas().isEmpty())) {
                startRefresh();
            }
        } else {
            MobclickAgent.onPageEnd(mPageName); 
        }
    }
    
    /**
     * 设置页面名称，用于统计
     */
    public void setPageName(String pageName) {
        mPageName = pageName;
    }

    public void setBloger(Bloger bloger) {
        mBloger = bloger;
    }
    
    public void clearList() {
        if (mAdapter != null) {
            mAdapter.removeAllDatas();
        }
    }

    public boolean isListEmpty() {
        return mAdapter == null || mAdapter.isEmpty();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.d("onStart");
        EventBus.getDefault().register(this);

        if (getArguments() != null) {
            mBloger = (Bloger) getArguments().getSerializable(BlogerBlogListActivity.EXTRA_KEY_BLOGER);
        }
    }
    
    @Override
    public void onStop() {
        LogUtil.d("onStop");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        LogUtil.d("onDestroy");

        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
    
    /**
     * EventBus回调
     */
    @Subscribe
    public void onEventMainThread(TypeChangeEvent event) {
        mType = TypeManager.updateCateType(mType, event.cateType);
        
        if (mBlogListView != null) {
            startRefresh();
        }
    }

    private void startRefresh() {
        if (mRefreshable && (checkUpdateTime()||Config.DEBUG_ENABLE)) {
            mRefreshLayout.setRefreshing(true);
            loadData(true);
        }
    }

    private boolean checkUpdateTime() {
        long lastUpdateTime = FileUtil.getFileLastModified(Env.getContext().getFilesDir() + "/cache_" + mType);
        return System.currentTimeMillis() - lastUpdateTime > 3000_000;
    }
}
