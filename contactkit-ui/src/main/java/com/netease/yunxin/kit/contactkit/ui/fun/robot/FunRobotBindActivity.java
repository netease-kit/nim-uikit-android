// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.graphics.Typeface;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotBindLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotBindActivity;
import com.netease.yunxin.kit.contactkit.ui.robot.RobotBindAdapter;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/** 娱乐版绑定机器人页面 */
public class FunRobotBindActivity extends BaseRobotBindActivity {

  @Override
  protected void initViews() {
    FunRobotBindLayoutBinding b = FunRobotBindLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    rvRobotList = b.rvRobotList;
    llEmpty = b.llEmpty;
    rlyCreate = b.rlyCreate;
  }

  @Override
  protected void configTitle(BackTitleBar bar) {
    super.configTitle(bar);
    bar.getTitleTextView().setTextSize(17);
    bar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
  }

  @Override
  protected RobotBindAdapter createAdapter() {
    return new RobotBindAdapter(true);
  }

  @Override
  protected Class<?> getCreateActivityClass() {
    return FunRobotCreateActivity.class;
  }

  @Override
  protected String getRobotInfoRouterPath() {
    return RouterConstant.PATH_FUN_MY_ROBOT_INFO_PAGE;
  }
}
