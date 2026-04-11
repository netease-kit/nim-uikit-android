// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.robot;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.RobotEditLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotCreateActivity;

/** 协同版机器人创建页 */
public class RobotCreateActivity extends BaseRobotCreateActivity {

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.normal_page_bg_second_color);
    RobotEditLayoutBinding b = RobotEditLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    avatarView = b.avatarView;
    tvName = b.tvName;
    rlyAvatar = b.rlyAvatar;
    rlyName = b.rlyName;
    tvSave = b.tvSave;
  }

  @Override
  protected Class<?> getNameEditActivityClass() {
    return RobotEditNameActivity.class;
  }
}
