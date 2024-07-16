// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.databinding.AiUserSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAISelectorFragment;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAIUserSelectorAdapter;

/** AI用户选择器 选择AI数字人 */
public class AIUserSelectorFragment extends BaseAISelectorFragment {

  AiUserSelectorLayoutBinding binding;

  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = AiUserSelectorLayoutBinding.inflate(inflater, container, false);
    recyclerView = binding.recyclerView;
    emptyLayout = binding.emptyLayout;
    searchEmpty = binding.searchEmpty;
    return binding.getRoot();
  }

  @Override
  protected BaseAIUserSelectorAdapter provideAdapter() {
    return new AIUserSelectorAdapter();
  }
}
