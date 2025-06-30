// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.verify;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.VerifyListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.verify.FriendVerifyBaseFragment;
import com.netease.yunxin.kit.contactkit.ui.verify.TeamVerifyBaseFragment;
import com.netease.yunxin.kit.contactkit.ui.verify.VerifyBaseActivity;

public class ContactVerifyActivity extends VerifyBaseActivity {

  protected VerifyListActivityLayoutBinding layoutBinding;

  @Override
  public FriendVerifyBaseFragment getFriendFragment() {
    return new FriendVerifyListFragment();
  }

  @Override
  public TeamVerifyBaseFragment getTeamFragment() {
    return new TeamVerifyFragment();
  }

  @Override
  public void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.color_white);
    layoutBinding = VerifyListActivityLayoutBinding.inflate(getLayoutInflater());
    setContentView(layoutBinding.getRoot());
    titleBarView = layoutBinding.title;
    tabLayout = layoutBinding.tabLayout;
    fragmentViewPager = layoutBinding.viewPager;
  }
}
