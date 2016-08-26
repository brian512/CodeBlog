
package com.brian.csdnblog.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.brian.csdnblog.Env;
import com.brian.csdnblog.R;
import com.brian.csdnblog.manager.Constants;
import com.brian.csdnblog.manager.TypeManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.util.LogUtil;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.market.sdk.UpdateResponse;
import com.xiaomi.market.sdk.UpdateStatus;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.market.sdk.XiaomiUpdateListener;

import butterknife.ButterKnife;

/**
 * 个人中心
 */
public class PersonCenterFragment extends Fragment implements OnClickListener {

    private static final String TAG = "CSNDBlog_PersonCenterFragment";

    private View viewCheckUpdate; // 检查更新
    private View viewSelectType; // 设置类型
    private View viewHistory;
    private View viewFavo;
    private View viewNews;
    private View viewAbout;
    private View viewSettings;
    private View viewChat;

    private TextView tvSelectType = null;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.i(TAG, "onActivityCreated");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_center, null);
        ButterKnife.bind(this, view);
        initComponent(view);

        return view;
    }

    /**
     * 查找控件
     * 
     * @param view
     */
    private void initComponent(View view) {
        viewSelectType = view.findViewById(R.id.select_article_type);
        viewCheckUpdate = view.findViewById(R.id.checkUpdateView);
        viewHistory = view.findViewById(R.id.blog_history);
        viewFavo = view.findViewById(R.id.blog_favo);
        viewNews = view.findViewById(R.id.news);
        viewAbout = view.findViewById(R.id.aboutView);
        viewSettings = view.findViewById(R.id.settings);
        viewChat = view.findViewById(R.id.chat);

        tvSelectType = (TextView) view.findViewById(R.id.tv_select_type);
        tvSelectType.setText(TypeManager.getCurrCateName());

        viewSelectType.setOnClickListener(this);
        viewCheckUpdate.setOnClickListener(this);
        viewFavo.setOnClickListener(this);
        viewNews.setOnClickListener(this);
        viewHistory.setOnClickListener(this);
        viewAbout.setOnClickListener(this);
        viewSettings.setOnClickListener(this);
        viewChat.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
        case R.id.checkUpdateView: // 检测更新
            UsageStatsManager.sendUsageData(UsageStatsManager.MENU_LIST, "update");
            checkUpdate(Env.getContext());
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
                    	tvSelectType.setText(TypeManager.getCurrCateName());
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
        LogUtil.i(TAG, "onActivityResult");
    }
    
    private void checkUpdate(final Context context) {
        XiaomiUpdateAgent.setUpdateAutoPopup(false);
        XiaomiUpdateAgent.setUpdateListener(new XiaomiUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                case UpdateStatus.STATUS_UPDATE: // has update
                    XiaomiUpdateAgent.arrange();
                    break;
                case UpdateStatus.STATUS_NO_UPDATE: // has no update
                    Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                    break;
                case UpdateStatus.STATUS_NO_WIFI: // none wifi
                    Toast.makeText(context, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
                    break;
                case UpdateStatus.STATUS_NO_NET: // time out
                case UpdateStatus.STATUS_FAILED: // time out
                case UpdateStatus.STATUS_LOCAL_APP_FAILED: // time out
                    Toast.makeText(context, "服务器访问超时", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        });
        XiaomiUpdateAgent.update(context);
    }
    

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume");
        MobclickAgent.onPageStart(this.getClass().getName()); //统计页面
    }
    
    @Override
    public void onPause() {
        super.onPause();
        LogUtil.i(TAG, "onPause");
        MobclickAgent.onPageEnd(this.getClass().getName());
    }

}
