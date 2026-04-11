// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.robot;

import android.graphics.Typeface;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotInfoLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotInfoActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/** 娱乐版机器人信息页面 */
public class FunRobotInfoActivity extends BaseRobotInfoActivity {

  @Override
  protected void initViews() {
    changeStatusBarColor(R.color.color_white);
    FunRobotInfoLayoutBinding b = FunRobotInfoLayoutBinding.inflate(getLayoutInflater());
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
  protected void configTitle(BackTitleBar bar) {
    super.configTitle(bar);
    bar.getTitleTextView().setTextSize(17);
    bar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
  }

  @Override
  protected int getConfirmPositiveColor() {
    return ContextCompat.getColor(this, R.color.fun_contact_primary_color);
  }

  @Override
  protected String getChatRouterPath() {
    return RouterConstant.PATH_FUN_CHAT_P2P_PAGE;
  }

  @Override
  protected Class<?> getEditActivityClass() {
    return FunRobotEditActivity.class;
  }

  /** Fun 版：隐藏独立编辑行，点击头像+名称区域进入编辑页 */
  @Override
  protected void setupEditEntry() {
    rlyEdit.setVisibility(View.GONE);
    rlyAvatarName.setOnClickListener(v -> onEditClick());
  }

  @Override
  protected Class<?> getViewConfigActivityClass() {
    return FunRobotViewConfigActivity.class;
  }
}
