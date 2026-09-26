package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import androidx.annotation.NonNull;

/** A sticker item declared by sticker.json. */
public final class StickerItem {
  public final String packId;
  public final String stickerId;
  public final String displayName;
  public final String resourceKey;
  public final int order;

  public StickerItem(
      @NonNull String packId,
      @NonNull String stickerId,
      @NonNull String displayName,
      @NonNull String resourceKey,
      int order) {
    this.packId = packId;
    this.stickerId = stickerId;
    this.displayName = displayName;
    this.resourceKey = resourceKey;
    this.order = order;
  }
}
