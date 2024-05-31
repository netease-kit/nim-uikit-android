// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.factory;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.AUDIO_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.FILE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.IMAGE_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.VIDEO_MESSAGE_VIEW_TYPE;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.common.ChatPinFactory;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatAudioPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatFilePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatForwardPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatImagePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatLocationPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatRichTextPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatTextPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatVideoPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;

/** Fun皮肤聊天界面Pin消息ViewHolder工厂类，用于创建自定义消息的ViewHolder 根据消息类型返回对应的ViewHolder */
public class FunPinViewHolderFactory extends ChatPinFactory {

  /**
   * 内置消息类型
   *
   * @return FunChatViewHolderFactory
   */
  @Override
  public ChatBaseViewHolder createNormalViewHolder(@NonNull ViewGroup parent, int viewType) {

    ChatBaseViewHolder viewHolder;
    FunChatBasePinViewHolderBinding viewHolderBinding =
        FunChatBasePinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == AUDIO_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunChatAudioPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == IMAGE_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunChatImagePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == VIDEO_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunChatVideoPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == FILE_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunChatFilePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunChatLocationPinViewHolder(viewHolderBinding, viewType);
    } else {
      // default as text message
      viewHolder = new FunChatTextPinViewHolder(viewHolderBinding, viewType);
    }
    return viewHolder;
  }

  /**
   * 自定义消息类型
   *
   * @return FunChatViewHolderFactory
   */
  @Override
  protected ChatBaseViewHolder createCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT) {
      FunChatBasePinViewHolderBinding viewHolderBinding =
          FunChatBasePinViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new FunChatForwardPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.RICH_TEXT_ATTACHMENT) {

      FunChatBasePinViewHolderBinding viewHolderBinding =
          FunChatBasePinViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new FunChatRichTextPinViewHolder(viewHolderBinding, viewType);
    }
    return null;
  }
}
