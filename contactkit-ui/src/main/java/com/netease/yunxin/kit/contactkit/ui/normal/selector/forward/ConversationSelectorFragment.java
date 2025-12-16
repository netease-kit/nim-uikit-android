// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward;

import static com.netease.yunxin.kit.contactkit.ui.utils.TextUtils.getSelectSpanText;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.coexist.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardConversationSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter.ConversationSelectorAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseConversationSelectorFragment;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseConversationSelectorAdapter;

public class ConversationSelectorFragment extends BaseConversationSelectorFragment {

  ForwardConversationSelectorLayoutBinding binding;

  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = ForwardConversationSelectorLayoutBinding.inflate(inflater, container, false);
    recyclerView = binding.recyclerView;
    searchEmpty = binding.searchEmpty;
    emptyLayout = binding.emptyLayout;
    return binding.getRoot();
  }

  @Override
  protected BaseConversationSelectorAdapter provideAdapter() {
    return new ConversationSelectorAdapter();
  }

  @Override
  protected void showSearchResultEmptyView() {
    String showText = getString(R.string.global_search_no_result, viewModel.getSearchKey());
    int start = showText.indexOf(viewModel.getSearchKey());
    int end = start + viewModel.getSearchKey().length();
    RecordHitInfo recordHitInfo = new RecordHitInfo(start, end);
    binding.tvNoResult.setText(
        getSelectSpanText(getResources().getColor(R.color.color_337eff), showText, recordHitInfo));
    super.showSearchResultEmptyView();
  }
}
