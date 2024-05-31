// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.factory;

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
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatAudioPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatFilePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatForwardPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatImagePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatLocationPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatPinTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatRichTextPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatVideoPinViewHolder;

/** 会话Pin列表页面消息ViewHolder工厂类。 */
public class PinViewHolderFactory extends ChatPinFactory {

  @Override
  public ChatBaseViewHolder createNormalViewHolder(@NonNull ViewGroup parent, int viewType) {
    ChatBaseViewHolder viewHolder;
    ChatBasePinViewHolderBinding viewHolderBinding =
        ChatBasePinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == AUDIO_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatAudioPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == IMAGE_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatImagePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == VIDEO_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatVideoPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == FILE_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatFilePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatLocationPinViewHolder(viewHolderBinding, viewType);
    } else {
      // default as text message
      viewHolder = new ChatPinTextViewHolder(viewHolderBinding, viewType);
    }
    return viewHolder;
  }

  @Override
  protected ChatBaseViewHolder createCustomViewHolder(@NonNull ViewGroup parent, int viewType) {

    if (viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT) {
      ChatBasePinViewHolderBinding viewHolderBinding =
          ChatBasePinViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ChatForwardPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == ChatMessageType.RICH_TEXT_ATTACHMENT) {

      ChatBasePinViewHolderBinding viewHolderBinding =
          ChatBasePinViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ChatRichTextPinViewHolder(viewHolderBinding, viewType);
    }
    return null;
  }
}
