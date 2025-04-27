// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection;

import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.CollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.CollectionTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.ui.view.MarkDownViwUtils;

/** 收藏消息文本消息view holder */
public class CollectionTextViewHolder extends CollectionBaseViewHolder {

  CollectionTextViewHolderBinding textBinding;

  public CollectionTextViewHolder(@NonNull CollectionBaseViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    textBinding =
        CollectionTextViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void onBindData(CollectionBean message, int position) {
    super.onBindData(message, position);
    if (message.getMessageData() != null
        && message.getMessageData().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      if (MessageHelper.isAIResponseMessage(message.getMessageInfo())) {
        MarkDownViwUtils.makeMarkDown(
            textBinding.getRoot().getContext(),
            textBinding.messageText,
            message.getMessageData().getText());
      } else {
        MessageHelper.identifyFaceExpression(
            textBinding.getRoot().getContext(),
            textBinding.messageText,
            message.getMessageData().getText(),
            ImageSpan.ALIGN_BOTTOM);
      }
    } else {
      //文件消息暂不支持所以展示提示信息
      textBinding.messageText.setText(
          parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
    }
  }
}
