// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
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
  static final long INTERVAL = 500L;
  ChatLocationPinViewHolderBinding binding;
  IChatMap chatMap;
  LocationAttachment attachment;

  public ChatLocationPinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    binding =
        ChatLocationPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    binding.locationClick.setOnClickListener(
        v -> itemListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    ALog.d(
        ChatKitUIConstant.LIB_TAG,
        TAG,
        "bindData" + "title" + message.getMessageData().getMessage().getContent());
    super.onBindData(message, position);
    attachment = (LocationAttachment) message.getMessageData().getMessage().getAttachment();
    if (attachment == null) {
      return;
    }
    binding.locationItemTitle.setText(message.getMessageData().getMessage().getContent());
    binding.locationItemAddress.setText(attachment.getAddress());

    String url = null;
    if (ChatKitClient.getMessageMapProvider() != null) {
      url =
          ChatKitClient.getMessageMapProvider()
              .getChatMpaItemImage(attachment.getLatitude(), attachment.getLongitude());
    }
    if (!TextUtils.isEmpty(url)) {
      binding.locationMapMarkerIv.setVisibility(View.VISIBLE);
      binding.locationItemMapIv.setVisibility(View.VISIBLE);
      Glide.with(binding.locationItemMapView.getContext())
          .load(url)
          .into(binding.locationItemMapIv)
          .onLoadFailed(
              parent.getContext().getResources().getDrawable(R.drawable.ic_chat_location_default));
    }

    if (TextUtils.isEmpty(url)) {
      binding.locationItemMapIv.setImageResource(R.drawable.ic_chat_location_default);
      binding.locationMapMarkerIv.setVisibility(View.GONE);
    }
  }
}
