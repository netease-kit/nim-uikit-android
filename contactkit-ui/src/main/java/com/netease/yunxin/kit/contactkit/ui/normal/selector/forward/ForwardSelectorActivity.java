// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.forward;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ForwardContactSelectorLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter.RecentForwardAdapter;
import com.netease.yunxin.kit.contactkit.ui.normal.selector.forward.adapter.SelectedAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.BaseForwardSelectorActivity;

public class ForwardSelectorActivity extends BaseForwardSelectorActivity {

  ForwardContactSelectorLayoutBinding binding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = ForwardContactSelectorLayoutBinding.inflate(getLayoutInflater());
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
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    super.initView();
  }

  @Override
  protected void showSelectedDetail() {
    SelectedListDialog dialog = new SelectedListDialog();
    dialog.show(getSupportFragmentManager(), SelectedListDialog.TAG);
  }

  @Override
  protected void setSelectorAdapter() {
    recentForwardSelectorAdapter = new RecentForwardAdapter();
    selectedAdapter = new SelectedAdapter();
  }

  @Override
  protected void setTitleBarActionNumber(int number) {
    if (!isMultiSelect) {
      return;
    }
    if (number > 0) {
      titleBar.setActionText(getString(R.string.selector_sure, number));
      titleBar.setActionEnable(true);
    } else {
      titleBar.setActionText(getString(R.string.selector_sure_without_num));
      titleBar.setActionEnable(false);
    }
  }

  @Override
  protected void initFragments() {
    fragments.add(new ConversationSelectorFragment());
    fragments.add(new FriendSelectorFragment());
    if (IMKitConfigCenter.getTeamEnable()) {
      fragments.add(new TeamSelectorFragment());
    }
  }
}
