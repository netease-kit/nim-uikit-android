// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.adapter.BaseFragmentAdapter;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import java.util.ArrayList;
import java.util.List;

public abstract class VerifyBaseActivity extends BaseLocalActivity {

  public TabLayout tabLayout;
  public ViewPager2 fragmentViewPager;
  public BackTitleBar titleBarView;
  TabLayout.Tab tabFriend;
  TabLayout.Tab tabTeam;

  TabLayoutMediator mediator;

  FriendVerifyBaseFragment friendFragment;
  TeamVerifyBaseFragment teamFragment;

  protected static final String TAG = "VerifyListBaseActivity";

  protected void configViewHolderFactory() {}

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViewAndSetContentView(savedInstanceState);
    bindView();
  }

  public abstract FriendVerifyBaseFragment getFriendFragment();

  public abstract TeamVerifyBaseFragment getTeamFragment();

  public abstract void initViewAndSetContentView(@Nullable Bundle savedInstanceState);

  public void bindView() {

    if (titleBarView != null) {
      titleBarView.setOnBackIconClickListener(v -> onBackPressed());
    }

    List<Fragment> fragments = new ArrayList<>();
    friendFragment = getFriendFragment();
    teamFragment = getTeamFragment();
    fragments.add(friendFragment);
    fragments.add(teamFragment);

    if (tabLayout != null) {
      tabFriend = tabLayout.newTab();
      tabTeam = tabLayout.newTab();
      tabLayout.addTab(tabTeam);
      tabLayout.addTab(tabFriend);
    }

    if (fragmentViewPager != null) {
      BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(this);
      fragmentAdapter.setFragmentList(fragments);
      fragmentViewPager.setAdapter(fragmentAdapter);
    }

    if (fragmentViewPager != null && tabLayout != null) {
      mediator =
          new TabLayoutMediator(
              tabLayout,
              fragmentViewPager,
              (tab, position) -> ALog.d(LIB_TAG, TAG, "onConfigureTab pos = " + position));

      mediator.attach();
    }

    titleBarView
        .setTitle(R.string.verify_msg)
        .setActionText(R.string.clear_all)
        .setActionListener(
            v -> {
              if (tabFriend.isSelected()) {
                friendFragment.clearVerifyList();
              } else if (tabTeam.isSelected()) {
                teamFragment.clearVerifyList();
              }
            });
    tabFriend.setText(getString(R.string.friend_verify_title));
    tabTeam.setText(getString(R.string.team_verify_title));
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (teamFragment != null) {
      teamFragment.clearUnreadCount();
    }
  }
}
