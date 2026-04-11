// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.graphics.Typeface;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotViewConfigLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotViewConfigActivity;

/** 娱乐版查看机器人配置串页面 */
public class FunRobotViewConfigActivity extends BaseRobotViewConfigActivity {

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.fun_contact_page_bg_color);
    FunRobotViewConfigLayoutBinding b =
        FunRobotViewConfigLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    tvConfigContent = b.tvConfigContent;
    btnCopy = b.btnCopy;
  }

  @Override
  protected void configTitle(BackTitleBar bar) {
    super.configTitle(bar);
    bar.setBackgroundResource(R.color.color_ededed);
    bar.getTitleTextView().setTextSize(17);
    bar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
  }
}
