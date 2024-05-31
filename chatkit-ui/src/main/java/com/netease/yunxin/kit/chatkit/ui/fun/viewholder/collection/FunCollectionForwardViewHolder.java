// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionForwardViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;

/** 收藏合并转发消息 */
public class FunCollectionForwardViewHolder extends FunCollectionBaseViewHolder {

  // 合并转发消息UI
  FunCollectionForwardViewHolderBinding viewBinding;

  public FunCollectionForwardViewHolder(
      @NonNull FunCollectionBaseViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }
  // 添加布局到容器
  @Override
  public void addContainer() {
    viewBinding =
        FunCollectionForwardViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void onBindData(CollectionBean message, int position) {
    super.onBindData(message, position);
    if (message != null && message.getCustomAttachment() instanceof MultiForwardAttachment) {
      MultiForwardAttachment attachment = (MultiForwardAttachment) message.getCustomAttachment();
      String titleText =
          String.format(
              getContainer().getContext().getString(R.string.chat_message_multi_record_title),
              attachment.sessionName);
      viewBinding.messageMultiTitle.setText(titleText);
      if (attachment.abstractsList != null) {
        String contentFormat =
            getContainer().getContext().getString(R.string.chat_message_multi_record_content);
        SpannableStringBuilder textBuilder = new SpannableStringBuilder();
        for (int i = 0; i < attachment.abstractsList.size(); i++) {
          String content =
              String.format(
                  contentFormat,
                  ChatUtils.getEllipsizeMiddleNick(attachment.abstractsList.get(i).senderNick),
                  attachment.abstractsList.get(i).content);
          SpannableString sb =
              MessageHelper.replaceEmoticons(
                  parent.getContext(), content, MessageHelper.DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
          textBuilder.append(sb);
          if (i < attachment.abstractsList.size() - 1) {
            textBuilder.append("\n");
          }
        }
        viewBinding.messageText.setText(textBuilder);
      }
    }
  }
}
