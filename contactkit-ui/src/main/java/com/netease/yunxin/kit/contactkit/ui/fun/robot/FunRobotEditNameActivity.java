// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.graphics.Typeface;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotEditNameLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotEditNameActivity;

/** 娱乐版机器人名称编辑页 */
public class FunRobotEditNameActivity extends BaseRobotEditNameActivity {

  @Override
  protected int getMaxNameLength() {
    return 15;
  }

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.color_ededed);
    FunRobotEditNameLayoutBinding b = FunRobotEditNameLayoutBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    titleBar = b.title;
    etName = b.etName;
    ivClear = b.ivClear;
    tvSave = b.tvSave;
    tvCount = b.tvCount;
  }

  @Override
  protected void configTitle(BackTitleBar bar) {
    super.configTitle(bar);
    bar.getTitleTextView().setTextSize(17);
    bar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
  }
}
