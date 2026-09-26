package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import java.util.List;

/** Supplies sticker metadata and paths relative to the host application's assets directory. */
public interface StickerProvider {
  /**
   * Returns sticker packs in display order. Each {@code packId} must be globally unique across the
   * default assets catalog and this extension provider.
   */
  @NonNull
  List<StickerPack> getPacks();

  /** Returns an {@link AssetManager}-readable relative path for the sticker image. */
  @NonNull
  String getStickerAssetPath(@NonNull StickerItem item);

  /**
   * Converts an icon key into an {@link AssetManager}-readable asset path.
   *
   * <p>The returned value must be a non-empty relative path. URI schemes, absolute paths and paths
   * containing {@code ..} are not supported.
   */
  @NonNull
  default String getIconAssetPath(@NonNull String iconKey) {
    return iconKey;
  }

  /** Returns the provider resource version used to isolate send-file caches. */
  @NonNull
  default String getVersion() {
    return "1";
  }
}
