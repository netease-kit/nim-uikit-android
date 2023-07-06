// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.factory;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_FILE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatAudioPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatFilePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatImagePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatLocationPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatPinTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin.ChatVideoPinViewHolder;

public class PinViewHolderFactory implements IChatViewHolderFactory {

  @Override
  public ChatBaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    ChatBaseViewHolder viewHolder;
    ChatBasePinViewHolderBinding viewHolderBinding =
        ChatBasePinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == NORMAL_MESSAGE_VIEW_TYPE_AUDIO) {
      viewHolder = new ChatAudioPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_IMAGE) {
      viewHolder = new ChatImagePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_VIDEO) {
      viewHolder = new ChatVideoPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_FILE) {
      viewHolder = new ChatFilePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatLocationPinViewHolder(viewHolderBinding, viewType);
    } else {
      //default as text message
      viewHolder = new ChatPinTextViewHolder(viewHolderBinding, viewType);
    }
    return viewHolder;
  }
}
