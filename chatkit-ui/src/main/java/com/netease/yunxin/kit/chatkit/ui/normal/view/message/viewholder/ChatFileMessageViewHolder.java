// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageFileViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.ChatMessageViewHolderUIOptions;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.MessageStatusUIOption;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtils;

public class ChatFileMessageViewHolder extends NormalChatBaseMessageViewHolder {

  private static final String TAG = "ChatFileViewHolder";
  private NormalChatMessageFileViewHolderBinding binding;
  private static final int PROGRESS_MAX = 100;

  public ChatFileMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    super.addViewToMessageContainer();
    binding =
        NormalChatMessageFileViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContentContainer(), true);
    ViewGroup.LayoutParams params = binding.placeHolder.getLayoutParams();
    params.width = ScreenUtils.getDisplayWidth();
  }

  @Override
  protected void onLayoutConfig(ChatMessageBean messageBean) {
    super.onLayoutConfig(messageBean);
    ViewGroup.MarginLayoutParams rootParams =
        (ViewGroup.MarginLayoutParams) binding.getRoot().getLayoutParams();
    boolean hasReaction = !messageBean.getReactionState().summarize().isEmpty();
    int horizontalInset =
        hasReaction ? parent.getResources().getDimensionPixelSize(R.dimen.dimen_12_dp) : 0;
    rootParams.setMarginStart(horizontalInset);
    rootParams.setMarginEnd(horizontalInset);
    binding.getRoot().setLayoutParams(rootParams);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    loadData();
  }

  protected V2NIMMessage getMsgInternal() {
    return currentMessage.getMessageData().getMessage();
  }

  @Override
  protected void onMessageStatus(ChatMessageBean data) {
    super.onMessageStatus(data);
    loadData();
  }

  private void loadData() {
    V2NIMMessageFileAttachment attachment =
        (V2NIMMessageFileAttachment) getMsgInternal().getAttachment();
    if (attachment == null) {
      return;
    }
    if (!TextUtils.isEmpty(currentMessage.keyword)) {
      MessageHelper.identifyHighlight(
          parent.getContext(),
          binding.displayName,
          attachment.getName(),
          currentMessage.getKeyword(),
          ContextCompat.getColor(parent.getContext(), R.color.color_chat_message_highlight));
    } else {
      binding.displayName.setText(attachment.getName());
    }
    binding.displaySize.setText(ChatUtils.formatFileSize(attachment.getSize()));
    String fileType = attachment.getExt();
    if (TextUtils.isEmpty(fileType)) {
      fileType = FileUtils.getFileExtension(attachment.getName());
    }
    if (fileType.startsWith(".")) {
      fileType = fileType.substring(1);
    }
    if (properties != null
        && properties.fileDrawable != null
        && properties.fileDrawable.containsKey(fileType)) {
      binding.fileTypeIv.setImageDrawable(properties.fileDrawable.get(fileType));
    } else {
      binding.fileTypeIv.setImageResource(ChatUtils.getFileIcon(fileType));
    }
    ALog.d(LIB_TAG, TAG, "file:" + fileType + "name:" + attachment.getName());
  }

  @Override
  protected void onProgressUpdate(ChatMessageBean data) {
    super.onProgressUpdate(data);
    binding.progressBar.setIndeterminate(false);
    ALog.d(
        LIB_TAG,
        TAG,
        "onProgressUpdate:"
            + data.getLoadProgress()
            + "message="
            + data.hashCode()
            + "PR:"
            + data.progress);
    updateProgress(data.progress);
  }

  private void updateProgress(int progress) {
    ALog.d(LIB_TAG, TAG, "updateProgress:" + progress);
    if (progress >= PROGRESS_MAX) {
      // finish
      binding.fileProgressFl.setVisibility(View.GONE);
      binding.progressBar.setVisibility(View.GONE);
      binding.progressBarInsideIcon.setVisibility(View.GONE);
    } else {
      binding.fileProgressFl.setVisibility(View.VISIBLE);
      binding.progressBar.setVisibility(View.VISIBLE);
      binding.progressBarInsideIcon.setVisibility(View.VISIBLE);
      binding.progressBar.setProgress(progress);
    }
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    boolean hasReaction = !messageBean.getReactionState().summarize().isEmpty();
    // 文件主体在有无 Reaction、发送和接收状态下都保留独立的白色圆角边框。
    binding.getRoot().setBackgroundResource(R.drawable.bg_shape_corner);
    if (!hasReaction) {
      // 无 Reaction 时保留文件消息原有的透明外层背景。
      baseViewBinding.messageContentGroup.setBackgroundResource(R.color.title_transfer);
    }
  }

  @Override
  protected ChatMessageViewHolderUIOptions provideUIOptions(ChatMessageBean messageBean) {
    MessageStatusUIOption messageStatusUIOption = new MessageStatusUIOption();
    messageStatusUIOption.showSendingStatus = false;
    return ChatMessageViewHolderUIOptions.wrapExitsOptions(super.provideUIOptions(messageBean))
        .messageStatusUIOption(messageStatusUIOption)
        .build();
  }
}
