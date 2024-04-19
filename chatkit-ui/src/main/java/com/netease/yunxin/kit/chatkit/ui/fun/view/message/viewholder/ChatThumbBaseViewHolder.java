// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ThumbHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageThumbnailViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.media.GranularRoundedCornersWithCenterCrop;
import com.netease.yunxin.kit.common.ui.widgets.ShapeDrawable;
import com.netease.yunxin.kit.common.utils.ScreenUtils;

/** view holder to show image/video thumb */
public abstract class ChatThumbBaseViewHolder extends FunChatBaseMessageViewHolder {
  private static final String TAG = "ChatThumbBaseViewHolder";

  FunChatMessageThumbnailViewHolderBinding binding;

  public ChatThumbBaseViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  protected V2NIMMessage getMsgInternal() {
    return currentMessage.getMessageData().getMessage();
  }

  @Override
  public void addViewToMessageContainer() {
    binding =
        FunChatMessageThumbnailViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);

    load();
  }

  @Override
  protected void onMessageStatus(ChatMessageBean data) {
    super.onMessageStatus(data);
    load();
  }

  private void load() {
    V2NIMMessageFileAttachment attachment =
        (V2NIMMessageFileAttachment) getMsgInternal().getAttachment();
    if (attachment == null) {
      return;
    }
    String path = attachment.getPath();
    //图片消息优先加载本地
    if (getMsgInternal().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      V2NIMMessageImageAttachment imageAttachment = (V2NIMMessageImageAttachment) attachment;
      if (!TextUtils.isEmpty(path)) {
        loadThumbnailImage(thumbFromSourceFile(path));
      } else if (attachment.getUrl() != null) {
        //              没有本地图片，加载缩略图url
        String thumbUrl =
            ThumbHelper.makeImageThumbUrl(
                attachment.getUrl(), imageAttachment.getWidth(), imageAttachment.getHeight());
        loadThumbnailInternal(thumbUrl, getBounds(null));
      }
    } else if (getMsgInternal().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      if (attachment.getUrl() != null) {
        //视频消息拼接第一帧
        String videoUrl = attachment.getUrl();
        String thumbUrl = ThumbHelper.makeVideoThumbUrl(videoUrl);
        loadThumbnailImage(thumbUrl);
      } else {
        loadThumbnailImage(null);
      }
    }
  }

  private void loadThumbnailImage(String path) {
    int[] bounds = getBounds(path);
    loadThumbnailInternal(path, bounds);
  }

  private void loadThumbnailInternal(String path, int[] bounds) {
    int w = bounds[0];
    int h = bounds[1];
    int thumbMinEdge = getImageThumbMinEdge();
    if (w < thumbMinEdge) {
      w = thumbMinEdge;
      h = bounds[0] != 0 ? w * bounds[1] / bounds[0] : 0;
    }
    int thumbMaxEdge = getImageThumbMaxEdge();
    int thumbMaxHeight = (int) (0.45 * ScreenUtils.getDisplayHeight());
    if (w > thumbMaxEdge) {
      w = thumbMaxEdge;
      h = w * bounds[1] / bounds[0];
    }
    if (h > thumbMaxHeight) {
      h = thumbMaxHeight;
    }
    //避免0的case出现
    if (w == 0) {
      w = thumbMinEdge;
    }
    if (h == 0) {
      h = thumbMinEdge;
    }
    FrameLayout.LayoutParams thumbParams =
        (FrameLayout.LayoutParams) binding.getRoot().getLayoutParams();
    thumbParams.width = w;
    thumbParams.height = h;
    binding.getRoot().setLayoutParams(thumbParams);

    // change container's background, stroke the thumbnail
    float[] corners = getCorners();
    ShapeDrawable.Builder shapeBuilder =
        new ShapeDrawable.Builder()
            .setStroke(
                1, ContextCompat.getColor(getMessageContainer().getContext(), R.color.color_e2e5e8))
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
                      new GranularRoundedCornersWithCenterCrop(
                          corners[0], corners[1], corners[2], corners[3])))
          .override(w, h)
          .into(binding.thumbnail);
    }
  }

  private int getImageThumbMinEdge() {
    return (int) (0.25 * ScreenUtils.getDisplayWidth());
  }

  private int getImageThumbMaxEdge() {
    return (int) (0.48 * ScreenUtils.getDisplayWidth());
  }

  protected abstract String thumbFromSourceFile(String path);

  /** @return [width, height] */
  protected abstract int[] getBounds(String path);

  /** @return [leftTop, rightTop, leftBottom, rightBottom] */
  protected abstract float[] getCorners();
}
