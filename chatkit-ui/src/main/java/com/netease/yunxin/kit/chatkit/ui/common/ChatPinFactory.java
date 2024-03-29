// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;

/** PIN页面的ViewHolder工厂类 */
public class ChatPinFactory implements IChatViewHolderFactory {

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
      if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.custom) {
        CustomAttachment attachment =
            (CustomAttachment) messageBean.getMessageData().getMessage().getAttachment();
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
