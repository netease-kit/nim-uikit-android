// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.robot;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.RobotViewConfigLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotViewConfigActivity;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

/** 协同版查看机器人配置串页面 */
public class RobotViewConfigActivity extends BaseRobotViewConfigActivity {

  /** 配置串字段分隔符，修改此处即可全局生效 */
  private static final String CONFIG_SEPARATOR = "|";

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.normal_page_bg_second_color);
    RobotViewConfigLayoutBinding b = RobotViewConfigLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    tvConfigContent = b.tvConfigContent;
    btnCopy = b.btnCopy;
  }

  @Override
  protected String buildConfigString(RobotInfoBean bean) {
    if (bean != null && bean.getAIBot() != null) {
      return IMKitClient.getOptions().appKey
          + CONFIG_SEPARATOR
          + bean.getAccountId()
          + CONFIG_SEPARATOR
          + bean.getAIBot().getToken();
    }
    return "";
  }

  @Override
  protected void configTitle(com.netease.yunxin.kit.common.ui.widgets.BackTitleBar bar) {
    super.configTitle(bar);
    bar.setBackgroundResource(R.color.normal_page_bg_second_color);
  }
}
