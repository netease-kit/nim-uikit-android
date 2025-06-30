// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.verify;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.VerifyListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.verify.FriendVerifyBaseFragment;
import com.netease.yunxin.kit.contactkit.ui.verify.TeamVerifyBaseFragment;
import com.netease.yunxin.kit.contactkit.ui.verify.VerifyBaseActivity;

public class FunContactVerifyActivity extends VerifyBaseActivity {

  protected VerifyListActivityLayoutBinding layoutBinding;

  @Override
  public FriendVerifyBaseFragment getFriendFragment() {
    return new FunFriendVerifyFragment();
  }

  @Override
  public TeamVerifyBaseFragment getTeamFragment() {
    return new FunTeamVerifyFragment();
  }

  @Override
  public void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.color_ededed);
    layoutBinding = VerifyListActivityLayoutBinding.inflate(getLayoutInflater());
    setContentView(layoutBinding.getRoot());
    layoutBinding.title.getTitleTextView().setTextSize(17);
    layoutBinding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    layoutBinding.title.setBackgroundResource(R.color.color_ededed);
    titleBarView = layoutBinding.title;
    tabLayout = layoutBinding.tabLayout;
    fragmentViewPager = layoutBinding.viewPager;
  }
}
