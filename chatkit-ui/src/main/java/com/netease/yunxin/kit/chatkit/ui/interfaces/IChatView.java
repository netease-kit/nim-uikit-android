// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
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

  void updateMessageStatus(ChatMessageBean message);

  void updateProgress(AttachmentProgress progress);

  void setTypeState(boolean isTyping);

  void hideCurrentInput();

  void updateInputHintInfo(String content);

  void setInputMute(boolean mute);

  void updateUserInfo(List<UserInfo> userInfoList);

  void setNetWorkState(boolean available);

  void setMessageBackground(Drawable drawable);

  void setReeditMessage(String content);

  void setReplyMessage(ChatMessageBean messageBean);

  void setMessageBackgroundRes(int res);

  void setMessageProxy(IMessageProxy messageProxy);

  void setTitleBarVisible(int visible);

  void setAitManager(AitManager manager);

  void setMessageViewHolderFactory(IChatFactory viewHolderFactory);

  void setLayoutCustom(IChatViewCustom layoutCustom);

  View getRootView();
}
