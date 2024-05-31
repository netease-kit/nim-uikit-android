// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.forward;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunForwardContactSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.adapter.FunRecentForwardAdapter;
import com.netease.yunxin.kit.contactkit.ui.fun.selector.forward.adapter.FunSelectedAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseForwardSelectorActivity;

public class FunForwardSelectorActivity extends BaseForwardSelectorActivity {

  FunForwardContactSelectorLayoutBinding binding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = FunForwardContactSelectorLayoutBinding.inflate(getLayoutInflater());
    titleBar = binding.title;
    viewPager = binding.viewPager;
    searchEditText = binding.searchEt;
    tabLayout = binding.tabLayout;
    recentForwardRecyclerView = binding.recyclerView;
    recentForwardLayout = binding.recentForwardLayout;
    rvSelected = binding.rvSelected;
    selectedLayout = binding.selectedLayout;
    ivSelectedDetail = binding.ivDetail;
    selectedDivider = binding.divider;
    ivSearchClear = binding.clearIv;
    changeStatusBarColor(R.color.color_ededed);
    return binding.getRoot();
  }

  @Override
  protected void initFragments() {
    fragments.add(new FunConversationSelectorFragment());
    fragments.add(new FunFriendSelectorFragment());
    if (IMKitConfigCenter.getTeamEnable()) {
      fragments.add(new FunTeamSelectorFragment());
    }
  }

  @Override
  protected void showSelectedDetail() {
    // show selected detail
    FunSelectedListDialog dialog = new FunSelectedListDialog();
    dialog.show(getSupportFragmentManager(), FunSelectedListDialog.TAG);
  }

  @Override
  protected void setSelectorAdapter() {
    recentForwardSelectorAdapter = new FunRecentForwardAdapter();
    selectedAdapter = new FunSelectedAdapter();
  }

  @Override
  protected void setTitleBarMultiSelectActionUI() {
    titleBar
        .setActionText(getString(R.string.selector_finish_without_number))
        .setActionBackgroundRes(R.drawable.fun_selector_finish_bg)
        .setActionTextColor(getResources().getColor(R.color.color_white));
    titleBar
        .getActionTextView()
        .setPadding(
            SizeUtils.dp2px(13), SizeUtils.dp2px(7), SizeUtils.dp2px(13), SizeUtils.dp2px(7));
  }

  @Override
  protected void setTitleBarActionNumber(int number) {
    if (!isMultiSelect) {
      return;
    }
    if (number > 0) {
      titleBar.setActionText(getString(R.string.selector_finish, number));
      titleBar.setActionEnable(true);
    } else {
      titleBar.setActionText(getString(R.string.selector_finish_without_number));
      titleBar.setActionEnable(false);
    }
  }
}
