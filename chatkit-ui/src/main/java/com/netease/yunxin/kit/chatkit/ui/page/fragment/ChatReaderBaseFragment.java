// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatReadUserBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.ChatUserBaseAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatReadStateViewModel;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import java.util.List;

/** chat message read state page */
public abstract class ChatReaderBaseFragment extends BaseFragment {

  public RecyclerView userRv;

  public View emptyView;

  public TextView emptyTv;

  public ChatUserBaseAdapter adapter;

  public static final String ACK_KEY = "is_ack_list";

  public static final String TID_KEY = "team_id";

  /** true for ack list false for unack list */
  public boolean ack;

  public String tid;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = initViewAndGetRootView(inflater, container);
    bindView();
    initData();
    return rootView;
  }

  //初始化View 并返回布局的RootView
  public abstract View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container);

  public void bindView() {
    if (userRv != null) {
      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      userRv.setLayoutManager(layoutManager);
      adapter = new ChatUserBaseAdapter();
      userRv.setAdapter(adapter);
      adapter.setViewHolderFactory(getViewHolderFactory());
    }
  }

  public void initData() {
    if (getArguments() != null) {
      ack = getArguments().getBoolean(ACK_KEY);
      tid = getArguments().getString(TID_KEY);
      ChatReadStateViewModel viewModel =
          new ViewModelProvider(requireActivity()).get(ChatReadStateViewModel.class);
      viewModel
          .getTeamAckInfoLiveData()
          .observe(
              getViewLifecycleOwner(),
              teamMsgAckInfo -> {
                if (ack) {
                  if (teamMsgAckInfo.getAckAccountList().isEmpty()) {
                    if (emptyView != null) {
                      emptyView.setVisibility(View.VISIBLE);
                    }
                    if (emptyTv != null) {
                      emptyTv.setText(R.string.chat_all_user_have_unread);
                    }
                  } else if (adapter != null) {
                    List<ChatReadUserBean> dataList =
                        ChatReadUserBean.generateChatSearchBean(
                            tid, teamMsgAckInfo.getAckUserInfoList());
                    adapter.appendData(dataList);
                  }
                } else {
                  if (teamMsgAckInfo.getUnAckAccountList().isEmpty()) {
                    if (emptyView != null) {
                      emptyView.setVisibility(View.VISIBLE);
                    }
                    if (emptyTv != null) {
                      emptyTv.setText(R.string.chat_all_user_have_read);
                    }
                  } else if (adapter != null) {
                    List<ChatReadUserBean> dataList =
                        ChatReadUserBean.generateChatSearchBean(
                            tid, teamMsgAckInfo.getUnAckUserInfoList());
                    adapter.appendData(dataList);
                  }
                }
              });
    }
  }

  public abstract IViewHolderFactory getViewHolderFactory();
}
