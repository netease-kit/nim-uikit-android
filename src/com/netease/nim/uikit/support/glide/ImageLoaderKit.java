package com.netease.nim.uikit.support.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import java.util.concurrent.TimeUnit;

/**
 * 图片缓存管理组件
 */
public class ImageLoaderKit {

    private static final String TAG = "ImageLoaderKit";

    private Context context;

    public ImageLoaderKit(Context context) {
        this.context = context;
    }

    /**
     * 清空图像缓存
     */

    public void clear() {
        NIMGlideModule.clearMemoryCache(context);
    }

    /**
     * 构建图像缓存
     */
    public void buildImageCache() {
        // 不必清除缓存，并且这个缓存清除比较耗时
        // clear();

        // build self avatar cache
        asyncLoadAvatarBitmapToCache(NimUIKit.getAccount());
    }

    /**
     * 获取通知栏提醒所需的头像位图，只存内存缓存/磁盘缓存中取，如果没有则返回空，自动发起异步加载
     * 注意：该方法在后台线程执行
     */
    public Bitmap getNotificationBitmapFromCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        final int imageSize = HeadImageView.DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE;

        Bitmap cachedBitmap = null;
        try {
            cachedBitmap = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(new RequestOptions()
                            .centerCrop()
                            .override(imageSize, imageSize))
                    .submit()
                    .get(200, TimeUnit.MILLISECONDS)// 最大等待200ms
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cachedBitmap;
    }

    /**
     * 异步加载头像位图到Glide缓存中
     */
    private void asyncLoadAvatarBitmapToCache(final String account) {
        NimSingleThreadExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
                if (userInfo != null) {
                    loadAvatarBitmapToCache(userInfo.getAvatar());
                }
            }
        });
    }

    private void loadAvatarBitmapToCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        final int imageSize = HeadImageView.DEFAULT_AVATAR_THUMB_SIZE;
        Glide.with(context)
                .load(url)
                .submit(imageSize, imageSize);
    }
}
