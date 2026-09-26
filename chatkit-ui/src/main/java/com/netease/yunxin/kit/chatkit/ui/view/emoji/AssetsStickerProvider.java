package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/** Reads the host application's fixed assets/sticker catalog. */
final class AssetsStickerProvider implements StickerProvider {
  private static final String ROOT = "sticker";
  private final AssetManager assets;
  private final List<StickerPack> packs;
  private String version = "1";

  AssetsStickerProvider(@NonNull Context context) {
    assets = context.getAssets();
    packs = loadPacks();
  }

  @NonNull
  @Override
  public List<StickerPack> getPacks() {
    return packs;
  }

  @NonNull
  @Override
  public String getStickerAssetPath(@NonNull StickerItem item) {
    return ROOT + "/" + item.resourceKey;
  }

  @NonNull
  @Override
  public String getIconAssetPath(@NonNull String iconKey) {
    return ROOT + "/" + iconKey;
  }

  @NonNull
  @Override
  public String getVersion() {
    return version;
  }

  private List<StickerPack> loadPacks() {
    List<StickerPack> result = new ArrayList<>();
    Set<String> packIds = new HashSet<>();
    Set<String> stickerIds = new HashSet<>();
    try {
      JSONObject root = new JSONObject(readText(ROOT + "/sticker.json"));
      version = root.optString("version", "1");
      if (version.length() == 0) version = "1";
      JSONArray packArray = root.optJSONArray("packs");
      if (packArray == null) return result;
      for (int i = 0; i < packArray.length(); i++) {
        try {
          JSONObject packObject = packArray.optJSONObject(i);
          StickerPack pack = parsePack(packObject, packIds, stickerIds);
          if (pack != null && !pack.items.isEmpty()) result.add(pack);
        } catch (Exception ignored) {
          // Ignore one malformed pack and keep the other valid packs available.
        }
      }
      Collections.sort(
          result,
          new Comparator<StickerPack>() {
            @Override
            public int compare(StickerPack left, StickerPack right) {
              return left.order - right.order;
            }
          });
    } catch (Exception ignored) {
      return new ArrayList<>();
    }
    return result;
  }

  private StickerPack parsePack(JSONObject object, Set<String> packIds, Set<String> stickerIds) {
    if (object == null) return null;
    String packId = object.optString("id", "");
    if (packId.length() == 0 || packIds.contains(packId)) return null;
    JSONArray stickerArray = object.optJSONArray("stickers");
    if (stickerArray == null) return null;
    List<StickerItem> items = new ArrayList<>();
    Set<String> packStickerIds = new HashSet<>();
    for (int i = 0; i < stickerArray.length(); i++) {
      JSONObject itemObject = stickerArray.optJSONObject(i);
      if (itemObject == null) continue;
      String stickerId = itemObject.optString("id", "");
      String file = itemObject.optString("file", "");
      if (stickerId.length() == 0
          || !isPackAsset(packId, file)
          || stickerIds.contains(stickerId)
          || !packStickerIds.add(stickerId)) {
        continue;
      }
      items.add(
          new StickerItem(
              packId,
              stickerId,
              itemObject.optString("name", stickerId),
              file,
              itemObject.optInt("order", i)));
    }
    Collections.sort(
        items,
        new Comparator<StickerItem>() {
          @Override
          public int compare(StickerItem left, StickerItem right) {
            return left.order - right.order;
          }
        });
    if (items.isEmpty()) return null;
    packIds.add(packId);
    stickerIds.addAll(packStickerIds);
    String cover = object.optString("cover", "");
    if (!isPackAsset(packId, cover)) cover = items.get(0).resourceKey;
    String iconNormal = object.optString("iconNormal", "");
    String iconSelected = object.optString("iconSelected", "");
    if (!isRootAsset(iconNormal)) iconNormal = "";
    if (!isRootAsset(iconSelected)) iconSelected = "";
    return new StickerPack(
        packId,
        object.optString("title", packId),
        object.optInt("order", 0),
        cover,
        iconNormal,
        iconSelected,
        items);
  }

  private boolean isRootAsset(String path) {
    if (path == null || path.length() == 0 || path.startsWith("/") || path.contains("..")) {
      return false;
    }
    InputStream input = null;
    try {
      input = assets.open(ROOT + "/" + path);
      return true;
    } catch (IOException ignored) {
      return false;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException ignored) {
          // Ignore close failures after validating the asset reference.
        }
      }
    }
  }

  private boolean isPackAsset(String packId, String path) {
    if (path == null || path.length() == 0 || path.startsWith("/") || path.contains("..")) {
      return false;
    }
    String packPrefix = packId + "/";
    if (!path.startsWith(packPrefix)) return false;
    InputStream input = null;
    try {
      input = assets.open(ROOT + "/" + path);
      return true;
    } catch (IOException ignored) {
      return false;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException ignored) {
          // Ignore close failures after validating the asset reference.
        }
      }
    }
  }

  private String readText(String path) throws IOException {
    StringBuilder result = new StringBuilder();
    InputStream input = assets.open(path);
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null) result.append(line);
      reader.close();
    } finally {
      input.close();
    }
    return result.toString();
  }
}
