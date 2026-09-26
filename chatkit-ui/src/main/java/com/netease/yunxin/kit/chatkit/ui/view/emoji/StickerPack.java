package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

/** A sticker pack, which is also one selectable sticker category in the picker. */
public final class StickerPack {
  public final String packId;
  public final String title;
  public final int order;
  public final String coverKey;
  public final String iconNormalKey;
  public final String iconSelectedKey;
  public final List<StickerItem> items;

  public StickerPack(
      @NonNull String packId,
      @NonNull String title,
      int order,
      String coverKey,
      String iconNormalKey,
      String iconSelectedKey,
      @NonNull List<StickerItem> items) {
    this.packId = packId;
    this.title = title;
    this.order = order;
    this.coverKey = coverKey;
    this.iconNormalKey = iconNormalKey;
    this.iconSelectedKey = iconSelectedKey;
    this.items = Collections.unmodifiableList(items);
  }
}
