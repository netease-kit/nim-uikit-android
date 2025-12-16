// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.collection;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageLocationAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionBaseViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunCollectionLocationViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;

/** 收藏消息位置消息view holder */
public class FunCollectionLocationViewHolder extends FunCollectionBaseViewHolder {
  public static final String TAG = "FunCollectionLocationViewHolder";
  static final long INTERVAL = 500L;
  FunCollectionLocationViewHolderBinding binding;
  V2NIMMessageLocationAttachment attachment;

  public FunCollectionLocationViewHolder(
      @NonNull FunCollectionBaseViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    binding =
        FunCollectionLocationViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
  }

  @Override
  public void onBindData(CollectionBean message, int position) {
    ALog.d(
        ChatKitUIConstant.LIB_TAG, TAG, "bindData" + "title" + message.getMessageData().getText());
    super.onBindData(message, position);
    attachment = (V2NIMMessageLocationAttachment) message.getMessageData().getAttachment();
    if (attachment == null) {
      return;
    }
    binding.locationItemTitle.setText(message.getMessageData().getText());
    binding.locationItemAddress.setText(attachment.getAddress());

    String url = null;
    if (ChatKitClient.getMessageMapProvider() != null) {
      url =
          ChatKitClient.getMessageMapProvider()
              .getChatMpaItemImage(attachment.getLatitude(), attachment.getLongitude());
    }
    if (!TextUtils.isEmpty(url) && binding.locationItemMapView.getContext() != null) {
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
