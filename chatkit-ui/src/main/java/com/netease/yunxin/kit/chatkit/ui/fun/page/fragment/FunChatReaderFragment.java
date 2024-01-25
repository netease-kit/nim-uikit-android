// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatReaderFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatReaderViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.FunReaderViewHolder;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatReaderBaseFragment;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.common.utils.SizeUtils;

/** Fun皮肤消息已读未读页面，包含已读和未读两个tab Fun皮肤差异化的部分抽象到FunChatReaderFragment中 */
public class FunChatReaderFragment extends ChatReaderBaseFragment {
  FunChatReaderFragmentBinding binding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FunChatReaderFragmentBinding.inflate(inflater, container, false);
    userRv = binding.recyclerView;
    emptyView = binding.emptyLayout;
    emptyTv = binding.stateTv;
    binding.recyclerView.addItemDecoration(getItemDecoration());
    return binding.getRoot();
  }

  @Override
  public IViewHolderFactory getViewHolderFactory() {
    return (parent, viewType) -> {
      FunChatReaderViewHolderBinding binding =
          FunChatReaderViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new FunReaderViewHolder(binding);
    };
  }

  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(1);
      final int leftPadding = SizeUtils.dp2px(16);

      @Override
      public void getItemOffsets(
          @NonNull Rect outRect,
          @NonNull View view,
          @NonNull RecyclerView parent,
          @NonNull RecyclerView.State state) {
        outRect.set(leftPadding, topPadding, 0, 0);
      }
    };
  }
}
