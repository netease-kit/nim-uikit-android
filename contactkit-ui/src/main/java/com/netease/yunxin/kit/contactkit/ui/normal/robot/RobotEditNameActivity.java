// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.robot;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.RobotEditNameLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotEditNameActivity;

/** 协同版机器人名称编辑页 */
public class RobotEditNameActivity extends BaseRobotEditNameActivity {

  @Override
  protected int getMaxNameLength() {
    return 15;
  }

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.normal_page_bg_second_color);
    RobotEditNameLayoutBinding b = RobotEditNameLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    // 新布局使用自定义返回图标，不使用 BackTitleBar
    titleBar = null;
    ivBack = b.ivBack;
    etName = b.etName;
    ivClear = b.ivClear;
    tvSave = b.tvSave;
    tvCount = b.tvCount;
  }
}
