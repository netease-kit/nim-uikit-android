// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.robot;

import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.RobotInfoLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotInfoActivity;

/** 协同版机器人信息页面 */
public class RobotInfoActivity extends BaseRobotInfoActivity {

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.normal_page_bg_second_color);
    RobotInfoLayoutBinding b = RobotInfoLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    avatarView = b.avatarView;
    tvRobotName = b.tvRobotName;
    rlyEdit = b.rlyEdit;
    rlyAvatarName = b.rlyAvatarName;
    rlyViewConfig = b.rlyViewConfig;
    rlyRefreshToken = b.rlyRefreshToken;
    tvChat = b.tvChat;
    tvDelete = b.tvDelete;
  }

  @Override
  protected void configTitle(com.netease.yunxin.kit.common.ui.widgets.BackTitleBar bar) {
    super.configTitle(bar);
    bar.setBackgroundResource(R.color.normal_page_bg_second_color);
  }

  @Override
  protected Class<?> getEditActivityClass() {
    return RobotEditActivity.class;
  }

  /** 隐藏独立的编辑行，改为点击头像+名称区域进入编辑页 */
  @Override
  protected void setupEditEntry() {
    rlyEdit.setVisibility(View.GONE);
    rlyAvatarName.setOnClickListener(v -> onEditClick());
  }
}
