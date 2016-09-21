
package com.brian.csdnblog.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brian.common.view.CircleImageView;
import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.BlogerManager;
import com.brian.csdnblog.manager.Constants;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.Bloger;
import com.brian.csdnblog.model.event.CurrBlogerEvent;
import com.brian.csdnblog.util.LogUtil;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 侧边栏
 */
public class SidePageFragment extends Fragment implements OnClickListener {

    @BindView(R.id.bloger)              View mBlogerLy; // 博主
    @BindView(R.id.bloger_head)         CircleImageView mBlogerHeadView; // 博主头像
    @BindView(R.id.bloger_name)         TextView mBlogerNameView; // 博主名
    @BindView(R.id.select_article_type) View mSelectTypeLy; // 设置类型
    @BindView(R.id.blog_history)        View mHistoryLy;
    @BindView(R.id.blog_favo)           View mFavoLy;
    @BindView(R.id.news)                View mNewsLy;
    @BindView(R.id.aboutView)           View mAboutLy;
    @BindView(R.id.settings)            View mSettingsLy;
    @BindView(R.id.chat)                View viewChat;
    @BindView(R.id.tv_select_type)      TextView mSelectTypeView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        LogUtil.i("onActivityCreated");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide_menu, null);
        ButterKnife.bind(this, view);
        initUI();

        return view;
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        mSelectTypeView.setText(TypeManager.getCurrCateName());

        mBlogerLy.setOnClickListener(this);
        mSelectTypeLy.setOnClickListener(this);
        mFavoLy.setOnClickListener(this);
        mNewsLy.setOnClickListener(this);
        mHistoryLy.setOnClickListener(this);
        mAboutLy.setOnClickListener(this);
        mSettingsLy.setOnClickListener(this);
        viewChat.setOnClickListener(this);

        Bloger bloger = BlogerManager.getsInstance().getCurrBloger();
        mBlogerNameView.setText(bloger.nickName + "的博客");
        LogUtil.log("headUrl=" + bloger.headUrl);
        Picasso.with(Env.getContext()).load(bloger.headUrl).into(mBlogerHeadView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bloger: // 博主博文列表入口
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "bloger");
                Bloger bloger = BlogerManager.getsInstance().getCurrBloger();
                BlogerBlogListActivity.startActivity(getActivity(), bloger.blogerType, bloger);
                break;
            case R.id.aboutView: // 关于
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "about");
                AboutActivity.startActivity(getActivity());
                break;
            case R.id.settings: // 设置
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "setting");
                SettingActivity.startActivity(getActivity());
                break;
            case R.id.chat: // 聊天
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "chat");
                ChatActivity.startActivity(getActivity());
                break;
            case R.id.blog_favo: // 收藏的文章
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "favo");
                BlogListActivity.startActivity(getActivity(), BlogListActivity.TYPE_FAVO);
                break;
            case R.id.news: // 新闻
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "news");
                BlogListActivity.startActivity(getActivity(), BlogListActivity.TYPE_NEWS);
                break;
            case R.id.blog_history: // 查看过的文章
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "history");
                BlogListActivity.startActivity(getActivity(), BlogListActivity.TYPE_HISTORY);
                break;
            case R.id.select_article_type: // 设置文章类型
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "articletype");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("选择文章类型");

                // 设置一个下拉的列表选择项
                builder.setItems(Constants.TYPES_WORD, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TypeManager.setCateType(which);
                        mSelectTypeView.setText(TypeManager.getCurrCateName());
                    }
                });
                builder.show();
                break;
            default:
                UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "unknow");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i("onActivityResult");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i("onResume");
        MobclickAgent.onPageStart(this.getClass().getName()); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.i("onPause");
        MobclickAgent.onPageEnd(this.getClass().getName());
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * EventBus回调
     */
    @Subscribe
    public void onEventMainThread(CurrBlogerEvent event) {
        mBlogerNameView.setText(event.bloger.nickName + "的博客");
        LogUtil.log("headUrl=" + event.bloger.headUrl);
        Picasso.with(Env.getContext()).load(event.bloger.headUrl).into(mBlogerHeadView);
    }

}
