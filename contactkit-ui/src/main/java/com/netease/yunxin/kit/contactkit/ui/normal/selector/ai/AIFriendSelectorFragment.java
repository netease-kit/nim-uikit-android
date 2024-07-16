// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.databinding.AiFriendSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter.FriendSelectorAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAIFriendSelectorFragment;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseFriendSelectorAdapter;

public class AIFriendSelectorFragment extends BaseAIFriendSelectorFragment {

  AiFriendSelectorLayoutBinding binding;

  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = AiFriendSelectorLayoutBinding.inflate(inflater, container, false);
    recyclerView = binding.recyclerView;
    emptyLayout = binding.emptyLayout;
    searchEmpty = binding.searchEmpty;
    return binding.getRoot();
  }

  @Override
  protected BaseFriendSelectorAdapter provideAdapter() {
    FriendSelectorAdapter friendSelectorAdapter = new FriendSelectorAdapter();
    friendSelectorAdapter.setMultiSelectMode(true);
    return friendSelectorAdapter;
  }
}
