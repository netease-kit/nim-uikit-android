// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.userinfo;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.CommentActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.userinfo.BaseCommentActivity;

public class CommentActivity extends BaseCommentActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    CommentActivityLayoutBinding binding =
        CommentActivityLayoutBinding.inflate(getLayoutInflater());
    titleBar = binding.title;
    titleBar.setActionTextColor(getResources().getColor(R.color.color_337eff));
    edtComment = binding.edtComment;
    return binding.getRoot();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.color_eff1f4);
    super.onCreate(savedInstanceState);
  }
}
