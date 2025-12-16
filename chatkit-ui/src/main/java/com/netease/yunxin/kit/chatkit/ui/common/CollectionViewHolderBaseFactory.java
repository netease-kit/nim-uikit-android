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
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;

/** 收藏页面的ViewHolder工厂类 */
public class CollectionViewHolderBaseFactory implements IChatViewHolderFactory<CollectionBean> {

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

  public int getCustomViewType(CollectionBean messageBean) {
    if (messageBean != null && messageBean.getMessageData() != null) {
      if (messageBean.getMessageData().getMessageType()
          == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
        CustomAttachment attachment = messageBean.getCustomAttachment();
        if (attachment != null) {
          return attachment.getType();
        }
      }
    }
    return -1;
  }

  @Override
  public int getItemViewType(CollectionBean messageBean) {
    if (messageBean != null) {
      int customViewType = getCustomViewType(messageBean);
      if (customViewType > 0) {
        return customViewType;
      } else {
        return messageBean.getMessageType();
      }
    }
    return 0;
  }
}
