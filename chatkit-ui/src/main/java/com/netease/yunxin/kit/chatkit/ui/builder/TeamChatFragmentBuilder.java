// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.builder;

import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatTeamFragment;

public class TeamChatFragmentBuilder extends ChatFragmentBuilder<ChatTeamFragment> {

  @Override
  ChatTeamFragment getFragment() {
    return new ChatTeamFragment();
  }
}
