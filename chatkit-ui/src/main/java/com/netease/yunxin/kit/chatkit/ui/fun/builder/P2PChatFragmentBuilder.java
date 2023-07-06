// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.builder;

import com.netease.yunxin.kit.chatkit.ui.builder.ChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatP2PFragment;

public class P2PChatFragmentBuilder extends ChatFragmentBuilder<FunChatP2PFragment> {

  @Override
  public FunChatP2PFragment getFragment() {
    return new FunChatP2PFragment();
  }
}
