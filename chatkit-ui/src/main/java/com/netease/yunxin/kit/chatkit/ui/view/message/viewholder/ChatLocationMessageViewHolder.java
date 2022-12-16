// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.IChatMap;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageLocationViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public class ChatLocationMessageViewHolder extends ChatBaseMessageViewHolder {
  public static final String TAG = "ChatLocationMessageViewHolder";

  ChatMessageLocationViewHolderBinding binding;
  IChatMap chatMap;

  public ChatLocationMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    binding =
        ChatMessageLocationViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    chatMap = ChatKitClient.getMessageMapProvider().createChatMap(parent.getContext(), null);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    ALog.i(ChatKitUIConstant.LIB_TAG, TAG, "bindData");
    super.bindData(message, lastMessage);
    LocationAttachment attachment =
        (LocationAttachment) message.getMessageData().getMessage().getAttachment();
    if (attachment == null) {
      return;
    }
    binding.locationItemTitle.setText(message.getMessageData().getMessage().getContent());
    binding.locationItemAddress.setText(attachment.getAddress());
    binding.locationItemMapView.removeAllViews();
    if (itemClickListener != null) {
      binding.locationClick.setOnClickListener(
          v -> itemClickListener.onMessageClick(v, position, currentMessage));
    }

    if (ChatKitClient.getMessageMapProvider() != null) {
      View view =
          ChatKitClient.getMessageMapProvider()
              .setLocation(chatMap, attachment.getLatitude(), attachment.getLongitude());
      binding.locationItemMapView.addView(view);
      return;
    }
    ImageView emptyImage = new ImageView(parent.getContext());
    emptyImage.setImageResource(R.drawable.ic_map_empty);
    binding.locationItemMapView.addView(emptyImage);
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    chatMap.onDestroy();
  }
}
