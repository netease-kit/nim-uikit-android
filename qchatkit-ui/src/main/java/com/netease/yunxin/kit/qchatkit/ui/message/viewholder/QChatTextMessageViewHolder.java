// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatTextMessageViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QchatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.utils.MessageUtil;

public class QChatTextMessageViewHolder extends QChatBaseMessageViewHolder {

  private QChatTextMessageViewHolderBinding textBinding;

  public QChatTextMessageViewHolder(@NonNull QchatBaseMessageViewHolderBinding parent) {
    super(parent);
  }

  @Override
  public void addContainer() {
    textBinding =
        QChatTextMessageViewHolderBinding.inflate(
            LayoutInflater.from(getParent().getContext()), getContainer(), true);
  }

  @Override
  public void bindData(QChatMessageInfo data, QChatMessageInfo lastMessage) {
    super.bindData(data, lastMessage);
    if (data.getMessage().getMsgType() == MsgTypeEnum.text) {
      MessageUtil.identifyFaceExpression(
          textBinding.getRoot().getContext(),
          textBinding.messageText,
          data.getMessage().getContent(),
          ImageSpan.ALIGN_BOTTOM);
    } else {
      //文件消息暂不支持所以展示提示信息
      textBinding.messageText.setText(
          textBinding
              .getRoot()
              .getContext()
              .getResources()
              .getString(R.string.qchat_message_not_support_tips));
    }
    textBinding.messageText.setOnLongClickListener(
        v -> {
          ClipboardManager cmb =
              (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
          ClipData clipData = ClipData.newPlainText(null, data.getContent());
          cmb.setPrimaryClip(clipData);
          if (optionCallBack != null) {
            optionCallBack.onCopy(data);
          }
          return true;
        });
  }
}
