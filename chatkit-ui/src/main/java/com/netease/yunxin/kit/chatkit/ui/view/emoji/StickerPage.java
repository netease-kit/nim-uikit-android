package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.List;

/** A global sticker page used to make adjacent packs swipe continuously. */
public final class StickerPage {
  public final int packIndex;
  public final int pageIndexInPack;
  public final int packPageCount;
  public final List<StickerItem> items;

  public StickerPage(
      int packIndex, int pageIndexInPack, int packPageCount, @NonNull List<StickerItem> items) {
    this.packIndex = packIndex;
    this.pageIndexInPack = pageIndexInPack;
    this.packPageCount = packPageCount;
    this.items = Collections.unmodifiableList(items);
  }
}
