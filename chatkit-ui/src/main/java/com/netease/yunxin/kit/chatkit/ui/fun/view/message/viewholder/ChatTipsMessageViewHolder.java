// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_CREATED_TIP;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageTipViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import java.util.Map;

/** view holder for Text message */
public class ChatTipsMessageViewHolder extends FunChatBaseMessageViewHolder {

  private static final String TAG = "ChatTipsMessageViewHolder";

  FunChatMessageTipViewHolderBinding textBinding;

  public ChatTipsMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    textBinding =
        FunChatMessageTipViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    baseViewBinding.messageContainer.setBackgroundResource(R.color.title_transfer);
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

  @Override
  protected boolean needShowTimeView(ChatMessageBean message, ChatMessageBean lastMessage) {
    return lastMessage == null;
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    String content = message.getMessageData().getMessage().getContent();
    if (content == null || content.isEmpty()) {
      // create team tip
      Map<String, Object> extension = message.getMessageData().getMessage().getRemoteExtension();
      if (extension != null && extension.get(KEY_TEAM_CREATED_TIP) != null) {
        content = extension.get(KEY_TEAM_CREATED_TIP).toString();
      }
    }
    if (content != null && !content.isEmpty()) {
      textBinding.messageTipText.setGravity(Gravity.CENTER);
      textBinding.messageTipText.setTextColor(
          IMKitClient.getApplicationContext().getResources().getColor(R.color.color_999999));
      textBinding.messageTipText.setTextSize(12);
      textBinding.messageTipText.setText(content);
    } else {
      baseViewBinding.baseRoot.setVisibility(View.GONE);
    }
  }
}
