// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.view.View;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** Message item click event listener 消息项点击事件监听器 该接口定义了消息项的点击事件回调方法 */
public interface IMessageItemClickListener {
  /**
   * 消息长按事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onMessageLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 消息点击事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 消息选择事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @param selected 是否选中
   * @return 是否消费该事件
   */
  default boolean onMessageSelect(
      View view, int position, ChatMessageBean messageInfo, boolean selected) {
    return false;
  }

  /**
   * 用户头像点击事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onUserIconClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 自身头像点击事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onSelfIconClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 自身头像长按事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onSelfIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 用户头像长按事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onUserIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 重新编辑已撤回消息事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onReeditRevokeMessage(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 回复消息点击事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onReplyMessageClick(View view, int position, IMMessageInfo messageInfo) {
    return false;
  }

  /**
   * 发送失败按钮点击事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onSendFailBtnClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * 文本选中事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @param text 选中的文本
   * @param isSelectAll 是否全选
   * @return 是否消费该事件
   */
  default boolean onTextSelected(
      View view, int position, ChatMessageBean messageInfo, String text, boolean isSelectAll) {
    return false;
  }

  /**
   * 自定义点击事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onCustomClick(View view, int position, ChatMessageBean messageInfo) {
    return false;
  }

  /**
   * AI 消息刷新事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onAIMessageRefresh(View view, int position, IMMessageInfo messageInfo) {
    return false;
  }

  /**
   * AI 消息流式输出停止事件回调
   *
   * @param view 触发事件的视图
   * @param position 消息所在位置
   * @param messageInfo 消息信息
   * @return 是否消费该事件
   */
  default boolean onAIMessageStreamStop(View view, int position, IMMessageInfo messageInfo) {
    return false;
  }
}
