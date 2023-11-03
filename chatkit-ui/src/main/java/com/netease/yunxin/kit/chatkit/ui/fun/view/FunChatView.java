// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.ChatViewHolderDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.IChatFactory;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatViewBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.factory.FunChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageReader;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.List;

/** chat view contain all view about chat */
public class FunChatView extends LinearLayout implements IChatView {

  private CharSequence titleName;
  FunChatViewBinding binding;

  public FunChatView(Context context) {
    super(context);
    init(null);
  }

  public FunChatView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public FunChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    ChatViewHolderDefaultFactory.getInstance().config(FunChatViewHolderFactory.getInstance());
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    binding = FunChatViewBinding.inflate(layoutInflater, this, true);
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
    return binding.titleBar;
  }

  public FrameLayout getTitleBarLayout() {
    return binding.titleLayout;
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

  public void setChatConfig(ChatUIConfig config) {
    if (config != null) {
      binding.messageView.setMessageProperties(config.messageProperties);
      binding.inputView.setInputProperties(config.inputProperties);
    }
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

  public void appendMessageList(List<ChatMessageBean> messageList, boolean needToScrollEnd) {
    binding.messageView.appendMessageList(messageList, needToScrollEnd);
  }

  public void updateUserInfo(List<UserInfo> userInfoList) {
    binding.messageView.updateUserInfo(userInfoList);
  }

  public void setMessageBackground(Drawable drawable) {
    binding.bodyLayout.setBackground(drawable);
  }

  public void setMessageBackgroundRes(int res) {
    binding.bodyLayout.setBackgroundResource(res);
  }

  public void setMessageBackgroundColor(int color) {
    binding.bodyLayout.setBackgroundColor(color);
  }

  @Override
  public void setTitleBarVisible(int visible) {
    binding.titleLayout.setVisibility(visible);
  }

  @Override
  public void setReeditMessage(String content) {
    binding.inputView.setReEditMessage(content);
  }

  @Override
  public void setReplyMessage(ChatMessageBean messageBean) {
    binding.inputView.setReplyMessage(messageBean);
  }

  public void setTypeState(boolean isTyping) {
    if (titleName == null) {
      titleName = binding.titleBar.getTitleTextView().getText();
    }
    CharSequence tempTitleName;
    if (isTyping) {
      tempTitleName = binding.getRoot().getContext().getString(R.string.chat_message_is_typing_fun);
    } else {
      tempTitleName = titleName;
    }
    binding.titleBar.setTitle(String.valueOf(tempTitleName));
  }

  public void setNetWorkState(boolean available) {
    if (available) {
      binding.notificationTextView.setVisibility(GONE);
    } else {
      binding.notificationTextView.setVisibility(VISIBLE);
      binding.notificationTextView.setTextSize(14);
      binding.notificationTextView.setText(R.string.chat_network_error_tip);
      binding.notificationTextView.setTextColor(
          getContext().getResources().getColor(R.color.color_50_000000));
      binding.notificationTextView.setBackgroundResource(R.color.color_fceeee);
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

  @Override
  public void hideCurrentInput() {
    binding.inputView.hideCurrentInput();
  }

  @Override
  public void updateInputHintInfo(String content) {
    binding.inputView.updateInputHintInfo(content);
  }

  @Override
  public void setInputMute(boolean mute) {
    binding.inputView.setMute(mute);
  }

  public void setMessageProxy(IMessageProxy messageProxy) {
    binding.inputView.init(messageProxy);
  }

  public MessageBottomLayout getInputView() {
    return binding.inputView;
  }

  public FrameLayout getChatBodyLayout() {
    return binding.bodyLayout;
  }

  public FrameLayout getChatBottomLayout() {
    return binding.bottomLayout;
  }

  public FunChatViewBinding getChatViewFunLayoutBinding() {
    return binding;
  }

  public FrameLayout getChatBodyTopLayout() {
    return binding.bodyTopLayout;
  }

  public FrameLayout getChatBodyBottomLayout() {
    return binding.bodyBottomLayout;
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

  public View getRootView() {
    return binding.getRoot();
  }
}
