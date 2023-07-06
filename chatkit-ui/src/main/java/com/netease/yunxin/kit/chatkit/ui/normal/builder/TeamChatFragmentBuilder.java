// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.builder;

import com.netease.yunxin.kit.chatkit.ui.builder.ChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.normal.page.fragment.ChatTeamFragment;

public class TeamChatFragmentBuilder extends ChatFragmentBuilder<ChatTeamFragment> {

  @Override
  public ChatTeamFragment getFragment() {
    return new ChatTeamFragment();
  }
}
