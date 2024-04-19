// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.userinfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.databinding.UserInfoActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.userinfo.BaseUserInfoActivity;

public class UserInfoActivity extends BaseUserInfoActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    UserInfoActivityLayoutBinding binding =
        UserInfoActivityLayoutBinding.inflate(getLayoutInflater());
    contactInfoView = binding.contactUser;
    contactInfoView.configSignatureMaxLines(2);
    titleBar = binding.title;
    return binding.getRoot();
  }

  @Override
  protected Class<? extends Activity> getCommentActivity() {
    return CommentActivity.class;
  }
}
