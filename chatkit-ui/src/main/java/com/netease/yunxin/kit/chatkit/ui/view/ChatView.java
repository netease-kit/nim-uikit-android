// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatViewLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.input.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.List;

/** chat view contain all view about chat */
public class ChatView extends LinearLayout {

  ChatViewLayoutBinding binding;

  public ChatView(Context context) {
    super(context);
    init(null);
  }

  public ChatView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    binding = ChatViewLayoutBinding.inflate(layoutInflater, this, true);
    binding.messageView.setOnListViewEventListener(
        new ChatMessageListView.OnListViewEventListener() {
          @Override
          public void onListViewStartScroll() {
            binding.inputView.collapse(true);
          }

          @Override
          public void onListViewTouched() {
            binding.inputView.collapse(true);
          }
        });
    binding.getRoot().setOnClickListener(v -> binding.inputView.collapse(true));
  }

  public BackTitleBar getTitleBar() {
    return binding.chatViewTitle;
  }

  public ChatMessageListView getMessageListView() {
    return binding.messageView;
  }

  public void setLoadHandler(IMessageLoadHandler loadHandler) {
    binding.messageView.setLoadHandler(loadHandler);
  }

  public void setMessageReader(IMessageReader messageReader) {
    binding.messageView.setMessageReader(messageReader);
  }

  public void setItemClickListener(IMessageItemClickListener itemClickListener) {
    binding.messageView.setItemClickListener(itemClickListener);
  }

  public void setMessageProperties(MessageProperties properties) {
    binding.messageView.setMessageProperties(properties);
  }

  public void clearMessageList() {
    binding.messageView.clearMessageList();
  }

  public void addMessageListForward(List<ChatMessageBean> messageList) {
    binding.messageView.addMessageListForward(messageList);
  }

  public void appendMessageList(List<ChatMessageBean> messageList) {
    binding.messageView.appendMessageList(messageList);
  }

  public void updateUserInfo(List<UserInfo> userInfoList) {
    binding.messageView.updateUserInfo(userInfoList);
  }

  public void setMessageBackground(Drawable drawable) {
    binding.chatViewBody.setBackground(drawable);
  }

  public void setMessageBackgroundRes(int res) {
    binding.chatViewBody.setBackgroundResource(res);
  }

  public void setMessageBackgroundColor(int color) {
    binding.chatViewBody.setBackgroundColor(color);
  }

  public void setTypeState(boolean isTyping) {
    if (isTyping) {
      binding.tvInputTip.setVisibility(VISIBLE);
    } else {
      binding.tvInputTip.setVisibility(GONE);
    }
  }

  public void setNetWorkState(boolean available) {
    if (available) {
      binding.tvNotification.setVisibility(GONE);
    } else {
      binding.tvNotification.setVisibility(VISIBLE);
      binding.tvNotification.setTextSize(14);
      binding.tvNotification.setText(R.string.chat_network_error_tip);
      binding.tvNotification.setTextColor(
          getContext().getResources().getColor(R.color.color_fc596a));
      binding.tvNotification.setBackgroundResource(R.color.color_fee3e6);
    }
  }

  public void appendMessage(ChatMessageBean message) {
    binding.messageView.appendMessage(message);
  }

  public void updateMessageStatus(ChatMessageBean message) {
    binding.messageView.updateMessageStatus(message);
  }

  public void updateMessage(ChatMessageBean message, Object payload) {
    binding.messageView.updateMessage(message, payload);
  }

  public void updateMessage(IMMessage message, Object payload) {
    binding.messageView.updateMessage(message, payload);
  }

  public void updateProgress(AttachmentProgress progress) {
    binding.messageView.updateAttachmentProgress(progress);
  }

  public void setMessageProxy(IMessageProxy messageProxy) {
    binding.inputView.init(messageProxy);
  }

  public MessageBottomLayout getInputView() {
    return binding.inputView;
  }

  public FrameLayout getChatBodyLayout() {
    return binding.chatViewBody;
  }

  public FrameLayout getChatBottomLayout() {
    return binding.chatViewBottom;
  }

  public ChatViewLayoutBinding getChatViewLayoutBinding() {
    return binding;
  }

  public FrameLayout getChatBodyTopLayout() {
    return binding.chatViewBodyTop;
  }

  public FrameLayout getChatBodyBottomLayout() {
    return binding.chatViewBodyBottom;
  }

  public void setAitManager(AitManager manager) {
    manager.setAitTextChangeListener(binding.inputView);
    binding.inputView.setAitTextWatcher(manager);
  }

  public void setMessageViewHolderFactory(IChatFactory viewHolderFactory) {
    binding.messageView.setViewHolderFactory(viewHolderFactory);
  }

  public void setLayoutCustom(IChatViewCustom layoutCustom) {
    if (layoutCustom != null) {
      layoutCustom.customizeChatLayout(this);
    }
  }
}
