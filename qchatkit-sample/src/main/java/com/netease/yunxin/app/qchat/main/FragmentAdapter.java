// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

/** Fragment Adapter used in MainActivity */
public class FragmentAdapter extends FragmentStateAdapter {
  private static final String TAG = FragmentAdapter.class.getSimpleName();

  private List<Fragment> fragmentList;

  public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  public FragmentAdapter(@NonNull Fragment fragment) {
    super(fragment);
  }

  public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
    super(fragmentManager, lifecycle);
  }

  public void setFragmentList(List<Fragment> fragmentList) {
    this.fragmentList = fragmentList;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    if (fragmentList == null || fragmentList.size() <= position) {
      return new Fragment();
    }
    return fragmentList.get(position);
  }

  @Override
  public int getItemCount() {
    return fragmentList == null ? 0 : fragmentList.size();
  }
}
