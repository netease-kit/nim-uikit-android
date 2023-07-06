// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIconActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamUpdateIconActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.dialog.FunPhotoChoiceDialog;

/** set team icon activity */
public class FunTeamUpdateIconActivity extends BaseTeamUpdateIconActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamUpdateIconActivityBinding binding =
        FunTeamUpdateIconActivityBinding.inflate(getLayoutInflater());
    cancelView = binding.ivCancel;
    groupPrivilege = binding.groupPrivilege;
    ivIcon = binding.ivIcon;
    ivCamera = binding.ivCamera;
    ivDefault1 = binding.ivDefault1;
    ivDefault2 = binding.ivDefault2;
    ivDefault3 = binding.ivDefault3;
    ivDefault4 = binding.ivDefault4;
    ivDefault5 = binding.ivDefault5;
    tvSave = binding.tvSave;
    return binding.getRoot();
  }

  @Override
  protected boolean isCircle() {
    return false;
  }

  protected BasePhotoChoiceDialog getPhotoChoiceDialog() {
    return new FunPhotoChoiceDialog(this);
  }
}
