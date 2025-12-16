// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageAttachmentUploadState;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;

public class FunChatImagePinViewHolder extends FunChatThumbPinViewHolder {
  private static final String TAG = "ChatImagePinViewHolder";

  public FunChatImagePinViewHolder(@NonNull FunChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    binding.progressBarInsideIcon.setVisibility(View.GONE);
    binding.playIcon.setVisibility(View.GONE);
    if (getMsgInternal().getSendingState()
            == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SENDING
        || getMsgInternal().getAttachmentUploadState()
            == V2NIMMessageAttachmentUploadState.V2NIM_MESSAGE_ATTACHMENT_UPLOAD_STATE_UPLOADING) {
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
      bounds = ImageUtils.getSize(path);
    }
    if (bounds == null || bounds[0] == 0) {
      V2NIMMessageImageAttachment attachment =
          (V2NIMMessageImageAttachment) getMsgInternal().getAttachment();
      bounds = new int[] {attachment.getWidth(), attachment.getHeight()};
    }
    return bounds;
  }

  @Override
  protected float[] getCorners() {
    int corner = SizeUtils.dp2px(12);
    return new float[] {corner, corner, corner, corner};
  }
}
