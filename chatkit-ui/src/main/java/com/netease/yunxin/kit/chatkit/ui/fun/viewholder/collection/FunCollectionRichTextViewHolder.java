// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection;

import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;

/** 收藏消息富文本消息view holder */
public class FunCollectionRichTextViewHolder extends FunCollectionBaseViewHolder {

  protected FunCollectionRichTextViewHolderBinding viewBinding;

  public FunCollectionRichTextViewHolder(
      @NonNull FunCollectionBaseViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    viewBinding =
        FunCollectionRichTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void onBindData(CollectionBean message, int position) {
    super.onBindData(message, position);
    if (message != null
        && message.getMessageData() != null
        && message.getCustomAttachment() instanceof RichTextAttachment) {
      RichTextAttachment attachment = (RichTextAttachment) message.getCustomAttachment();
      if (attachment != null) {
        viewBinding.messageTitle.setText(attachment.title);
        MessageHelper.identifyFaceExpression(
            viewBinding.getRoot().getContext(),
            viewBinding.messageContent,
            attachment.body,
            ImageSpan.ALIGN_BOTTOM);
        // 指定模式（例如只识别电话和邮箱）
        Linkify.addLinks(
            viewBinding.messageTitle,
            Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        Linkify.addLinks(
            viewBinding.messageContent,
            Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
      }
    }
  }
}
