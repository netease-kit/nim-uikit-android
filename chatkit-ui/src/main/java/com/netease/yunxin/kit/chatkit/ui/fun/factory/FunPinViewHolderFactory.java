// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.factory;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_FILE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatAudioPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatFilePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatImagePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatLocationPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatPinTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin.FunChatVideoPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;

public class FunPinViewHolderFactory implements IChatViewHolderFactory {

  @Override
  public ChatBaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    ChatBaseViewHolder viewHolder;
    FunChatBasePinViewHolderBinding viewHolderBinding =
        FunChatBasePinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == NORMAL_MESSAGE_VIEW_TYPE_AUDIO) {
      viewHolder = new FunChatAudioPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_IMAGE) {
      viewHolder = new FunChatImagePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_VIDEO) {
      viewHolder = new FunChatVideoPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_FILE) {
      viewHolder = new FunChatFilePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new FunChatLocationPinViewHolder(viewHolderBinding, viewType);
    } else {
      // default as text message
      viewHolder = new FunChatPinTextViewHolder(viewHolderBinding, viewType);
    }
    return viewHolder;
  }
}
