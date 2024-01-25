// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatUserItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatUserListLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.ReadUserViewHolder;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatReaderBaseFragment;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;

/** 标准皮肤，消息已读未读成员列表页面Fragment。 */
public class ChatReaderFragment extends ChatReaderBaseFragment {
  ChatUserListLayoutBinding binding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = ChatUserListLayoutBinding.inflate(inflater, container, false);
    userRv = binding.recyclerView;
    emptyView = binding.llyEmpty;
    emptyTv = binding.tvAllState;
    return binding.getRoot();
  }

  @Override
  public IViewHolderFactory getViewHolderFactory() {
    return (parent, viewType) -> {
      ChatUserItemLayoutBinding binding =
          ChatUserItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ReadUserViewHolder(binding);
    };
  }
}
