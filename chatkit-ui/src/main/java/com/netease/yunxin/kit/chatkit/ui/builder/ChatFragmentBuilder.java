// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.builder;

import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;

public abstract class ChatFragmentBuilder<T extends ChatBaseFragment> {

  protected ChatUIConfig chatConfig;

  abstract T getFragment();

  public T build() {
    T fragment = getFragment();
    if (chatConfig != null) {
      fragment.setChatConfig(chatConfig);
    }
    return fragment;
  }

  public ChatFragmentBuilder<T> setChatViewCustom(IChatViewCustom chatViewCustom) {
    if (chatConfig == null) {
      chatConfig = new ChatUIConfig();
    }
    this.chatConfig.chatViewCustom = chatViewCustom;
    return this;
  }

  public ChatFragmentBuilder<T> setChatMessageViewHolderFactory(IChatFactory factory) {
    if (chatConfig == null) {
      chatConfig = new ChatUIConfig();
    }
    this.chatConfig.chatFactory = factory;
    return this;
  }

  public ChatFragmentBuilder<T> setChatMessageProperties(MessageProperties properties) {
    if (chatConfig == null) {
      chatConfig = new ChatUIConfig();
    }
    this.chatConfig.messageProperties = properties;
    return this;
  }
}
