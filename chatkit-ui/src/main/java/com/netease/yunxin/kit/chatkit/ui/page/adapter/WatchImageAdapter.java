// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.WatchImageViewHolderBinding;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import java.util.List;

public class WatchImageAdapter
    extends RecyclerView.Adapter<WatchImageAdapter.WatchImageViewHolder> {
  private static final String TAG = "WatchImageVideo";

  private final Context mContext;
  private final List<V2NIMMessage> messageList;

  public WatchImageAdapter(Context context, List<V2NIMMessage> messages) {
    this.mContext = context;
    this.messageList = messages;
  }

  @NonNull
  @Override
  public WatchImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    WatchImageViewHolderBinding binding =
        WatchImageViewHolderBinding.inflate(LayoutInflater.from(mContext), parent, false);
    return new WatchImageViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull WatchImageViewHolder holder, int position) {
    V2NIMMessage message = messageList.get(position);
    if (message == null) {
      return;
    }
    handlePictureView(holder, message, position);
  }

  @Override
  public void onBindViewHolder(
      @NonNull WatchImageViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      if (payloads.get(0) == LoadStatus.Loading) {
        holder.binding.watchImageLoading.setVisibility(View.VISIBLE);
      } else {
        holder.binding.watchImageLoading.setVisibility(View.GONE);
        updateView(holder, messageList.get(position));
      }
    }
  }

  @Override
  public int getItemCount() {
    return messageList == null ? 0 : messageList.size();
  }

  @Override
  public int getItemViewType(int position) {
    return messageList.get(position).getMessageType().getValue();
  }

  private void handlePictureView(WatchImageViewHolder holder, V2NIMMessage message, int position) {
    holder.binding.watchPhotoView.setFullScreen(true, false);
    holder.binding.watchPhotoView.enableImageTransforms(true);
    holder.binding.watchPhotoView.setFirst(position == 0);
    holder.binding.watchPhotoView.setLast(position == getItemCount() - 1);

    if (!TextUtils.isEmpty(((V2NIMMessageImageAttachment) message.getAttachment()).getPath())) {
      holder.binding.watchImageLoading.setVisibility(View.GONE);
    } else {
      holder.binding.watchImageLoading.setVisibility(View.VISIBLE);
    }
    updateView(holder, message);
  }

  public void updateView(WatchImageViewHolder holder, V2NIMMessage message) {
    V2NIMMessageFileAttachment attachment = (V2NIMMessageFileAttachment) message.getAttachment();
    if (attachment instanceof V2NIMMessageVideoAttachment) {
      updateVideo(holder, message);
    } else {
      updateImage(holder, message);
    }
  }

  private void updateVideo(WatchImageViewHolder holder, V2NIMMessage message) {
    V2NIMMessageVideoAttachment attachment = (V2NIMMessageVideoAttachment) message.getAttachment();
    if (attachment == null) {
      return;
    }
    String path = MessageHelper.getMessageAttachPath(message);
    if (TextUtils.isEmpty(path)) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "updateImage path:" + path);
    holder.binding.watchPhotoView.setVisibility(View.VISIBLE);
    holder.binding.watchImageView.setVisibility(View.GONE);
    Bitmap bitmap = ImageUtils.getBitmap(path);
    int degree = ImageUtils.getRotateDegree(path);
    if (degree != 0) {
      bitmap = ImageUtils.rotate(bitmap, degree, 0, 0);
    }
    if (bitmap != null) {
      float initScale =
          bitmap.getWidth() > 0 ? ScreenUtils.getDisplayWidth() * 1f / bitmap.getWidth() : 1f;
      holder.binding.watchPhotoView.setMaxInitialScale(initScale);
      holder.binding.watchPhotoView.bindPhoto(bitmap);
    }
  }

  private void updateImage(WatchImageViewHolder holder, V2NIMMessage message) {
    V2NIMMessageImageAttachment attachment = (V2NIMMessageImageAttachment) message.getAttachment();
    if (attachment == null) {
      return;
    }
    String path = MessageHelper.getMessageAttachPath(message);
    if (TextUtils.isEmpty(path)) {
      path = attachment.getUrl();
    }
    holder.binding.watchPhotoView.setVisibility(View.GONE);
    holder.binding.watchImageView.setVisibility(View.VISIBLE);
    if (path != null) {
      Glide.with(holder.binding.watchImageView.getContext())
          .load(path)
          .into(holder.binding.watchImageView);
    }
  }

  static class WatchImageViewHolder extends RecyclerView.ViewHolder {
    WatchImageViewHolderBinding binding;

    public WatchImageViewHolder(@NonNull WatchImageViewHolderBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
