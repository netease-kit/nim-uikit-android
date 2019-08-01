package com.netease.nim.uikit.common.ui.ptr2;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;


/**
 * 下拉刷新控件，可以配合 RecyclerView，Scrollview，ListView
 * Created by fish on 16/5/17.
 */
public class PullToRefreshLayout extends SuperSwipeRefreshLayout {

    public interface OnRefreshListener {
        void onPullDownToRefresh();

        void onPullUpToRefresh();
    }

    private CustomLoadingLayout loadingLayoutDown;
    private CustomLoadingLayout loadingLayoutUp;
    private OnRefreshListener listener;

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }


    public PullToRefreshLayout(Context context) {
        super(context);
        initLoadingView(true, true);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLoadingView(true, true);
    }

    //一般用于进页面第一次刷新
    public void autoRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(true);
                loadingLayoutDown.refreshing();
                if (listener != null) {
                    listener.onPullDownToRefresh();
                }
            }
        }, 100);
    }

    public void initLoadingView(boolean pullDown, boolean pullUp) {
        if (pullDown) {
            loadingLayoutDown = new CustomLoadingLayout(getContext());
            setHeaderView(loadingLayoutDown);
            setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                @Override
                public void onRefresh() {
                    loadingLayoutDown.refreshing();
                    if (listener != null) {
                        listener.onPullDownToRefresh();
                    }
                }

                @Override
                public void onPullDistance(int distance) {
                    if (distance == 0) {
                        loadingLayoutDown.reset();
                    }
                    loadingLayoutDown.onPull(distance * 1.0f / loadingLayoutDown.getContentSize());
                }

                @Override
                public void onPullEnable(boolean enable) {
//                    textView.setText(enable ? "松开刷新" : "下拉刷新");
                }
            });
        }

        if (pullUp) {
            loadingLayoutUp = new CustomLoadingLayout(getContext());
            setFooterView(loadingLayoutUp);
            setOnPushLoadMoreListener(new OnPushLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    loadingLayoutUp.refreshing();
                    if (listener != null) {
                        listener.onPullUpToRefresh();
                    }
                }

                @Override
                public void onPushDistance(int distance) {
                    if (distance == 0) {
                        loadingLayoutUp.reset();
                    }
                    loadingLayoutUp.onPull(distance * 1.0f / loadingLayoutUp.getContentSize());
                }

                @Override
                public void onPushEnable(boolean enable) {

                }
            });
        }

    }

}
