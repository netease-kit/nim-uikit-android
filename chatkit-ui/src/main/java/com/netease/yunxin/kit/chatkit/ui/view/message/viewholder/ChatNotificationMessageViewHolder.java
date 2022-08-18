// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachmentWithExtension;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.TeamNotificationHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.im.IMKitClient;

/** view holder for Text message */
public class ChatNotificationMessageViewHolder extends ChatBaseMessageViewHolder {

  private static final String LOG_TAG = "ChatNotificationMessageViewHolder";

  ChatMessageTextViewHolderBinding textBinding;

  public ChatNotificationMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    textBinding =
        ChatMessageTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    if (message.getMessageData().getMessage().getAttachment()
        instanceof NotificationAttachmentWithExtension) {
      textBinding.messageText.setTextColor(
          IMKitClient.getApplicationContext().getResources().getColor(R.color.color_999999));
      textBinding.messageText.setTextSize(12);
      textBinding.messageText.setText(
          TeamNotificationHelper.getTeamNotificationText(message.getMessageData()));
    } else {
      baseViewBinding.baseRoot.setVisibility(View.GONE);
    }
  }
}
