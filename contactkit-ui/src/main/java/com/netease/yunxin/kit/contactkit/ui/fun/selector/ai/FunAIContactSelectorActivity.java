// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.selector.ai;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactAiSelectorActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAIContactSelectorActivity;

public class FunAIContactSelectorActivity extends BaseAIContactSelectorActivity {

  FunContactAiSelectorActivityBinding binding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = FunContactAiSelectorActivityBinding.inflate(getLayoutInflater());
    titleBar = binding.title;
    viewPager = binding.viewPager;
    tabLayout = binding.tabLayout;
    rvSelected = binding.rvSelected;
    selectedLayout = binding.selectedLayout;
    selectedDivider = binding.divider;
    changeStatusBarColor(R.color.color_ededed);
    return binding.getRoot();
  }

  @Override
  protected void initFragments() {
    FunAIFriendSelectorFragment friendSelectorFragment = new FunAIFriendSelectorFragment();
    friendSelectorFragment.setArguments(getIntent().getExtras());
    fragments.add(friendSelectorFragment);
    FunAIUserSelectorFragment userSelectorFragment = new FunAIUserSelectorFragment();
    userSelectorFragment.setArguments(getIntent().getExtras());
    fragments.add(userSelectorFragment);
  }

  @Override
  protected void setSelectorAdapter() {
    selectedAdapter = new FunAISelectedAdapter();
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
