package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.collection.LruCache;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageQuickComment;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** chatkit-ui 自己维护的 QuickComment 索引映射，不依赖 qchatkit-ui。 */
public final class ReactionEmojiManager {
  private static final String ASSET_DIR = "reaction_emoji/";
  private static final int ASSET_COUNT = 112;
  private static final int ASSET_DENSITY = 320;
  private static final int MAX_QUICK_INDEX_COUNT = 7;
  // Shortcut values are persisted and consumed as 1-based reaction indexes.
  private static final List<Long> DEFAULT_QUICK_INDEXES =
      Collections.unmodifiableList(Arrays.asList(3L, 5L, 1L, 21L, 65L, 19L, 20L));
  private static volatile List<Long> quickIndexes = DEFAULT_QUICK_INDEXES;
  private static final List<Long> ALL_INDEXES = createAllIndexes();
  private static final LruCache<String, Bitmap> BITMAP_CACHE = new LruCache<>(24);

  private ReactionEmojiManager() {}

  public static List<Long> getQuickIndexes() {
    return quickIndexes;
  }

  /** Configures the shared shortcut indexes; only the first seven values are used. */
  public static void setQuickIndexes(long[] indexes) {
    if (indexes == null || indexes.length == 0) {
      quickIndexes = DEFAULT_QUICK_INDEXES;
      return;
    }
    List<Long> configuredIndexes = new ArrayList<>(MAX_QUICK_INDEX_COUNT);
    for (long index : indexes) {
      if (index > 0 && index <= ASSET_COUNT) {
        configuredIndexes.add(index);
      }
      if (configuredIndexes.size() == MAX_QUICK_INDEX_COUNT) {
        break;
      }
    }
    if (configuredIndexes.isEmpty()) {
      quickIndexes = DEFAULT_QUICK_INDEXES;
      return;
    }
    quickIndexes = Collections.unmodifiableList(configuredIndexes);
  }

  /** Returns the 1-based reaction indexes used by the SDK and message state. */
  public static List<Long> getAllIndexes() {
    return ALL_INDEXES;
  }

  public static int getCount() {
    return ASSET_COUNT;
  }

  public static boolean isValidIndex(long reactionIndex) {
    return reactionIndex > 0 && reactionIndex <= ASSET_COUNT;
  }

  public static Drawable getDrawable(long reactionIndex) {
    if (!isValidIndex(reactionIndex)) {
      return null;
    }
    Context context = IMKitClient.getApplicationContext();
    if (context == null) {
      return null;
    }
    String assetPath = ASSET_DIR + String.format(Locale.US, "%03d.png", reactionIndex);
    Bitmap bitmap = BITMAP_CACHE.get(assetPath);
    if (bitmap == null) {
      bitmap = loadBitmap(context, assetPath);
      if (bitmap != null) {
        BITMAP_CACHE.put(assetPath, bitmap);
      }
    }
    return bitmap == null ? null : new BitmapDrawable(context.getResources(), bitmap);
  }

  private static Bitmap loadBitmap(Context context, String assetPath) {
    InputStream input = null;
    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inDensity = ASSET_DENSITY;
      options.inScreenDensity = context.getResources().getDisplayMetrics().densityDpi;
      options.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
      input = context.getAssets().open(assetPath);
      return BitmapFactory.decodeStream(input, null, options);
    } catch (Exception ignored) {
      return null;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException ignored) {
          // Ignore close failures after the asset has been read.
        }
      }
    }
  }

  /** 回复资源已经统一画布规格，不再对单个表情做视觉缩放。 */
  public static float getVisualScale(long index) {
    return 1.0f;
  }

  public static boolean isKnown(V2NIMMessageQuickComment comment) {
    return comment != null && getDrawable(comment.getIndex()) != null;
  }

  private static List<Long> createAllIndexes() {
    Long[] indexes = new Long[ASSET_COUNT];
    for (int position = 0; position < ASSET_COUNT; position++) {
      indexes[position] = (long) position + 1;
    }
    return Collections.unmodifiableList(Arrays.asList(indexes));
  }
}
