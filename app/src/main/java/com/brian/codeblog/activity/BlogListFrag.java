
package com.brian.codeblog.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.brian.codeblog.Config;
import com.brian.codeblog.Env;
import com.brian.codeblog.datacenter.preference.SettingPreference;
import com.brian.codeblog.manager.AdHelper;
import com.brian.codeblog.manager.BlogManager;
import com.brian.codeblog.manager.DataFetcher;
import com.brian.codeblog.manager.DataFetcher.OnFetchDataListener;
import com.brian.codeblog.manager.DataFetcher.Result;
import com.brian.codeblog.manager.ThreadManager;
import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.manager.UsageStatsManager;
import com.brian.codeblog.model.BlogInfo;
import com.brian.codeblog.model.Bloger;
import com.brian.codeblog.model.event.TypeChangeEvent;
import com.brian.codeblog.parser.BlogHtmlParserFactory;
import com.brian.codeblog.parser.IBlogHtmlParser;
import com.brian.codeblog.util.CommonAdapter;
import com.brian.codeblog.util.FileUtil;
import com.brian.codeblog.util.LogUtil;
import com.brian.codeblog.util.ResourceUtil;
import com.brian.codeblog.util.WeakRefHandler;
import com.brian.common.view.RefreshLayout;
import com.brian.csdnblog.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tj.zl.op.normal.banner.BannerManager;
import tj.zl.op.normal.banner.BannerViewListener;

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
        mHandler.postDelayed(new Runnable() {
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
                                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_BLOGER_ENTR, "bloglist");
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

        mRootLy = inflater.inflate(R.layout.frag_bloglist, null);
        ButterKnife.bind(this, mRootLy);

        mRefreshLayout.setChildView(mBlogListView);
        mRefreshLayout.setColorSchemeResources(R.color.blue,
                R.color.green_yellow,
                R.color.red,
                R.color.yellow);

        if (AdHelper.isAdCanShow && SettingPreference.getInstance().getAdsEnable() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            final LinearLayout adLy = new LinearLayout(getContext());
            View bannerView = BannerManager.getInstance(getContext()).getBannerView(new BannerViewListener() {
                @Override
                public void onRequestSuccess() {
                    LogUtil.i(TAG, "YoumiSdk 请求广告条成功");
                }

                @Override
                public void onSwitchBanner() {
                    LogUtil.i(TAG, "YoumiSdk 广告条切换");
                }

                @Override
                public void onRequestFailed() {
                    mBlogListView.removeHeaderView(adLy);
                    LogUtil.e(TAG, "YoumiSdk 请求广告条失败");
                }
            });
            adLy.removeAllViews();
            adLy.addView(bannerView);
            mBlogListView.addHeaderView(adLy);
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
                List<BlogInfo> list = null;
                LogUtil.log("mType=" + mType);
                try {
                    String cachedStr = FileUtil.getFileContent(Env.getContext().getFilesDir() + "/cache_" + mType);
                    list = new Gson().fromJson(cachedStr, new TypeToken<List<BlogInfo>>() {} .getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg = mHandler.obtainMessage(MSG_LIST_UPDATE);
                msg.obj = list;
                mHandler.sendMessage(msg);
            }
        });

    }

    private void loadData(final boolean isRefresh) {
        if (mBlogParser == null) {
            return; // 设置type后，还会调用一次
        }
        LogUtil.i(TAG, "onRefresh");
        mNoBlogView.setVisibility(View.GONE);
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
        LogUtil.i(TAG, "refreshUrl=" + url);
        
        DataFetcher.getInstance().fetchString(url, new OnFetchDataListener<Result<String>>() {
            
            @Override
            public void onFetchFinished(final Result<String> response) {
                if (isRefresh) {
                    mRefreshLayout.setRefreshing(false);
                } else {
                    mRefreshLayout.setLoading(false);
                }
                
                if (TextUtils.isEmpty(response.data)) {
                    LogUtil.e("empty response");
                    if (mAdapter.isEmpty()) {
                        showNoBlog();
                    }
                } else {
                    ThreadManager.getPoolProxy().execute(new Runnable() {
                        @Override
                        public void run() {
                            List<BlogInfo> list = getBlogList(response.data, isRefresh);
                            if (isRefresh) {
                                Message msg = mHandler.obtainMessage(MSG_LIST_UPDATE);
                                msg.obj = list;
                                mHandler.sendMessage(msg);
                            } else {
                                Message msg = mHandler.obtainMessage(MSG_LIST_ADD);
                                msg.obj = list;
                                mHandler.sendMessage(msg);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private List<BlogInfo> getBlogList(String response, boolean isRefresh) {
        List<BlogInfo> list = mBlogParser.getBlogList(mType, response);
        if (isRefresh) {
            FileUtil.writeFile(Env.getContext().getFilesDir() + "/cache_" + mType, new Gson().toJson(list));
        }
        
        return list;
    }

    private void showNoBlog() {
        UsageStatsManager.sendUsageData(UsageStatsManager.EXP_EMPTY_LIST, TypeManager.getBlogName(mType));
        
        mBlogListView.setVisibility(View.GONE);
        mNoBlogView.setVisibility(View.VISIBLE);
    }
    
    
    private static final int MSG_LIST_UPDATE = 1;
    private static final int MSG_LIST_ADD = 2;
    
    private Handler.Callback mCallback = new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LIST_UPDATE:
                    List<BlogInfo> blogInfos = (List<BlogInfo>) msg.obj;
                    if (blogInfos != null && !blogInfos.isEmpty()) {
                        mBlogListView.setVisibility(View.VISIBLE);
                        mBlogListView.smoothScrollToPosition(0);
                        mNoBlogView.setVisibility(View.GONE);
                        mAdapter.initListWithDatas(blogInfos);
                        mCurrentPage = 1;
                    } else if (mAdapter.isEmpty() && hasInitedData) {
                        showNoBlog();
                    }
                    hasInitedData = true;
                    
                    break;
                    
                case MSG_LIST_ADD:
                    mFooterLayout.setVisibility(View.GONE);
                    List<BlogInfo> deltaBlogInfos = (List<BlogInfo>) msg.obj;
                    if (deltaBlogInfos != null && !deltaBlogInfos.isEmpty()) {
                        mCurrentPage++;
                        mAdapter.addDatas(deltaBlogInfos);
                    }
                    break;
                    
                default:
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    
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
        if (mRefreshable && (checkUpdateTime()||Config.isDebug)) {
            mRefreshLayout.setRefreshing(true);
            loadData(true);
        }
    }

    private boolean checkUpdateTime() {
        long lastUpdateTime = FileUtil.getFileLastModified(Env.getContext().getFilesDir() + "/cache_" + mType);
        return System.currentTimeMillis() - lastUpdateTime > 3000_000;
    }
}
