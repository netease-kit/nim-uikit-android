// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.factory;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.common.CollectionViewHolderBaseFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionAudioViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionFileViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionForwardViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionImageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionLocationViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionRichTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection.FunCollectionVideoViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;

/** 会话Pin列表页面消息ViewHolder工厂类。 */
public class FunCollectionViewHolderFactory extends CollectionViewHolderBaseFactory {

  @Override
  public ChatBaseViewHolder createNormalViewHolder(@NonNull ViewGroup parent, int viewType) {
    FunCollectionBaseViewHolderBinding viewHolderBinding =
        FunCollectionBaseViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    FunCollectionBaseViewHolder viewHolder = null;
    if (viewType == ChatMessageType.TEXT_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunCollectionTextViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.FILE_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunCollectionFileViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.IMAGE_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunCollectionImageViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.VIDEO_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunCollectionVideoViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunCollectionLocationViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.AUDIO_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunCollectionAudioViewHolder(viewHolderBinding, viewType);
    } else {
      viewHolder = new FunCollectionTextViewHolder(viewHolderBinding, viewType);
    }
    return viewHolder;
  }

  @Override
  protected ChatBaseViewHolder createCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
    FunCollectionBaseViewHolderBinding viewHolderBinding =
        FunCollectionBaseViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT) {
      return new FunCollectionForwardViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.RICH_TEXT_ATTACHMENT) {
      return new FunCollectionRichTextViewHolder(viewHolderBinding, viewType);
    }
    return null;
  }
}
