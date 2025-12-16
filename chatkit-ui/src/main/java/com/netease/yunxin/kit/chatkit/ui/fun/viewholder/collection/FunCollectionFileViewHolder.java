// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionFileViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.common.utils.FileUtils;

public class FunCollectionFileViewHolder extends FunCollectionBaseViewHolder {

  private static final String TAG = "ChatFileCollectionViewHolder";
  private FunCollectionFileViewHolderBinding binding;
  private static final int PROGRESS_MAX = 100;

  public FunCollectionFileViewHolder(
      @NonNull FunCollectionBaseViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    super.addContainer();
    binding =
        FunCollectionFileViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void onBindData(CollectionBean message, int position) {
    super.onBindData(message, position);
    loadData();
  }

  protected V2NIMMessage getMsgInternal() {
    return currentCollection.getMessageData();
  }

  private void loadData() {
    V2NIMMessageFileAttachment attachment =
        (V2NIMMessageFileAttachment) getMsgInternal().getAttachment();
    if (attachment == null) {
      return;
    }
    binding.displayName.setText(attachment.getName());
    binding.displaySize.setText(ChatUtils.formatFileSize(attachment.getSize()));
    String fileType = attachment.getExt();
    if (TextUtils.isEmpty(fileType)) {
      fileType = FileUtils.getFileExtension(attachment.getName());
    }
    if (fileType.startsWith(".")) {
      fileType = fileType.substring(1);
    }
    binding.fileTypeIv.setImageResource(ChatUtils.getFileIcon(fileType));
    ALog.d(LIB_TAG, TAG, "file:" + fileType + "name:" + attachment.getName());
  }

  @Override
  protected void onProgressUpdate(CollectionBean data) {
    super.onProgressUpdate(data);
    binding.progressBar.setIndeterminate(false);
    ALog.d(LIB_TAG, TAG, "onProgressUpdate:" + data.getLoadProgress());
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
