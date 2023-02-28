// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import com.netease.yunxin.kit.qchatkit.ui.R;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class EmojiManager {

  private static final String EMOJI_DIR = "emoji/";

  // max cache size
  private static final int CACHE_MAX_SIZE = 1024;

  private static Pattern pattern;

  // default entries
  private static final List<Entry> defaultEntries = new ArrayList<>();
  // text to entry
  private static final Map<String, Entry> text2entry = new HashMap<>();
  // asset bitmap cache, key: asset path
  private static LruCache<String, Bitmap> drawableCache;

  private static WeakReference<Context> sContext;

  public static void init(Context context) {

    sContext = new WeakReference<>(context);

    load(context);

    pattern = makePattern();

    drawableCache =
        new LruCache<String, Bitmap>(CACHE_MAX_SIZE) {
          @Override
          protected void entryRemoved(
              boolean evicted, @NonNull String key, @NonNull Bitmap oldValue, Bitmap newValue) {
            if (oldValue != newValue) oldValue.recycle();
          }
        };
  }

  public static Context getContext() {
    return sContext.get();
  }

  private static class Entry {
    String text;
    String assetPath;

    Entry(String text, String assetPath) {
      this.text = text;
      this.assetPath = assetPath;
    }
  }

  public static int getDisplayCount() {
    return defaultEntries.size();
  }

  public static Drawable getDisplayDrawable(Context context, int index) {
    String text =
        (index >= 0 && index < defaultEntries.size() ? defaultEntries.get(index).text : null);
    return text == null ? null : getDrawable(context, text);
  }

  public static String getDisplayText(int index) {
    return index >= 0 && index < defaultEntries.size() ? defaultEntries.get(index).text : null;
  }

  public static Pattern getPattern() {
    return pattern;
  }

  public static Drawable getDrawable(Context context, String text) {
    Entry entry = text2entry.get(text);
    if (entry == null) {
      return null;
    }

    Bitmap cache = drawableCache.get(entry.assetPath);
    if (cache == null) {
      cache = loadAssetBitmap(context, entry.assetPath);
    }
    return new BitmapDrawable(context.getResources(), cache);
  }

  private static Pattern makePattern() {
    return Pattern.compile(patternOfDefault());
  }

  private static String patternOfDefault() {
    return "\\[[^\\[]{1,20}\\]";
  }

  private static Bitmap loadAssetBitmap(Context context, String assetPath) {
    InputStream is = null;
    try {
      Resources resources = context.getResources();
      Options options = new Options();
      options.inDensity = DisplayMetrics.DENSITY_HIGH;
      options.inScreenDensity = resources.getDisplayMetrics().densityDpi;
      options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
      is = context.getAssets().open(assetPath);
      Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(), options);
      if (bitmap != null) {
        drawableCache.put(assetPath, bitmap);
      }
      return bitmap;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  private static void load(Context context) {
    new EntryLoader().load(context);
  }

  private static class EntryLoader {
    private String catalog = "";

    void load(Context context) {
      try {
        XmlResourceParser xmlParser = context.getResources().getXml(R.xml.emoji);
        //
        try {
          int event = xmlParser.getEventType(); //先获取当前解析器光标在哪
          while (event != XmlPullParser.END_DOCUMENT) { //如果还没到文档的结束标志，那么就继续往下处理
            if (event == XmlPullParser.START_TAG) { //一般都是获取标签的属性值，所以在这里数据你需要的数据
              if (xmlParser.getName().equals("Catalog")) {
                catalog = xmlParser.getAttributeValue(0);
              } else if (xmlParser.getName().equals("Emoticon")) {
                String fileName = xmlParser.getAttributeValue(0);
                String tag = xmlParser.getAttributeValue(2);
                Entry entry = new Entry(tag, EMOJI_DIR + catalog + "/" + fileName);
                text2entry.put(entry.text, entry);
                if (catalog.equals("default")) {
                  defaultEntries.add(entry);
                }
              }
            }
            event = xmlParser.next(); //将当前解析器光标往下一步移
          }
        } catch (XmlPullParserException | IOException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
