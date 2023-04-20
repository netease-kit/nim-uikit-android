// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.pin;

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
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatLocationPinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public class ChatLocationPinViewHolder extends ChatBasePinViewHolder {
  public static final String TAG = "ChatLocationMessageViewHolder";

  ChatLocationPinViewHolderBinding binding;
  IChatMap chatMap;

  public ChatLocationPinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    binding =
        ChatLocationPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    if (ChatKitClient.getMessageMapProvider() != null) {
      chatMap = ChatKitClient.getMessageMapProvider().createChatMap(parent.getContext(), null);
    }
    binding.locationClick.setOnClickListener(
        v -> itemClickListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void bindData(ChatMessageBean message, int position) {
    ALog.d(
        ChatKitUIConstant.LIB_TAG,
        TAG,
        "bindData" + "title" + message.getMessageData().getMessage().getContent());
    super.bindData(message, position);
    LocationAttachment attachment =
        (LocationAttachment) message.getMessageData().getMessage().getAttachment();
    if (attachment == null) {
      return;
    }
    binding.locationItemTitle.setText(message.getMessageData().getMessage().getContent());
    binding.locationItemAddress.setText(attachment.getAddress());
    binding.locationItemMapView.removeAllViews();
    if (ChatKitClient.getMessageMapProvider() != null && chatMap != null) {
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
}
