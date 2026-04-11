// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.graphics.Typeface;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotEditLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotCreateActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/** 娱乐版机器人创建页 */
public class FunRobotCreateActivity extends BaseRobotCreateActivity {

  @Override
  protected void initViews() {
    FunRobotEditLayoutBinding b = FunRobotEditLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    avatarView = b.avatarView;
    tvName = b.tvName;
    rlyAvatar = b.rlyAvatar;
    rlyName = b.rlyName;
    tvSave = b.tvSave;
  }

  @Override
  protected void configTitle(BackTitleBar bar) {
    super.configTitle(bar);
    bar.getTitleTextView().setTextSize(17);
    bar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
  }

  @Override
  protected String getRobotInfoRouterPath() {
    return RouterConstant.PATH_FUN_MY_ROBOT_INFO_PAGE;
  }

  @Override
  protected Class<?> getNameEditActivityClass() {
    return FunRobotEditNameActivity.class;
  }
}
