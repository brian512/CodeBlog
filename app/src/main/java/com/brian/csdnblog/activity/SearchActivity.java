
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brian.common.view.RefreshLayout;
import com.brian.common.view.TitleBar;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.DataFetcher;
import com.brian.csdnblog.manager.DataFetcher.OnFetchDataListener;
import com.brian.csdnblog.manager.DataFetcher.Result;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.model.SearchResult;
import com.brian.csdnblog.parser.CSDNHtmlParser;
import com.brian.csdnblog.util.CommonAdapter;
import com.brian.csdnblog.util.LogUtil;
import com.brian.csdnblog.util.ResourceUtil;
import com.brian.csdnblog.util.ToastUtil;
import com.brian.csdnblog.util.UIUtil;
import com.brian.csdnblog.util.WeakRefHandler;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    @BindView(R.id.title_bar) TitleBar mTitleBar;
    @BindView(R.id.et_search) EditText mSearchInput = null;
    @BindView(R.id.bt_search) TextView mSearchBtn = null;
    @BindView(R.id.lv_result) ListView mResultListView = null;
    @BindView(R.id.swipe_container) RefreshLayout mRefreshLayout;
    @BindView(R.id.progressbar) ProgressBar mProgressBar = null;
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
        ButterKnife.bind(this);

        initUI();
        initListener();
    }

    private void initUI() {
        mFooterLayout = getLayoutInflater().inflate(R.layout.loading_footer, null);
        mFooterLayout.setVisibility(View.GONE);
        mResultListView.addFooterView(mFooterLayout);
        mRefreshLayout.setChildView(mResultListView);

        mTitleBar.setTitle("CSDN搜索");
        mTitleBar.setRightImageVisible(View.INVISIBLE);

        mAdapter = new CommonAdapter<SearchResult>(Env.getContext(), null, R.layout.item_list_search) {
            private ForegroundColorSpan mColorSpanName = new ForegroundColorSpan(ResourceUtil.getColor(R.color.light_blue));
            @Override
            public void convert(ViewHolder holder, final SearchResult item) {
                holder.setText(R.id.title, item.title);
                holder.setText(R.id.authorTime, item.authorTime);
                holder.setText(R.id.searchDetail, item.searchDetail);

                TextView nameView = holder.getView(R.id.authorTime);
                Bloger bloger = Bloger.fromJson(item.blogerJson);
                if (bloger != null && !TextUtils.isEmpty(bloger.nickName) && !TextUtils.isEmpty(item.authorTime)) {
                    SpannableStringBuilder builder = new SpannableStringBuilder(item.authorTime);
                    int indexStart = item.authorTime.indexOf(bloger.nickName);
                    builder.setSpan(mColorSpanName, indexStart, indexStart + bloger.nickName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    nameView.setText(builder);
                } else {
                    nameView.setText(item.authorTime);
                }

                nameView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(item.blogerID)) {
                            LogUtil.log(item.blogerJson);
                            Bloger bloger = Bloger.fromJson(item.blogerJson);
                            if (bloger != null) {
                                BlogerBlogListActivity.startActivity(SearchActivity.this, item.type, bloger);
                                UsageStatsManager.sendUsageData(UsageStatsManager.USAGE_BLOGER_ENTR, "bloglist");
                            }
                        }
                    }
                });

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
        mResultListView.setAdapter(mAdapter);

        mResultListView.setVisibility(View.INVISIBLE);
    }

    private void initListener() {
        mTitleBar.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event!=null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    onSendMsg();
                    return true;
                }
                return false;
            }
        });

        mSearchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onSendMsg();
            }
        });

        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                if (!TextUtils.isEmpty(mInputText)) {
                    loadListData(getSearchUrl(mInputText, mCurrentPage));
                }
            }
        });
    }

    private void onSendMsg() {
        UIUtil.hideKeyboard(mSearchInput);
        mInputText = mSearchInput.getText().toString()
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
            ToastUtil.showMsgS("请输入适当关键字");
        }
    }


    private String getSearchUrl(String keyWord, int page) {
        CSDNHtmlParser parser = CSDNHtmlParser.getInstance();
        return parser.getSearchUrlByKeyword(keyWord, page);
    }

    private void loadListData(String loadUrl) {
        if (mAdapter.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mFooterLayout.setVisibility(View.VISIBLE);
        }

        DataFetcher.getInstance().fetchString(loadUrl, new OnFetchDataListener<Result<String>>() {

            @Override
            public void onFetchFinished(final Result<String> response) {
                mRefreshLayout.setLoading(false);

                if (TextUtils.isEmpty(response.data)) {
                    if (mAdapter.isEmpty()) {
                        // 没有搜索到结果的提示
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

    private static final int MSG_LIST_ADD = 2;
    private Handler.Callback mCallback = new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LIST_ADD:
                    if (mCurrentPage <= 1) {
                        mAdapter.removeAllDatas();
                    }
                    mResultListView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.INVISIBLE);
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
