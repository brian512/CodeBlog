
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brian.common.view.RefreshLayout;
import com.brian.common.view.TitleBar;
import com.brian.common.view.ToastUtil;
import com.brian.csdnblog.BaseActivity;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.DataFetcher;
import com.brian.csdnblog.manager.DataFetcher.OnFetchDataListener;
import com.brian.csdnblog.manager.DataFetcher.Result;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.SearchResult;
import com.brian.csdnblog.parser.CSDNHtmlParser;
import com.brian.csdnblog.util.CommonAdapter;
import com.brian.csdnblog.util.UIUtil;
import com.brian.csdnblog.util.WeakRefHandler;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends BaseActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    private TitleBar mTitleBar;
    private EditText etSearch = null;
    private TextView btSearch = null;
    private ListView lvResult = null;
    private RefreshLayout mRefreshLayout;
    private ProgressBar progressBar = null;
    private View mFooterLayout;

    private CommonAdapter<SearchResult> mAdapter = null;

    private int mCurrentPage = 1;
    private String mInputText = "";

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, SearchActivity.class);
        activity.startActivity(intent);
    }
    
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        etSearch = (EditText) findViewById(R.id.et_search);
        btSearch = (TextView) findViewById(R.id.bt_search);
        lvResult = (ListView) findViewById(R.id.lv_result);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.swipe_container);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        mFooterLayout = getLayoutInflater().inflate(R.layout.loading_footer, null);
        mFooterLayout.setVisibility(View.GONE);
        lvResult.addFooterView(mFooterLayout);
        mRefreshLayout.setChildView(lvResult);

        mTitleBar.setTitle("CSDN搜索");
        mTitleBar.setRightImageVisible(View.INVISIBLE);
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UIUtil.hideKeyboard(etSearch);
                mInputText = etSearch.getText().toString()
                        .replaceAll(
                                "[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]",
                                "");
                if (!TextUtils.isEmpty(mInputText)) {
                    mInputText = mInputText.trim().replace(' ', '+');
                    String url = getSearchUrl(mInputText, 1);
                    mCurrentPage = 1;
                    loadListData(url);
                    UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_SEARCH, mInputText);
                } else {
                    ToastUtil.showToast("请输入适当关键字");
                }
            }
        });

        mAdapter = new CommonAdapter<SearchResult>(Env.getContext(), null, R.layout.item_list_search) {

            @Override
            public void convert(ViewHolder holder, final SearchResult item) {
                
                holder.setText(R.id.title, item.title);
                holder.setText(R.id.authorTime, item.authorTime);
                holder.setText(R.id.searchDetail, item.searchDetail);
                
                holder.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BlogContentActivity.startActvity(SearchActivity.this, item);
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
        };
        lvResult.setAdapter(mAdapter);

        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {

            @Override
            public void onLoad() {
                if (!TextUtils.isEmpty(mInputText)) {
                    loadListData(getSearchUrl(mInputText, mCurrentPage));
                }
            }
        });
        lvResult.setVisibility(View.INVISIBLE);
    }

    private String getSearchUrl(String keyWord, int page) {
        CSDNHtmlParser parser = CSDNHtmlParser.getInstance();
        return parser.getSearchUrlByKeyword(keyWord, page);
    }

    private void loadListData(String loadUrl) {
        if (mAdapter.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            mFooterLayout.setVisibility(View.VISIBLE);
        }

        DataFetcher.getInstance().fetchString(loadUrl, TimeUnit.MINUTES.toMillis(30), new OnFetchDataListener<Result<String>>() {

            @Override
            public void onFetchFinished(final Result<String> response) {
                mRefreshLayout.setLoading(false);

                if (TextUtils.isEmpty(response.data)) {
                    if (mAdapter.isEmpty()) {
                        // 没有搜索岛结果的提示
                        UsageStatsManager.sendUsageData(UsageStatsManager.EXP_EMPTY_SEARCH, mInputText);
                    }
                } else {
                    ThreadManager.getPoolProxy().execute(new Runnable() {
                        @Override
                        public void run() {
                            List<SearchResult> list = CSDNHtmlParser.getInstance().getSearchResultList(response.data);
                            Message msg = mHandler.obtainMessage(MSG_LIST_ADD);
                            msg.obj = list;
                            mHandler.sendMessage(msg);
                        }
                    });
                }
            }
        });
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

    private static final int MSG_LIST_ADD = 2;

    private Handler.Callback mCallback = new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LIST_ADD:
                    if (mCurrentPage <= 1) {
                        mAdapter.removeAllDatas();
                    }
                    lvResult.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    List<SearchResult> deltaBlogInfos = (List<SearchResult>) msg.obj;
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
}
