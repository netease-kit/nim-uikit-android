package com.netease.nim.uikit;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.netease.nim.uikit.common.util.log.LogUtil;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片加载、缓存、管理组件
 */
public class ImageLoaderKit {

    private static final String TAG = ImageLoaderKit.class.getSimpleName();

    private static final int M = 1024 * 1024;

    private Context context;

    private static List<String> uriSchemes;

    public ImageLoaderKit(Context context) {
        this.context = context;
    }

    public void init(ImageLoaderConfiguration config) {
        try {
            ImageLoader.getInstance().init(config == null ? getDefaultConfig() : config);
        } catch (IOException e) {
            LogUtil.e(TAG, "init ImageLoaderKit error, e=" + e.getMessage().toString());
        }

        LogUtil.i(TAG, "init ImageLoaderKit completed");
    }

    public void clear() {
        ImageLoader.getInstance().clearMemoryCache();
    }

    private ImageLoaderConfiguration getDefaultConfig() throws IOException {
        int MAX_CACHE_MEMORY_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache/image/");

        LogUtil.i(TAG, "ImageLoader memory cache size = " + MAX_CACHE_MEMORY_SIZE / M + "M");
        LogUtil.i(TAG, "ImageLoader disk cache directory = " + cacheDir.getAbsolutePath());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(context)
                .threadPoolSize(3) //线程池内加载的数量
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(MAX_CACHE_MEMORY_SIZE))
                .discCache(new LruDiskCache(cacheDir, new Md5FileNameGenerator(), 0))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs()
                .build();

        return config;
    }

    public static Bitmap getBitmapFromCache(String uri, int width, int height) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }

        boolean cached = true;
        ImageDownloader.Scheme scheme = ImageDownloader.Scheme.ofUri(uri);
        if (scheme == ImageDownloader.Scheme.HTTP || scheme == ImageDownloader.Scheme.HTTPS || scheme ==
                ImageDownloader.Scheme.UNKNOWN) {
            // non local resource
            cached = MemoryCacheUtils.findCachedBitmapsForImageUri(uri, ImageLoader.getInstance()
                    .getMemoryCache()).size() > 0 || DiskCacheUtils.findInCache(uri, ImageLoader.getInstance()
                    .getDiskCache()) != null;
        }

        if (cached) {
            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(uri, new ImageSize(width, height));
            if (bitmap == null) {
                LogUtil.e(TAG, "load cached image failed, uri =" + uri);
            }
            return bitmap;
        }

        return null;
    }

    public static boolean isImageUriValid(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return false;
        }

        if (uriSchemes == null) {
            uriSchemes = new ArrayList<>();
            for (ImageDownloader.Scheme scheme : ImageDownloader.Scheme.values()) {
                uriSchemes.add(scheme.name().toLowerCase());
            }
        }

        for (String scheme : uriSchemes) {
            if (uri.toLowerCase().startsWith(scheme)) {
                return true;
            }
        }

        return false;
    }
}
