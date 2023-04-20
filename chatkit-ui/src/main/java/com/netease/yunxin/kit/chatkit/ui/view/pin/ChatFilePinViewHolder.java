// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.pin;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatFilePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.utils.FileUtils;

public class ChatFilePinViewHolder extends ChatBasePinViewHolder {

  private static final String TAG = "ChatFileViewHolder";
  private ChatFilePinViewHolderBinding binding;
  private static final int PROGRESS_MAX = 100;

  public ChatFilePinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    super.addContainer();
    binding =
        ChatFilePinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, int position) {
    super.bindData(message, position);
    loadData();
  }

  protected IMMessage getMsgInternal() {
    return currentMessage.getMessageData().getMessage();
  }

  @Override
  protected void onMessageStatus(ChatMessageBean data) {
    super.onMessageStatus(data);
    loadData();
  }

  private void loadData() {
    FileAttachment attachment = (FileAttachment) getMsgInternal().getAttachment();
    if (attachment == null) {
      return;
    }
    binding.displayName.setText(attachment.getDisplayName());
    binding.displaySize.setText(ChatUtils.formatFileSize(attachment.getSize()));
    String fileType = attachment.getExtension();
    if (TextUtils.isEmpty(fileType)) {
      fileType = FileUtils.getFileExtension(attachment.getDisplayName());
    }
    if (properties != null
        && properties.fileDrawable != null
        && properties.fileDrawable.containsKey(fileType)) {
      binding.fileTypeIv.setImageDrawable(properties.fileDrawable.get(fileType));
    } else {
      binding.fileTypeIv.setImageResource(ChatUtils.getFileIcon(fileType));
    }
    ALog.d(LIB_TAG, TAG, "file:" + fileType + "name:" + attachment.getDisplayName());
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
    updateProgress((int) data.getLoadProgress());
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
}
