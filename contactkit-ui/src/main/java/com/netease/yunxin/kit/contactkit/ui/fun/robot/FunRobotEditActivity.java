// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.graphics.Typeface;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotEditLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotEditActivity;

/** 娱乐版机器人编辑页 */
public class FunRobotEditActivity extends BaseRobotEditActivity {

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.fun_contact_page_primary_bg_color);
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
  protected Class<?> getNameEditActivityClass() {
    return FunRobotEditNameActivity.class;
  }
}
