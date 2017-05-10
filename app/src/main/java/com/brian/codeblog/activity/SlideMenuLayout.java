
package com.brian.codeblog.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.brian.codeblog.manager.BlogerManager;
import com.brian.codeblog.manager.Constants;
import com.brian.codeblog.manager.TypeManager;
import com.brian.codeblog.model.Bloger;
import com.brian.codeblog.model.event.CurrBlogerEvent;
import com.brian.codeblog.pay.PayHelper;
import com.brian.codeblog.stat.UsageStatsManager;
import com.brian.common.tools.Env;
import com.brian.common.utils.LogUtil;
import com.brian.common.utils.ToastUtil;
import com.brian.common.view.CircleImageView;
import com.brian.csdnblog.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 侧边栏
 */
public class SlideMenuLayout extends FrameLayout implements OnClickListener {

    @BindView(R.id.bloger)              View mBlogerLy; // 博主
    @BindView(R.id.bloger_head)         CircleImageView mBlogerHeadView; // 博主头像
    @BindView(R.id.bloger_name)         TextView mBlogerNameView; // 博主名
    @BindView(R.id.select_article_type) TextView mSelectTypeView; // 设置类型
    @BindView(R.id.blog_history)        View mHistoryLy;
    @BindView(R.id.blog_favo)           View mFavoLy;
    @BindView(R.id.news)                View mNewsLy;
    @BindView(R.id.payback)             View mPaybackLy;
    @BindView(R.id.aboutView)           View mAboutLy;
    @BindView(R.id.settings)            View mSettingsLy;
    @BindView(R.id.chat)                View mChatLy;

    public SlideMenuLayout(Context context) {
        this(context, null, 0);
    }

    public SlideMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(BaseActivity.getTopActivity()).inflate(R.layout.slide_menu, this);
        ButterKnife.bind(this, view);
        initUI();
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        mSelectTypeView.setText(TypeManager.getCurrCateName());

        mBlogerLy.setOnClickListener(this);
        mSelectTypeView.setOnClickListener(this);
        mFavoLy.setOnClickListener(this);
        mNewsLy.setOnClickListener(this);
        mHistoryLy.setOnClickListener(this);
        mAboutLy.setOnClickListener(this);
        mSettingsLy.setOnClickListener(this);
        mChatLy.setOnClickListener(this);
        mPaybackLy.setOnClickListener(this);

        Bloger bloger = BlogerManager.getsInstance().getCurrBloger();
        mBlogerNameView.setText(bloger.nickName);
        LogUtil.log("headUrl=" + bloger.headUrl);
        if (TextUtils.isEmpty(bloger.headUrl)) {
            mBlogerHeadView.setImageResource(R.drawable.ic_default_user);
        } else {
            Picasso.with(Env.getContext()).load(bloger.headUrl).into(mBlogerHeadView);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bloger: // 博主博文列表入口
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "bloger");
                Bloger bloger = BlogerManager.getsInstance().getCurrBloger();
                BlogerBlogListActivity.startActivity(getActivity(), bloger.blogerType, bloger);
                break;
            case R.id.aboutView: // 关于
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "about");
                AboutActivity.startActivity(getActivity());
                break;
            case R.id.settings: // 设置
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "setting");
                SettingActivity.startActivity(getActivity());
                break;
            case R.id.chat: // 聊天
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "chat");
                ChatActivity.startActivity(getActivity());
                break;
            case R.id.payback: // 打赏
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "payback");
                PayHelper.pay("打赏作者", new PayHelper.IPayListener() {
                    @Override
                    public void onResult(boolean isOK) {
                        if (!isOK) {
                            ToastUtil.showMsg("打赏未完成");
                        }
                    }
                });
                break;
            case R.id.blog_favo: // 收藏的文章
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "favo");
                BlogListActivity.startActivity(getActivity(), BlogListActivity.TYPE_FAVO);
                break;
            case R.id.news: // 新闻
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "news");
                BlogListActivity.startActivity(getActivity(), BlogListActivity.TYPE_NEWS);
                break;
            case R.id.blog_history: // 查看过的文章
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "history");
                BlogListActivity.startActivity(getActivity(), BlogListActivity.TYPE_HISTORY);
                break;
            case R.id.select_article_type: // 设置文章类型
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "articletype");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                UsageStatsManager.reportData(UsageStatsManager.MENU_LIST, "unknow");
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    /**
     * EventBus回调
     */
    @Subscribe
    public void onEventMainThread(CurrBlogerEvent event) {
        mBlogerNameView.setText(event.bloger.nickName);
        LogUtil.log("headUrl=" + event.bloger.headUrl);
        if (TextUtils.isEmpty(event.bloger.headUrl)) {
            mBlogerHeadView.setImageResource(R.drawable.ic_default_user);
        } else {
            Picasso.with(Env.getContext()).load(event.bloger.headUrl).into(mBlogerHeadView);
        }
    }

    public Activity getActivity() {
        return BaseActivity.getTopActivity();
    }
}
