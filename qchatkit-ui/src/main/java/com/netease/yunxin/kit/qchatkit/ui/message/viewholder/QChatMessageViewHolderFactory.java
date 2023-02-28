// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QchatBaseMessageViewHolderBinding;

public class QChatMessageViewHolderFactory {
  public QChatBaseMessageViewHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {

    QChatBaseMessageViewHolder viewHolder = null;
    QchatBaseMessageViewHolderBinding viewHolderBinding =
        QchatBaseMessageViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == MsgTypeEnum.image.getValue()) {
      viewHolder = new QChatImageMessageViewHolder(viewHolderBinding);
    } else if (viewType == MsgTypeEnum.audio.getValue()) {
      viewHolder = new QChatAudioMessageViewHolder(viewHolderBinding, viewType);
    } else {
      viewHolder = new QChatTextMessageViewHolder(viewHolderBinding);
    }
    return viewHolder;
  }
}
