// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.yunxin.kit.chatkit.media.BitmapDecoder;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import java.io.File;

public class ChatImageMessageViewHolder extends ChatThumbBaseViewHolder {
  private static final String TAG = "ChatImageMessageViewHolder";

  public ChatImageMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);

    binding.progressBarInsideIcon.setVisibility(View.GONE);
    binding.playIcon.setVisibility(View.GONE);
    if (getMsgInternal().getStatus() == MsgStatusEnum.sending
        || getMsgInternal().getAttachStatus() == AttachStatusEnum.transferring) {
      binding.progressBar.setVisibility(View.VISIBLE);
      binding.progressBar.setIndeterminate(true);
    } else {
      binding.progressBar.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onMessageStatus(ChatMessageBean data) {
    super.onMessageStatus(data);
    binding.progressBar.setVisibility(View.GONE);
  }

  @Override
  protected String thumbFromSourceFile(String path) {
    return path;
  }

  @Override
  protected int[] getBounds(String path) {
    int[] bounds = null;
    if (path != null) {
      bounds = BitmapDecoder.decodeBound(new File(path));
    }
    if (bounds == null) {
      ImageAttachment attachment = (ImageAttachment) getMsgInternal().getAttachment();
      bounds = new int[] {attachment.getWidth(), attachment.getHeight()};
    }
    return bounds;
  }

  @Override
  protected float[] getCorners() {
    int corner = SizeUtils.dp2px(12);
    boolean msgIn = isReceivedMessage(currentMessage);
    float radiusTopLeft = msgIn ? 0 : corner;
    float radiusTopRight = msgIn ? corner : 0;
    return new float[] {radiusTopLeft, radiusTopRight, corner, corner};
  }
}
