// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** PIN页面的ViewHolder工厂类 */
public class ChatPinFactory implements IChatViewHolderFactory<ChatMessageBean> {

  @Override
  public ChatBaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {
    ChatBaseViewHolder viewHolder = createCustomViewHolder(parent, viewType);
    if (viewHolder == null) {
      viewHolder = createNormalViewHolder(parent, viewType);
    }
    return viewHolder;
  }

  protected ChatBaseViewHolder createCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
    return null;
  }

  public ChatBaseViewHolder createNormalViewHolder(@NonNull ViewGroup parent, int viewType) {

    return null;
  }

  public int getCustomViewType(ChatMessageBean messageBean) {
    if (messageBean != null) {
      if (messageBean.getMessageData().getMessage().getMessageType()
          == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
        CustomAttachment attachment =
            (CustomAttachment) messageBean.getMessageData().getAttachment();
        if (attachment != null) {
          return attachment.getType();
        }
      }
    }
    return -1;
  }

  @Override
  public int getItemViewType(ChatMessageBean messageBean) {
    if (messageBean != null) {
      int customViewType = getCustomViewType(messageBean);
      if (customViewType > 0) {
        return customViewType;
      } else {
        return messageBean.getViewType();
      }
    }
    return 0;
  }
}
