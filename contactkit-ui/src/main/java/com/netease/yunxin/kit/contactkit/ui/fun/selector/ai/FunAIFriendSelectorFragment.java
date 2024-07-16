// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunAiFriendSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.adapter.FunFriendSelectorAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAIFriendSelectorFragment;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseFriendSelectorAdapter;

/** AI好友选择器 选择好友 */
public class FunAIFriendSelectorFragment extends BaseAIFriendSelectorFragment {

  FunAiFriendSelectorLayoutBinding binding;

  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FunAiFriendSelectorLayoutBinding.inflate(inflater, container, false);
    recyclerView = binding.recyclerView;
    emptyLayout = binding.emptyLayout;
    searchEmpty = binding.searchEmpty;
    return binding.getRoot();
  }

  @Override
  protected BaseFriendSelectorAdapter provideAdapter() {
    return new FunFriendSelectorAdapter();
  }
}
