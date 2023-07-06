// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin;

import static com.netease.yunxin.kit.chatkit.ui.ChatUIConstants.KEY_MAP_FOR_PIN;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
  static final long INTERVAL = 500L;
  ChatLocationPinViewHolderBinding binding;
  IChatMap chatMap;
  LocationAttachment attachment;
  Runnable renderAction =
      new Runnable() {
        @Override
        public void run() {
          if (binding != null) {
            addMapViewToGroup(binding.locationItemMapView);
          }
        }
      };
  final Handler renderHandler = new Handler(Looper.getMainLooper());

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
    binding.locationItemMapView.removeAllViews();
    if (ChatKitClient.getMessageMapProvider() != null) {
      return;
    }
    ImageView emptyImage = new ImageView(parent.getContext());
    emptyImage.setImageResource(R.drawable.ic_map_empty);
    binding.locationItemMapView.addView(emptyImage);
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    renderHandler.removeCallbacks(renderAction);
    ChatKitClient.getMessageMapProvider().destroyChatMap(KEY_MAP_FOR_PIN, chatMap);
    chatMap = null;
    binding.locationItemMapView.removeAllViews();
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    renderHandler.postDelayed(renderAction, INTERVAL);
  }

  private void addMapViewToGroup(ViewGroup group) {
    if (ChatKitClient.getMessageMapProvider() == null || attachment == null) {
      return;
    }
    chatMap =
        ChatKitClient.getMessageMapProvider()
            .createChatMap(KEY_MAP_FOR_PIN, parent.getContext(), null);
    View mapView =
        ChatKitClient.getMessageMapProvider()
            .setLocation(chatMap, attachment.getLatitude(), attachment.getLongitude());
    group.addView(mapView);
  }
}
