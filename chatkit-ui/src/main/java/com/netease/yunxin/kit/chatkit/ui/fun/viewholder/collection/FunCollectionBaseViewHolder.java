// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import java.util.List;

/** 消息收藏列表 基础ViewHolder 提供收藏消息的基础UI和点击事件以及数据绑定 */
public abstract class FunCollectionBaseViewHolder extends ChatBaseViewHolder<CollectionBean> {

  private static final String TAG = "FunCollectionBaseViewHolder";

  // 消息类型
  public int type;
  // 消息位置
  public int position;
  // 当前消息数据
  public CollectionBean currentCollection;
  // 基础UI
  public FunCollectionBaseViewHolderBinding baseViewBinding;

  public ViewGroup parent;

  public FunCollectionBaseViewHolder(
      @NonNull FunCollectionBaseViewHolderBinding parent, int viewType) {
    this(parent.fileBaseRoot);
    this.parent = parent.getRoot();
    this.type = viewType;
    baseViewBinding = parent;
  }

  public FunCollectionBaseViewHolder(View view) {
    super(view);
  }

  @Override
  public void onBindData(CollectionBean data, int position, @NonNull List<?> payload) {
    if (!payload.isEmpty()) {
      for (int i = 0; i < payload.size(); ++i) {
        String payloadItem = payload.get(i).toString();
        if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_STATUS)) {
          currentCollection = data;
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

  protected void onMessageStatus(CollectionBean data) {}

  protected void onProgressUpdate(CollectionBean data) {}

  @Override
  public void onBindData(CollectionBean message, int position) {
    currentCollection = message;
    int padding = SizeUtils.dp2px(8);
    baseViewBinding.fileBaseRoot.setPadding(padding, padding, padding, padding);
    baseViewBinding.messageContainer.removeAllViews();
    addContainer();
    setUserInfo(message);
    setTime(message);
    setClickListener();
  }

  private void setUserInfo(CollectionBean message) {
    if (type == ChatMessageType.NOTICE_MESSAGE_VIEW_TYPE
        || type == ChatMessageType.TIP_MESSAGE_VIEW_TYPE) {
      return;
    }
    baseViewBinding.messageBody.setGravity(Gravity.START);
    baseViewBinding.userNameTv.setText(message.getUserName());
    if (message.getMessageData() != null) {
      baseViewBinding.messageAvatar.setData(
          message.getSenderAvatar(),
          message.getUserName(),
          AvatarColor.avatarColor(message.getMessageData().getSenderId()));
      String conversationName = "";
      if (message.getMessageData().getConversationType()
          == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
        conversationName =
            String.format(
                getContainer().getContext().getString(R.string.chat_collection_p2p_name_tip),
                message.getConversationName());
      } else {
        conversationName =
            String.format(
                getContainer().getContext().getString(R.string.chat_collection_team_name_tip),
                message.getConversationName());
      }
      baseViewBinding.conversationNameTv.setText(conversationName);
    }
  }

  private void setTime(CollectionBean message) {
    long createTime =
        message.getCreateTime() == 0 ? System.currentTimeMillis() : message.getCreateTime();
    baseViewBinding.timeTv.setVisibility(View.VISIBLE);
    baseViewBinding.timeTv.setText(
        TimeFormatUtils.formatMillisecond(itemView.getContext(), createTime));
  }

  /** set click listener for ivStatus */
  private void setClickListener() {
    if (itemListener != null) {
      baseViewBinding.fileBaseRoot.setOnClickListener(
          v -> itemListener.onMessageClick(v, position, currentCollection));
      baseViewBinding.messageBody.setOnClickListener(
          v -> itemListener.onMessageClick(v, position, currentCollection));

      baseViewBinding.fileBaseRoot.setOnLongClickListener(
          v -> itemListener.onMessageLongClick(v, position, currentCollection));
      baseViewBinding.messageBody.setOnLongClickListener(
          v -> itemListener.onMessageLongClick(v, position, currentCollection));

      baseViewBinding.ivMoreAction.setOnClickListener(
          v -> itemListener.onViewClick(v, position, currentCollection));
    }
  }

  public void addContainer() {}

  public ViewGroup getContainer() {
    return baseViewBinding.messageContainer;
  }
}
