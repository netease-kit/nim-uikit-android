// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_CREATED_TIP;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import java.util.Map;

/** view holder for Text message */
public class ChatTipsMessageViewHolder extends ChatBaseMessageViewHolder {

  private static final String LOG_TAG = "ChatTipsMessageViewHolder";

  ChatMessageTextViewHolderBinding textBinding;

  public ChatTipsMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
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
    String content = message.getMessageData().getMessage().getContent();
    if (content == null || content.isEmpty()) {
      // create team tip
      Map<String, Object> extension = message.getMessageData().getMessage().getRemoteExtension();
      if (extension != null && extension.get(KEY_TEAM_CREATED_TIP) != null) {
        content = extension.get(KEY_TEAM_CREATED_TIP).toString();
      }
    }
    if (content != null && !content.isEmpty()) {
      textBinding.messageText.setTextColor(
          IMKitClient.getApplicationContext().getResources().getColor(R.color.color_999999));
      textBinding.messageText.setTextSize(12);
      textBinding.messageText.setText(content);
    } else {
      baseViewBinding.baseRoot.setVisibility(View.GONE);
    }
  }
}
