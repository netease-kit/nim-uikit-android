// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.builder;

import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;

/**
 * 聊天页面构建器抽象类 用于构建和配置继承自ChatBaseFragment的聊天片段实例 提供链式调用方式设置各种配置参数
 *
 * @param <T> 泛型参数，必须继承自ChatBaseFragment
 */
public abstract class ChatFragmentBuilder<T extends ChatBaseFragment> {

  /** 聊天UI配置对象 用于存储聊天界面的各种配置信息 */
  protected ChatUIConfig chatConfig;

  /**
   * 获取聊天片段实例的抽象方法 由子类实现具体的片段创建逻辑
   *
   * @return 聊天片段实例
   */
  public abstract T getFragment();

  /**
   * 构建并返回配置好的聊天片段实例 如果chatConfig不为null，则将其设置到片段中
   *
   * @return 配置好的聊天片段实例
   */
  public T build() {
    T fragment = getFragment();
    if (chatConfig != null) {
      fragment.setChatConfig(chatConfig);
    }
    return fragment;
  }

  /**
   * 设置聊天视图自定义接口 用于自定义聊天界面的外观和行为
   *
   * @param chatViewCustom 聊天视图自定义接口实现
   * @return 当前构建器实例，支持链式调用
   */
  public ChatFragmentBuilder<T> setChatViewCustom(IChatViewCustom chatViewCustom) {
    if (chatConfig == null) {
      chatConfig = new ChatUIConfig();
    }
    this.chatConfig.chatViewCustom = chatViewCustom;
    return this;
  }

  /**
   * 设置聊天消息视图持有者工厂 用于创建不同类型消息的视图持有者
   *
   * @param factory 聊天工厂接口实现
   * @return 当前构建器实例，支持链式调用
   */
  public ChatFragmentBuilder<T> setChatMessageViewHolderFactory(IChatFactory factory) {
    if (chatConfig == null) {
      chatConfig = new ChatUIConfig();
    }
    this.chatConfig.chatFactory = factory;
    return this;
  }

  /**
   * 设置聊天消息属性 用于配置消息的显示属性
   *
   * @param properties 消息属性对象
   * @return 当前构建器实例，支持链式调用
   */
  public ChatFragmentBuilder<T> setChatMessageProperties(MessageProperties properties) {
    if (chatConfig == null) {
      chatConfig = new ChatUIConfig();
    }
    this.chatConfig.messageProperties = properties;
    return this;
  }
}
