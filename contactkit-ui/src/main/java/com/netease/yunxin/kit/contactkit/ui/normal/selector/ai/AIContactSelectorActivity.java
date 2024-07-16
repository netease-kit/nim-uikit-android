// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.selector.ai;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.AiContactSelectorActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.selector.ai.BaseAIContactSelectorActivity;

public class AIContactSelectorActivity extends BaseAIContactSelectorActivity {

  AiContactSelectorActivityBinding binding;

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    binding = AiContactSelectorActivityBinding.inflate(getLayoutInflater());
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
    AIFriendSelectorFragment friendSelectorFragment = new AIFriendSelectorFragment();
    friendSelectorFragment.setArguments(getIntent().getExtras());
    fragments.add(friendSelectorFragment);
    AIUserSelectorFragment userSelectorFragment = new AIUserSelectorFragment();
    userSelectorFragment.setArguments(getIntent().getExtras());
    fragments.add(userSelectorFragment);
  }

  @Override
  protected void setSelectorAdapter() {
    selectedAdapter = new AISelectedAdapter();
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
