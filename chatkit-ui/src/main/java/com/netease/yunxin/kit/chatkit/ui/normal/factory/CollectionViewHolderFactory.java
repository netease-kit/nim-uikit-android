// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.factory;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.common.CollectionViewHolderBaseFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.CollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionAudioViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionFileViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionForwardViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionImageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionLocationViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionRichTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.collection.CollectionVideoViewHolder;

/** 会话Pin列表页面消息ViewHolder工厂类。 */
public class CollectionViewHolderFactory extends CollectionViewHolderBaseFactory {

  @Override
  public ChatBaseViewHolder createNormalViewHolder(@NonNull ViewGroup parent, int viewType) {
    CollectionBaseViewHolderBinding viewHolderBinding =
        CollectionBaseViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    CollectionBaseViewHolder viewHolder = null;
    if (viewType == ChatMessageType.TEXT_MESSAGE_VIEW_TYPE) {
      viewHolder = new CollectionTextViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.FILE_MESSAGE_VIEW_TYPE) {
      viewHolder = new CollectionFileViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.IMAGE_MESSAGE_VIEW_TYPE) {
      viewHolder = new CollectionImageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.VIDEO_MESSAGE_VIEW_TYPE) {
      viewHolder = new CollectionVideoViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new CollectionLocationViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.AUDIO_MESSAGE_VIEW_TYPE) {
      viewHolder = new CollectionAudioViewHolder(viewHolderBinding, viewType);
    } else {
      viewHolder = new CollectionTextViewHolder(viewHolderBinding, viewType);
    }
    return viewHolder;
  }

  @Override
  protected ChatBaseViewHolder createCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
    CollectionBaseViewHolderBinding viewHolderBinding =
        CollectionBaseViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT) {
      return new CollectionForwardViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.RICH_TEXT_ATTACHMENT) {
      return new CollectionRichTextViewHolder(viewHolderBinding, viewType);
    }
    return null;
  }
}
