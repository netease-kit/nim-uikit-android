// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.addfriend;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.addfriend.BaseAddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunAddFriendActivityBinding;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.model.V2UserInfo;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunAddFriendActivity extends BaseAddFriendActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunAddFriendActivityBinding viewBinding =
        FunAddFriendActivityBinding.inflate(getLayoutInflater());
    etAddFriendAccount = viewBinding.etAddFriendAccount;
    ivAddFriendBack = viewBinding.ivAddFriendBack;
    ivFriendClear = viewBinding.ivFriendClear;
    addFriendEmptyLayout = viewBinding.addFriendEmptyLayout;
    return viewBinding.getRoot();
  }

  @Override
  protected void startProfileActivity(V2UserInfo userInfo) {
    if (userInfo == null) {
      return;
    }
    if (TextUtils.equals(userInfo.getAccountId(), IMKitClient.account())) {
      XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE).withContext(this).navigate();
    } else {
      XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
          .withContext(this)
          .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userInfo.getAccountId())
          .navigate();
    }
  }
}
