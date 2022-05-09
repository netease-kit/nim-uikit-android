/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.page.interfaces.ILoadListener;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.conversationkit.ui.common.XLog;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

import java.util.Comparator;
import java.util.List;

/**
 * conversation list view
 */
public class ConversationView extends FrameLayout {

    private final String TAG = "ConversationView";
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private ILoadListener loadMoreListener;
    private final int LOAD_MORE_DIFF = 5;

    public ConversationView(Context context) {
        super(context);
        init(null);
    }

    public ConversationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ConversationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        recyclerView = new RecyclerView(getContext());
        this.addView(recyclerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new ConversationAdapter();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = layoutManager.findLastVisibleItemPosition();
                    if (loadMoreListener != null && loadMoreListener.hasMore() && adapter.getItemCount() < position + LOAD_MORE_DIFF) {
                        ConversationBean last = adapter.getData(adapter.getItemCount() - 1);
                        loadMoreListener.loadMore(last);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void setLoadMoreListener(ILoadListener listener) {
        this.loadMoreListener = listener;
    }

    public void setItemClickListener(ViewHolderClickListener listener) {
        adapter.setViewHolderClickListener(listener);
    }

    public void setViewHolderFactory(IViewHolderFactory factory) {
        adapter.setViewHolderFactory(factory);
    }

    public void setComparator(Comparator<ConversationInfo> comparator) {
        this.adapter.setComparator(comparator);
    }

    public void setData(List<ConversationBean> data) {
        if (adapter != null) {
            adapter.setData(data);
        }
    }

    public void addData(List<ConversationBean> data) {
        if (adapter != null) {
            adapter.appendData(data);
        }
    }

    public void update(List<ConversationBean> data) {
        if (adapter != null) {
            XLog.d(TAG, "update ConversationBean list", "start");
            adapter.update(data);
            XLog.d(TAG, "update ConversationBean list", "end");
        }
    }

    public void update(ConversationBean data) {
        if (adapter != null) {
            XLog.d(TAG, "update ConversationBean", "start");
            adapter.update(data);
            XLog.d(TAG, "update ConversationBean", "end");
        }
    }

    public void updateUserInfo(List<UserInfo> data) {
        if (adapter != null) {
            adapter.updateUserInfo(data);
        }
    }

    public void updateFriendInfo(List<FriendInfo> data) {
        if (adapter != null) {
            adapter.updateFriendInfo(data);
        }
    }

    public void updateTeamInfo(List<Team> data) {
        if (adapter != null) {
            adapter.updateTeamInfo(data);
        }
    }

    public void updateMuteInfo(MuteListChangedNotify changedNotify){
        if (adapter != null) {
            adapter.updateMuteInfo(changedNotify);
        }
    }

    public void remove(List<ConversationBean> data) {
        if (adapter != null) {
            adapter.removeData(data);
        }
    }

    public void removeAll() {
        if (adapter != null) {
            adapter.removeAll();
        }
    }

    public void removeConversation(String id){
        if (adapter != null) {
            adapter.removeData(id);
        }
    }

    public void addStickTop(String id){
        if (adapter != null) {
            adapter.addStickTop(id);
        }
    }

    public void removeStickTop(String id){
        if (adapter != null) {
            adapter.removeStickTop(id);
        }
    }
}
