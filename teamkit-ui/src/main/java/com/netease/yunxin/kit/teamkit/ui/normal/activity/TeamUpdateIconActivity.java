// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIconActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateIconActivityBinding;

/** set team icon activity */
public class TeamUpdateIconActivity extends BaseTeamUpdateIconActivity {
  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamUpdateIconActivityBinding binding =
        TeamUpdateIconActivityBinding.inflate(getLayoutInflater());
    cancelView = binding.tvCancel;
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
    return true;
  }

  protected BasePhotoChoiceDialog getPhotoChoiceDialog() {
    return new PhotoChoiceDialog(this);
  }
}
