package com.brian.common.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by huamm on 2016/8/9 0009.
 */
public class RefreshLayout extends SwipeRefreshLayout {

    private final int mTouchSlop;
    private ListView mListView;
    private OnLoadListener mOnLoadListener;

    private float firstTouchY;
    private float lastTouchY;

    private boolean isLoading = false;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    //set the child view of RefreshLayout,ListView
    public void setChildView(ListView mListView) {
        this.mListView = mListView;
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean isLastRow = false;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //正在滚动时回调，回调2-3次，手指没抛则回调2次。scrollState = 2的这次不回调
                //回调顺序如下
                //第1次：scrollState = SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
                //第2次：scrollState = SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
                //第3次：scrollState = SCROLL_STATE_IDLE(0) 停止滚动
                //当屏幕停止滚动时为0；当屏幕滚动且用户使用的触碰或手指还在屏幕上时为1；
                //由于用户的操作，屏幕产生惯性滑动时为2

                //当滚到最后一行且停止滚动时，执行加载
                if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (canLoadMore()) {
                        loadData();
                    }
                    isLastRow = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //滚动时一直回调，直到停止滚动时才停止回调。单击时回调一次。
                //firstVisibleItem：当前能看见的第一个列表项ID（从0开始）
                //visibleItemCount：当前能看见的列表项个数（小半个也算）
                //totalItemCount：列表项共数

                //判断是否滚到最后一行
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                    isLastRow = true;
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                firstTouchY = event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                lastTouchY = event.getRawY();
                if (canLoadMore()) {
                    loadData();
                }
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    private boolean canLoadMore() {
        return !isLoading && isBottom() && isPullingUp();
    }

    private boolean isBottom() {
        if (mListView.getCount() > 0) {
            if (mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1 &&
                    mListView.getChildAt(mListView.getChildCount() - 1).getBottom() <= mListView.getHeight()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPullingUp() {
        return (firstTouchY - lastTouchY) >= mTouchSlop;
    }

    private void loadData() {
        if (mOnLoadListener != null) {
            setLoading(true);
        }
    }

    public void setLoading(boolean loading) {
        if (mListView == null) return;
        isLoading = loading;
        if (loading) {
            if (isRefreshing()) {
                setRefreshing(false);
            }
            mListView.setSelection(mListView.getAdapter().getCount() - 1);
            mOnLoadListener.onLoad();
        } else {
            firstTouchY = 0;
            lastTouchY = 0;
        }
    }

    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    public interface OnLoadListener {
        void onLoad();
    }

}
