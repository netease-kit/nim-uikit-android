// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.pin;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatThumbnailPinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.widgets.ShapeDrawable;
import com.netease.yunxin.kit.common.utils.ScreenUtils;

/** view holder to show image/video thumb */
public abstract class ChatThumbPinViewHolder extends ChatBasePinViewHolder {
  private static final String TAG = "ChatThumbBaseViewHolder";

  ChatThumbnailPinViewHolderBinding binding;

  public ChatThumbPinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  protected IMMessage getMsgInternal() {
    return currentMessage.getMessageData().getMessage();
  }

  @Override
  public void addContainer() {
    binding =
        ChatThumbnailPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    binding.thumbnail.setOnClickListener(
        v -> itemClickListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void bindData(ChatMessageBean message, int position) {
    super.bindData(message, position);

    load();
  }

  @Override
  protected void onMessageStatus(ChatMessageBean data) {
    super.onMessageStatus(data);
    load();
  }

  private void load() {
    FileAttachment attachment = (FileAttachment) getMsgInternal().getAttachment();
    if (attachment == null) {
      return;
    }
    String path = attachment.getPath();
    String thumbPath = attachment.getThumbPath();
    if (!TextUtils.isEmpty(thumbPath)) {
      ALog.d(TAG, "load from thumb");
      loadThumbnailImage(thumbPath);
    } else if (!TextUtils.isEmpty(path)) {
      ALog.d(TAG, "load from path");
      loadThumbnailImage(thumbFromSourceFile(path));
    } else {
      loadThumbnailInternal(null, getImageThumbMinEdge(), getImageThumbMinEdge());
    }
  }

  private void loadThumbnailImage(String path) {
    int[] bounds = getBounds(path);
    int w = bounds[0];
    int h = bounds[1];
    int thumbMinEdge = getImageThumbMinEdge();
    if (w < thumbMinEdge) {
      w = thumbMinEdge;
      h = bounds[0] != 0 ? w * bounds[1] / bounds[0] : 0;
    }
    int thumbMaxEdge = getImageThumbMaxEdge();
    if (w > thumbMaxEdge) {
      w = thumbMaxEdge;
      h = w * bounds[1] / bounds[0];
    }

    loadThumbnailInternal(path, w, h);
  }

  private void loadThumbnailInternal(String path, int w, int h) {
    FrameLayout.LayoutParams thumbParams =
        (FrameLayout.LayoutParams) binding.getRoot().getLayoutParams();
    thumbParams.width = w;
    thumbParams.height = h;
    binding.getRoot().setLayoutParams(thumbParams);

    // change container's background, stroke the thumbnail
    float[] corners = getCorners();
    ShapeDrawable.Builder shapeBuilder =
        new ShapeDrawable.Builder()
            .setStroke(1, ContextCompat.getColor(getContainer().getContext(), R.color.color_e2e5e8))
            .setRadii(
                new float[] {
                  corners[0],
                  corners[0],
                  corners[1],
                  corners[1],
                  corners[2],
                  corners[2],
                  corners[3],
                  corners[3]
                });
    if (path == null) {
      shapeBuilder.setSolid(Color.BLACK);
    }
    binding.getRoot().setBackground(shapeBuilder.build());

    if (path != null) {
      Glide.with(binding.thumbnail.getContext())
          .load(path)
          .apply(
              new RequestOptions()
                  .transform(
                      new GranularRoundedCorners(corners[0], corners[1], corners[2], corners[3])))
          .override(w, h)
          .into(binding.thumbnail);
    }
  }

  private int getImageThumbMinEdge() {
    return (int) (111.0 / 375.0 * ScreenUtils.getDisplayWidth());
  }

  private int getImageThumbMaxEdge() {
    return (int) (222.0 / 375.0 * ScreenUtils.getDisplayWidth());
  }

  protected abstract String thumbFromSourceFile(String path);

  /** @return [width, height] */
  protected abstract int[] getBounds(String path);

  /** @return [leftTop, rightTop, leftBottom, rightBottom] */
  protected abstract float[] getCorners();
}
