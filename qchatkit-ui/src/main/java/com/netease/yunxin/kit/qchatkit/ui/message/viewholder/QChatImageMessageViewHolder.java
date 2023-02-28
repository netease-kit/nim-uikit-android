// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.yunxin.kit.common.ui.widgets.ShapeDrawable;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.WatchImageActivity;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatImageMessageViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QchatBaseMessageViewHolderBinding;

public class QChatImageMessageViewHolder extends QChatBaseMessageViewHolder {

  private QChatImageMessageViewHolderBinding imageBinding;
  protected static int maxEdge = 0;
  protected static int minEdge = 0;

  public QChatImageMessageViewHolder(@NonNull QchatBaseMessageViewHolderBinding parent) {
    super(parent);
  }

  @Override
  public void addContainer() {
    imageBinding =
        QChatImageMessageViewHolderBinding.inflate(
            LayoutInflater.from(getParent().getContext()), getContainer(), true);
  }

  @Override
  public void bindData(QChatMessageInfo data, QChatMessageInfo lastMessage) {
    super.bindData(data, lastMessage);
    String imageAttachStr = data.getAttachStr();
    if (TextUtils.isEmpty(imageAttachStr)) {
      return;
    }
    ImageAttachment imageAttachment = new ImageAttachment(imageAttachStr);
    if (TextUtils.isEmpty(imageAttachment.getPath())
        && TextUtils.isEmpty(imageAttachment.getUrl())) {
      return;
    }
    data.setAttachment(imageAttachment);
    String path = imageAttachment.getPath();
    if (TextUtils.isEmpty(path)) {
      path = imageAttachment.getUrl();
    }

    int[] bounds = getBounds(path);
    int w = bounds[0];
    int h = bounds[1];
    if (w == 0 || h == 0) {
      w = getImageMaxEdge();
      h = getImageMaxEdge();
    }
    int thumbMinEdge = getImageThumbMinEdge();
    if (w < thumbMinEdge) {
      w = thumbMinEdge;
      h = bounds[0] != 0 ? w * bounds[1] / bounds[0] : 0;
    }
    int thumbMaxEdge = getImageMaxEdge();
    if (w > thumbMaxEdge) {
      w = thumbMaxEdge;
      h = w * bounds[1] / bounds[0];
    }

    FrameLayout.LayoutParams thumbParams =
        (FrameLayout.LayoutParams) imageBinding.getRoot().getLayoutParams();
    thumbParams.width = w;
    thumbParams.height = h;
    imageBinding.getRoot().setLayoutParams(thumbParams);

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
    imageBinding.getRoot().setBackground(shapeBuilder.build());

    Glide.with(itemView.getContext())
        .load(path)
        .apply(
            new RequestOptions()
                .transform(
                    new GranularRoundedCorners(corners[0], corners[1], corners[2], corners[3])))
        .override(w, h)
        .placeholder(R.drawable.bg_image_loading_qchat)
        .into(imageBinding.messageImage);
    imageBinding.messageImage.setOnClickListener(
        view -> WatchImageActivity.start(itemView.getContext(), data));
  }

  private int getImageMaxEdge() {
    if (maxEdge == 0) {
      maxEdge = (int) (222.0 / 320.0 * ScreenUtils.getDisplayWidth());
    }
    return maxEdge;
  }

  private int getImageThumbMinEdge() {
    if (minEdge == 0) {
      minEdge = (int) (111.0 / 375.0 * ScreenUtils.getDisplayWidth());
    }
    return minEdge;
  }

  protected float[] getCorners() {
    int corner = SizeUtils.dp2px(12);
    boolean msgIn = isReceivedMessage(currentMessage);
    float radiusTopLeft = msgIn ? 0 : corner;
    float radiusTopRight = msgIn ? corner : 0;
    return new float[] {radiusTopLeft, radiusTopRight, corner, corner};
  }

  protected int[] getBounds(String path) {
    int[] bounds = null;
    if (path != null) {
      bounds = ImageUtils.getSize(path);
    }
    if (bounds == null || bounds[0] == 0) {
      ImageAttachment attachment = (ImageAttachment) currentMessage.getAttachment();
      bounds = new int[] {attachment.getWidth(), attachment.getHeight()};
    }
    return bounds;
  }
}
