
package com.brian.csdnblog.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.brian.common.view.RefreshLayout;
import com.brian.csdnblog.Config;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.DataFetcher;
import com.brian.csdnblog.manager.DataFetcher.OnFetchDataListener;
import com.brian.csdnblog.manager.DataFetcher.Result;
import com.brian.csdnblog.manager.FavoBlogManager;
import com.brian.csdnblog.manager.HistoryBlogManager;
import com.brian.csdnblog.manager.SettingPreference;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.model.TypeChangeEvent;
import com.brian.csdnblog.parser.BlogHtmlParserFactory;
import com.brian.csdnblog.parser.IBlogHtmlParser;
import com.brian.csdnblog.util.CommonAdapter;
import com.brian.csdnblog.util.FileUtil;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.WeakRefHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qhad.ads.sdk.adcore.Qhad;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
/**
 * Fragment页面
 */
public class BlogListFrag extends Fragment {

    private static final String TAG = BlogListFrag.class.getSimpleName();

    private View mRootLy;
    private RefreshLayout mRefreshLayout;
    private ListView mBlogListView;// 博客列表
    private View mNoBlogView; // 无数据时显示
    private View mFooterLayout;

    private boolean mRefreshable = true;
    
    private String mPageName = "BlogListFrag";
    
    private CommonAdapter<BlogInfo> mAdapter;// 列表适配器

    private int mCurrentPage = 1;

    private int mType = -1;
    
    private boolean hasInitedData = false;
    
    private IBlogHtmlParser mBlogParser;
    
    /**
     * 设置博客类型
     */
    public void setType(int type) {
        mType = type;
        mBlogParser = BlogHtmlParserFactory.getBlogParser(mType);
        initData();
        if (mRefreshLayout != null) {
            startRefresh();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateView: " + mType);
        initUI(inflater);// 初始化组件
        return mRootLy;
    }

    private void initUI(LayoutInflater inflater) {
        mAdapter = new CommonAdapter<BlogInfo>(Env.getContext(), null, R.layout.item_list_blog) {
    
            @Override
            public void convert(ViewHolder holder, final BlogInfo item) {
                holder.setText(R.id.title, item.title);
                holder.setText(R.id.description, item.description);
                holder.setText(R.id.msg, item.msg);
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
        mRefreshLayout = (RefreshLayout) mRootLy.findViewById(R.id.swipe_container);
        mBlogListView = (ListView) mRootLy.findViewById(R.id.blogListView);
        mRefreshLayout.setChildView(mBlogListView);

        mRefreshLayout.setColorSchemeResources(R.color.blue,
                R.color.green_yellow,
                R.color.red,
                R.color.yellow);

        if (SettingPreference.getIsShowAd(Env.getContext())) {
            FrameLayout adLy  = (FrameLayout) inflater.inflate(R.layout.layout_ad, null);
            String adSpaceid = Config.getAdBannerKey();
            if (!TextUtils.isEmpty(adSpaceid)) {
                Qhad.showBanner(adLy, BaseActivity.getTopActivity(), adSpaceid, false);
            }
            mBlogListView.addHeaderView(adLy);
        }
        mFooterLayout = inflater.inflate(R.layout.loading_footer, null);
        mFooterLayout.setVisibility(View.GONE);
        mBlogListView.addFooterView(mFooterLayout);
        mBlogListView.setAdapter(mAdapter);// 设置适配器

        mNoBlogView = mRootLy.findViewById(R.id.no_blog);
        mNoBlogView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh();
            }
        });

        if (TypeManager.getWebType(mType) == TypeManager.TYPE_WEB_FAVO) {
            mAdapter.addDatas(FavoBlogManager.getInstance().getFavoBlogList());
            mRefreshable = false;
        } else if (TypeManager.getWebType(mType) == TypeManager.TYPE_WEB_HISTORY) {
            mAdapter.addDatas(HistoryBlogManager.getInstance().getHistoryBlogList());
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
                loadData(false);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
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
            url = mBlogParser.getUrlByType(mType, 1);
        } else {
            url = mBlogParser.getUrlByType(mType, mCurrentPage + 1);
            mFooterLayout.setVisibility(View.VISIBLE);
        }
        LogUtil.i(TAG, "refreshUrl=" + url);
        
        DataFetcher.getInstance().fetchString(url, Config.INTERVAL_REFRESH, true, null, new OnFetchDataListener<Result<String>>() {
            
            @Override
            public void onFetchFinished(final Result<String> response) {
                
                if (isRefresh) {
                    mRefreshLayout.setRefreshing(false);
                } else {
                    mRefreshLayout.setLoading(false);
                }
                
                if (TextUtils.isEmpty(response.data)) {
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
    
    public void clearList() {
        if (mAdapter != null) {
            mAdapter.removeAllDatas();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        LogUtil.d("onStart");
        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onStop() {
        LogUtil.d("onStop");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        LogUtil.e("onDestroy");
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
        if (mRefreshable) {
            mRefreshLayout.setRefreshing(true);
            loadData(true);
        }
    }
}
