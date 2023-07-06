// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.addfriend;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.addfriend.BaseAddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.AddFriendActivityBinding;

public class AddFriendActivity extends BaseAddFriendActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    AddFriendActivityBinding viewBinding = AddFriendActivityBinding.inflate(getLayoutInflater());
    etAddFriendAccount = viewBinding.etAddFriendAccount;
    ivAddFriendBack = viewBinding.ivAddFriendBack;
    ivFriendClear = viewBinding.ivFriendClear;
    addFriendEmptyLayout = viewBinding.addFriendEmptyLayout;
    return viewBinding.getRoot();
  }
}
