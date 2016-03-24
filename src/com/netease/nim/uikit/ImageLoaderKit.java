package com.netease.nim.uikit;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.netease.nim.uikit.common.media.picker.util.BitmapUtil;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
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

    public ImageLoaderKit(Context context, ImageLoaderConfiguration config) {
        this.context = context;
        init(config);
    }

    private void init(ImageLoaderConfiguration config) {
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
                .threadPoolSize(3) // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2) // 降低线程的优先级，减小对UI主线程的影响
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(MAX_CACHE_MEMORY_SIZE))
                .discCache(new LruDiskCache(cacheDir, new Md5FileNameGenerator(), 0))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs()
                .build();

        return config;
    }


    /**
     * 判断图片地址是否合法，合法地址如下：
     * String uri = "http://site.com/image.png"; // from Web
     * String uri = "file:///mnt/sdcard/image.png"; // from SD card
     * String uri = "content://media/external/audio/albumart/13"; // from content provider
     * String uri = "assets://image.png"; // from assets
     * String uri = "drawable://" + R.drawable.image; // from drawables (only images, non-9patch)
     */
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

    /**
     * 从ImageLoader内存缓存中取出头像位图
     */
    private static Bitmap getMemoryCachedAvatarBitmap(UserInfoProvider.UserInfo userInfo) {
        if(userInfo == null || !isImageUriValid(userInfo.getAvatar())) {
            return null;
        }

        String key = HeadImageView.getAvatarCacheKey(userInfo.getAvatar());

        // DiskCacheUtils.findInCache(uri, ImageLoader.getInstance().getDiskCache() 查询磁盘缓存示例
        List<Bitmap> bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(key, ImageLoader.getInstance().getMemoryCache());
        if(bitmaps.size() > 0) {
            return bitmaps.get(0);
        }

        return null;
    }

    /**
     * 异步加载头像位图到ImageLoader内存缓存
     */
    private static void asyncLoadAvatarBitmapToCache(UserInfoProvider.UserInfo userInfo) {
        if(userInfo == null || !isImageUriValid(userInfo.getAvatar())) {
            return;
        }

        String url = HeadImageView.getAvatarCacheKey(userInfo.getAvatar());
        ImageLoader.getInstance().loadImage(url,
                new ImageSize(HeadImageView.DEFAULT_AVATAR_THUMB_SIZE, HeadImageView.DEFAULT_AVATAR_THUMB_SIZE),
                avatarLoadOption, null);

    }

    /**
     * 获取通知栏提醒所需的头像位图，只存内存缓存中取，如果没有则返回空，自动发起异步加载
     */
    public static Bitmap getNotificationBitmapFromCache(UserInfoProvider.UserInfo userInfo) {
        Bitmap cachedBitmap = getMemoryCachedAvatarBitmap(userInfo);
        if(cachedBitmap == null) {
            asyncLoadAvatarBitmapToCache(userInfo);
        } else {
            return BitmapUtil.resizeBitmap(cachedBitmap,
                    HeadImageView.DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE,
                    HeadImageView.DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE);
        }

        return null;
    }

    /**
     * 构建头像缓存
     */
    public static void buildAvatarCache(List<String> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        UserInfoProvider.UserInfo userInfo;
        for (String account : accounts) {
            userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
            asyncLoadAvatarBitmapToCache(userInfo);
        }

        LogUtil.i(TAG, "build avatar cache completed, avatar count =" + accounts.size());
    }

    /**
     * 头像ImageLoader加载配置
     */
    private static DisplayImageOptions avatarLoadOption = createImageOptions();

    private static final DisplayImageOptions createImageOptions() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
}
