// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.builder;

import com.netease.yunxin.kit.chatkit.ui.builder.ChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatTeamFragment;

public class TeamChatFragmentBuilder extends ChatFragmentBuilder<FunChatTeamFragment> {

  @Override
  public FunChatTeamFragment getFragment() {
    return new FunChatTeamFragment();
  }
}
