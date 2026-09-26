package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Internal bridge between sticker views, providers and the image message sender. */
public final class StickerAssetManager {
  private final Context appContext;
  private final List<StickerPack> packs;
  private final Map<String, StickerProvider> packProviders;
  private final Map<String, StickerProvider> iconProviders;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final File cacheDir;
  private final String resourceVersion;

  public StickerAssetManager(@NonNull Context context, @Nullable StickerProvider provider) {
    appContext = context.getApplicationContext();
    StickerProvider defaultProvider = new AssetsStickerProvider(appContext);
    packProviders = new HashMap<>();
    iconProviders = new HashMap<>();
    List<StickerPack> mergedPacks = new ArrayList<>();
    appendProviderPacks(defaultProvider, mergedPacks);
    if (provider != null) {
      appendProviderPacks(provider, mergedPacks);
    }
    packs = Collections.unmodifiableList(mergedPacks);
    cacheDir = new File(appContext.getCacheDir(), "sticker");
    resourceVersion = buildResourceVersion(defaultProvider, provider);
  }

  @NonNull
  public List<StickerPack> getPacks() {
    return packs;
  }

  @NonNull
  public String getStickerAssetPath(@NonNull StickerItem item) {
    StickerProvider provider = packProviders.get(item.packId);
    if (provider == null) {
      throw new IllegalArgumentException("Sticker pack is not registered: " + item.packId);
    }
    return provider.getStickerAssetPath(item);
  }

  @Nullable
  public String getStickerIconPreviewPath(@Nullable String iconKey) {
    if (iconKey == null || iconKey.length() == 0) return null;
    StickerProvider provider = iconProviders.get(iconKey);
    if (provider == null) return null;
    return getPreviewPath(provider.getIconAssetPath(iconKey));
  }

  @Nullable
  public String getStickerIconPreviewPath(@Nullable String packId, @Nullable String iconKey) {
    if (packId == null || packId.length() == 0 || iconKey == null || iconKey.length() == 0) {
      return null;
    }
    StickerProvider provider = packProviders.get(packId);
    if (provider == null) return null;
    return getPreviewPath(provider.getIconAssetPath(iconKey));
  }

  @Nullable
  public String getStickerPreviewPath(@NonNull StickerItem item) {
    return getPreviewPath(getStickerAssetPath(item));
  }

  @Nullable
  public String getPreviewPath(@Nullable String path) {
    if (!isValidAssetPath(path)) return null;
    return "file:///android_asset/" + path;
  }

  public void loadStickerFile(@NonNull StickerItem item, @NonNull StickerFileCallback callback) {
    executor.execute(
        () -> {
          try {
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
              throw new IllegalStateException("Cannot create sticker cache directory");
            }
            String assetPath = getStickerAssetPath(item);
            if (!isValidAssetPath(assetPath)) {
              throw new IllegalArgumentException("Sticker path must be a relative assets path");
            }
            String safeName = resourceVersion + "_" + item.packId + "_" + item.stickerId + ".png";
            File file = new File(cacheDir, safeName.replaceAll("[^a-zA-Z0-9_.-]", "_"));
            if (!file.exists() || file.length() == 0) {
              InputStream input = appContext.getAssets().open(assetPath);
              try {
                FileOutputStream output = new FileOutputStream(file);
                try {
                  byte[] buffer = new byte[8192];
                  int count;
                  while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                } finally {
                  output.close();
                }
              } finally {
                input.close();
              }
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
              throw new IllegalArgumentException("Invalid sticker image");
            }
            mainHandler.post(() -> callback.onSuccess(file, options.outWidth, options.outHeight));
          } catch (Throwable error) {
            mainHandler.post(() -> callback.onError(error));
          }
        });
  }

  public void clear() {
    executor.shutdownNow();
    mainHandler.removeCallbacksAndMessages(null);
  }

  private String sanitizeFileName(String value) {
    if (value == null || value.length() == 0) return "1";
    String result = value.replaceAll("[^a-zA-Z0-9_.-]", "_");
    return result.length() == 0 ? "1" : result;
  }

  private void appendProviderPacks(
      @NonNull StickerProvider provider, @NonNull List<StickerPack> mergedPacks) {
    List<StickerPack> providerPacks = provider.getPacks();
    if (providerPacks == null) return;
    for (StickerPack pack : providerPacks) {
      if (pack == null
          || pack.packId == null
          || pack.packId.length() == 0
          || pack.items == null
          || pack.items.isEmpty()) {
        continue;
      }
      if (packProviders.containsKey(pack.packId)) {
        ALog.w(
            LIB_TAG,
            "StickerAssetManager",
            "Duplicate sticker packId ignored, packId: "
                + pack.packId
                + ", provider: "
                + provider.getClass().getName());
        continue;
      }
      packProviders.put(pack.packId, provider);
      mergedPacks.add(pack);
      registerIconProvider(provider, pack.iconNormalKey);
      registerIconProvider(provider, pack.iconSelectedKey);
    }
  }

  private void registerIconProvider(@NonNull StickerProvider provider, @Nullable String iconKey) {
    if (iconKey != null && iconKey.length() > 0 && !iconProviders.containsKey(iconKey)) {
      iconProviders.put(iconKey, provider);
    }
  }

  private String buildResourceVersion(
      @NonNull StickerProvider defaultProvider, @Nullable StickerProvider extensionProvider) {
    String defaultVersion = sanitizeFileName(defaultProvider.getVersion());
    if (extensionProvider == null) return defaultVersion;
    return sanitizeFileName(defaultVersion + "_" + extensionProvider.getVersion());
  }

  private boolean isValidAssetPath(@Nullable String path) {
    return path != null
        && path.length() > 0
        && !path.startsWith("/")
        && !path.contains("..")
        && !path.contains("://");
  }
}
