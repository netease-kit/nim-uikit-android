// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.TimeFormatLocalUtils;
import java.util.List;
import java.util.Locale;

/** base message view holder for chat message item */
public abstract class ChatBasePinViewHolder extends ChatBaseViewHolder<ChatMessageBean> {

  private static final String TAG = "ChatBasePinViewHolder";

  public int type;

  public int position;

  public Team teamInfo;

  public ChatMessageBean currentMessage;

  MessageProperties properties = new MessageProperties();

  public ChatBasePinViewHolderBinding baseViewBinding;

  public ViewGroup parent;

  public ChatBasePinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    this(parent.fileBaseRoot);
    this.parent = parent.getRoot();
    this.type = viewType;
    baseViewBinding = parent;
  }

  public ChatBasePinViewHolder(View view) {
    super(view);
  }

  public void setProperties(MessageProperties properties) {
    if (properties != null) {
      this.properties = properties;
    }
  }

  @Override
  public void onBindData(ChatMessageBean data, int position, @NonNull List<?> payload) {
    if (!payload.isEmpty()) {
      for (int i = 0; i < payload.size(); ++i) {
        String payloadItem = payload.get(i).toString();
        if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_STATUS)) {
          currentMessage = data;
          onMessageStatus(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_USERINFO)) {
          setUserInfo(data);
        } else if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_PROGRESS)) {
          // 消息附件下载进度更新
          onProgressUpdate(data);
        }
      }
    }
    this.position = position;
  }

  protected void onMessageStatus(ChatMessageBean data) {}

  protected void onProgressUpdate(ChatMessageBean data) {}

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    currentMessage = message;
    int padding = SizeUtils.dp2px(8);
    baseViewBinding.fileBaseRoot.setPadding(padding, padding, padding, padding);
    baseViewBinding.messageContainer.removeAllViews();
    addContainer();

    setUserInfo(message);
    setTime(message);
    setClickListener();
  }

  private void setUserInfo(ChatMessageBean message) {
    if (type == ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE
        || type == ChatMessageType.TIP_MESSAGE_VIEW_TYPE) {
      return;
    }
    baseViewBinding.messageBody.setGravity(Gravity.START);
    loadNickAndAvatar(message);
  }

  private void loadNickAndAvatar(ChatMessageBean message) {

    //get nick name
    String name =
        MessageHelper.getChatMessageUserNameByAccount(
            message.getSenderId(), message.getMessageData().getMessage().getConversationType());
    baseViewBinding.otherUsername.setText(name);
    if (properties.getUserNickColor() != null) {
      baseViewBinding.otherUsername.setTextColor(properties.getUserNickColor());
    }
    if (properties.getUserNickTextSize() != null) {
      baseViewBinding.otherUsername.setTextSize(properties.getUserNickTextSize());
    }
    String avatar =
        MessageHelper.getChatCacheAvatar(
            message.getSenderId(), message.getMessageData().getMessage().getConversationType());
    baseViewBinding.messageAvatar.setVisibility(View.VISIBLE);
    if (properties.getAvatarCornerRadius() != null) {
      baseViewBinding.messageAvatar.setCornerRadius(properties.getAvatarCornerRadius());
    }
    baseViewBinding.messageAvatar.setData(
        avatar, name, AvatarColor.avatarColor(message.getMessageData().getMessage().getSenderId()));
  }

  private void setTime(ChatMessageBean message) {
    long createTime =
        message.getMessageData().getMessage().getCreateTime() == 0
            ? System.currentTimeMillis()
            : message.getMessageData().getMessage().getCreateTime();
    baseViewBinding.tvTime.setVisibility(View.VISIBLE);
    if (properties.getTimeTextColor() != null) {
      baseViewBinding.tvTime.setTextColor(properties.getTimeTextColor());
    }
    if (properties.getTimeTextSize() != null) {
      baseViewBinding.tvTime.setTextSize(properties.getTimeTextSize());
    }
    baseViewBinding.tvTime.setText(
        TimeFormatLocalUtils.formatMillisecond(
            itemView.getContext(),
            createTime,
            new Locale(
                AppLanguageConfig.getInstance()
                    .getAppLanguage(IMKitClient.getApplicationContext()))));
  }

  public void setTeamInfo(Team teamInfo) {
    this.teamInfo = teamInfo;
  }

  /** set click listener for ivStatus */
  private void setClickListener() {
    if (itemListener != null) {

      baseViewBinding.fileBaseRoot.setOnClickListener(
          v -> itemListener.onMessageClick(v, position, currentMessage));

      baseViewBinding.messageBody.setOnClickListener(
          v -> itemListener.onCustomViewClick(v, position, currentMessage));

      baseViewBinding.ivMoreAction.setOnClickListener(
          v -> itemListener.onCustomViewClick(v, position, currentMessage));
    }
  }

  public void addContainer() {}

  public ViewGroup getContainer() {
    return baseViewBinding.messageContainer;
  }
}
