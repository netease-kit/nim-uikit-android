// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.forward;

import static com.netease.yunxin.kit.contactkit.ui.utils.TextUtils.getSelectSpanText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.coexist.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunForwardConversationSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.adapter.FunConversationSelectorAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseConversationSelectorFragment;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseConversationSelectorAdapter;

public class FunConversationSelectorFragment extends BaseConversationSelectorFragment {
  FunForwardConversationSelectorLayoutBinding binding;

  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FunForwardConversationSelectorLayoutBinding.inflate(inflater, container, false);
    recyclerView = binding.recyclerView;
    searchEmpty = binding.searchEmpty;
    emptyLayout = binding.emptyLayout;
    return binding.getRoot();
  }

  @Override
  protected BaseConversationSelectorAdapter provideAdapter() {
    return new FunConversationSelectorAdapter();
  }

  @Override
  protected void showSearchResultEmptyView() {
    String showText = getString(R.string.global_search_no_result, viewModel.getSearchKey());
    int start = showText.indexOf(viewModel.getSearchKey());
    int end = start + viewModel.getSearchKey().length();
    RecordHitInfo recordHitInfo = new RecordHitInfo(start, end);
    binding.tvNoResult.setText(
        getSelectSpanText(getResources().getColor(R.color.color_58be6b), showText, recordHitInfo));
    super.showSearchResultEmptyView();
  }
}
