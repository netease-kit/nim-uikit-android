package com.netease.nim.uikit.common.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * 位图缓存
 * <p/>
 * Created by huangjun on 2015/7/13.
 */
public class BitmapCache {
    private final String TAG = "BitmapCache";

    public static BitmapCache getInstance() {
        return InstanceHolder.instance;
    }

    private Cache cache;

    private BitmapCache() {
        int MAX_CACHE_MEMORY_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024);
        cache = new Cache(MAX_CACHE_MEMORY_SIZE / 8);
    }

    private class Cache extends LruCache<String, Bitmap> {
        public Cache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    }

    public void init() {
        Log.i(TAG, "Bitmap cache init...");
    }

    public void putBitmap(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) {
            return;
        }

        if (getBitmap(key) != null) {
            return;
        }

        cache.put(key, bitmap);
        Log.i(TAG, "PUT BITMAP TO CACHE, KEY =" + key + "," + cache.size() + "/" + cache.maxSize());
    }

    public Bitmap getBitmap(String key) {
        Bitmap bm = cache.get(key);
        if (bm != null) {
            Log.i(TAG, "GET CACHED BITMAP, KEY =" + key);
        }
        return bm;
    }

    public void removeBitmapCache(String key) {
        if (key != null) {
            if (cache != null) {
                Bitmap bm = cache.remove(key);
                if (bm != null) {
                    bm.recycle();
                }
            }
        }
    }

    public void clearCache() {
        if (cache.size() > 0) {
            cache.evictAll();
        }
    }

    private static class InstanceHolder {
        static final BitmapCache instance = new BitmapCache();
    }
}
