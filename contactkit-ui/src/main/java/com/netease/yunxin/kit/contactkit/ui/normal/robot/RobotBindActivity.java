// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.robot;

import com.netease.yunxin.kit.contactkit.ui.databinding.RobotBindLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotBindActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/** 协同版绑定机器人页面 */
public class RobotBindActivity extends BaseRobotBindActivity {

  @Override
  protected void initViews() {
    RobotBindLayoutBinding b = RobotBindLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    rvRobotList = b.rvRobotList;
    llEmpty = b.llEmpty;
    rlyCreate = b.rlyCreate;
  }

  @Override
  protected Class<?> getCreateActivityClass() {
    return RobotCreateActivity.class;
  }

  @Override
  protected String getRobotInfoRouterPath() {
    return RouterConstant.PATH_MY_ROBOT_INFO_PAGE;
  }
}
