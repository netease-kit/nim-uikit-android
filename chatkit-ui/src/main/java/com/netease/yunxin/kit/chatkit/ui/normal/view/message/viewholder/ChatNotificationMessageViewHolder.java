// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageNotificationAttachment;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.TeamNotificationHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.util.List;

/** view holder for Text message */
public class ChatNotificationMessageViewHolder extends NormalChatBaseMessageViewHolder {

  private static final String LOG_TAG = "ChatNotificationMessageViewHolder";

  NormalChatMessageTextViewHolderBinding textBinding;

  public ChatNotificationMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    textBinding =
        NormalChatMessageTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    loadData(message, lastMessage, true);
  }

  @Override
  public void bindData(ChatMessageBean message, int position, @NonNull List<?> payload) {
    super.bindData(message, position, payload);
    for (int i = 0; i < payload.size(); ++i) {
      String payloadItem = payload.get(i).toString();
      if (TextUtils.equals(payloadItem, ActionConstants.PAYLOAD_USERINFO)) {
        loadData(message, null, false);
      }
    }
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    baseViewBinding.contentWithTopLayer.setBackgroundResource(R.color.title_transfer);
  }

  @Override
  protected void onLayoutConfig(ChatMessageBean messageBean) {
    ConstraintLayout.LayoutParams messageContainerLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
    ConstraintLayout.LayoutParams messageTopLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageTopGroup.getLayoutParams();
    ConstraintLayout.LayoutParams messageBottomLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageBottomGroup.getLayoutParams();
    messageContainerLayoutParams.horizontalBias = 0.5f;
    messageTopLayoutParams.horizontalBias = 0.5f;
    messageBottomLayoutParams.horizontalBias = 0.5f;
  }

  @Override
  protected void onCommonViewVisibleConfig(ChatMessageBean messageBean) {
    baseViewBinding.otherUsername.setVisibility(View.GONE);
    baseViewBinding.otherUserAvatar.setVisibility(View.GONE);
    baseViewBinding.myAvatar.setVisibility(View.GONE);
    baseViewBinding.myName.setVisibility(View.GONE);
    baseViewBinding.messageStatus.setVisibility(View.GONE);
  }

  private void loadData(ChatMessageBean message, ChatMessageBean lastMessage, boolean refreshTime) {
    if (message.getMessageData().getMessage().getAttachment()
        instanceof V2NIMMessageNotificationAttachment) {
      textBinding.messageText.setGravity(Gravity.CENTER);
      textBinding.messageText.setTextColor(
          IMKitClient.getApplicationContext().getResources().getColor(R.color.color_999999));
      textBinding.messageText.setTextSize(12);
      String content = TeamNotificationHelper.getTeamNotificationText(message.getMessageData());
      if (TextUtils.isEmpty(content)) {
        textBinding.getRoot().setVisibility(View.GONE);
        baseViewBinding.messageContentGroup.setVisibility(View.GONE);
        baseViewBinding.msgBgLayout.setVisibility(View.GONE);
      } else {
        baseViewBinding.messageContentGroup.setVisibility(View.VISIBLE);
        baseViewBinding.msgBgLayout.setVisibility(View.VISIBLE);
        textBinding.getRoot().setVisibility(View.VISIBLE);
        textBinding.messageText.setText(content);
      }
    } else {
      textBinding.getRoot().setVisibility(View.GONE);
      baseViewBinding.messageContentGroup.setVisibility(View.VISIBLE);
      baseViewBinding.msgBgLayout.setVisibility(View.VISIBLE);
      baseViewBinding.baseRoot.setVisibility(View.GONE);
    }
  }

  @Override
  protected boolean needMessageClickAndExtra() {
    return false;
  }

  @Override
  protected boolean needShowTimeView(ChatMessageBean message, ChatMessageBean lastMessage) {
    return lastMessage == null;
  }
}
