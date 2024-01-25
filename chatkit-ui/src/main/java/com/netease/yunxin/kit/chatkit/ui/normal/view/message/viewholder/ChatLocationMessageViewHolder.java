// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MAP_FOR_MESSAGE;

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
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageLocationViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public class ChatLocationMessageViewHolder extends NormalChatBaseMessageViewHolder {
  public static final String TAG = "ChatLocationMessageViewHolder";
  static final long INTERVAL = 500L;
  NormalChatMessageLocationViewHolderBinding binding;
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

  public ChatLocationMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    binding =
        NormalChatMessageLocationViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    ALog.d(
        ChatKitUIConstant.LIB_TAG,
        TAG,
        "bindData" + "title" + message.getMessageData().getMessage().getContent());
    super.bindData(message, lastMessage);
    attachment = (LocationAttachment) message.getMessageData().getMessage().getAttachment();
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
    if (ChatKitClient.getMessageMapProvider() != null) {
      ChatKitClient.getMessageMapProvider().destroyChatMap(KEY_MAP_FOR_MESSAGE, chatMap);
    }
    chatMap = null;
    binding.locationItemMapView.removeAllViews();
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    renderHandler.postDelayed(renderAction, INTERVAL);
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    if (!messageBean.isRevoked()) {
      boolean isReceivedMsg = MessageHelper.isReceivedMessage(messageBean);
      if (isReceivedMsg) {
        baseViewBinding.contentWithTopLayer.setBackgroundResource(
            R.drawable.chat_message_stroke_other_bg);
      } else {
        baseViewBinding.contentWithTopLayer.setBackgroundResource(
            R.drawable.chat_message_stoke_self_bg);
      }
    }
  }

  private void addMapViewToGroup(ViewGroup group) {
    if (ChatKitClient.getMessageMapProvider() == null || attachment == null) {
      return;
    }
    chatMap =
        ChatKitClient.getMessageMapProvider()
            .createChatMap(KEY_MAP_FOR_MESSAGE, parent.getContext(), null);
    View mapView =
        ChatKitClient.getMessageMapProvider()
            .setLocation(chatMap, attachment.getLatitude(), attachment.getLongitude());
    group.addView(mapView);
  }
}
