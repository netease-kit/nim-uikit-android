// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.List;

public interface IChatView {

  BackTitleBar getTitleBar();

  ChatMessageListView getMessageListView();

  void setLoadHandler(IMessageLoadHandler loadHandler);

  void setMessageReader(IMessageReader messageReader);

  void setItemClickListener(IMessageItemClickListener itemClickListener);

  void setChatConfig(ChatUIConfig config);

  void clearMessageList();

  void addMessageListForward(List<ChatMessageBean> messageList);

  void appendMessageList(List<ChatMessageBean> messageList);

  void appendMessageList(List<ChatMessageBean> messageList, boolean needToScrollEnd);

  void appendMessage(ChatMessageBean message);

  void deleteMessage(List<ChatMessageBean> message);

  /**
   * 根据clientId删除消息
   *
   * @param clientIds 消息clientId
   */
  void deleteMessages(List<String> clientIds);

  List<ChatMessageBean> getMessageList();

  void revokeMessage(V2NIMMessageRefer message);

  void updateMessageStatus(ChatMessageBean message);

  void notifyUserInfoChanged(List<String> accountIdList);

  void updateProgress(IMMessageProgress progress);

  void setTypeState(boolean isTyping);

  void hideCurrentInput();

  void updateInputHintInfo(String content);

  void setInputMute(boolean mute);

  void updateUserInfo(List<UserInfo> userInfoList);

  void setNetWorkState(boolean available);

  void setMessageBackground(Drawable drawable);

  // 设置撤回消息重新编辑内容到输入框
  void setReeditMessage(String content);

  // 设置富文本撤回消息重新编辑内容到输入框
  void setReEditRichMessage(String title, String body);

  void setReplyMessage(ChatMessageBean messageBean);

  // 展示富文本输入框，并将底部输入框内容设置标题和内容
  void showRichInputPanel();

  // 隐藏富文本输入框，并设置标题和内容到底部输入框
  void hideRichInputPanel();

  void setMessageBackgroundRes(int res);

  void setMessageProxy(IMessageProxy messageProxy);

  void setTitleBarVisible(int visible);

  void setAitManager(AitManager manager);

  void setMessageViewHolderFactory(IChatFactory viewHolderFactory);

  void setLayoutCustom(IChatViewCustom layoutCustom);

  void showMultiSelect(boolean show);

  boolean isMultiSelect();

  void setMultiSelectEnable(boolean enable);

  View getRootView();
}
