package com.netease.nim.uikit.common.media.imagepicker.loader;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;


public interface ImageLoader extends Serializable {

    void displayImage(Context context, String path, ImageView imageView, int width, int height);

    void displayImage(Context context, String path, ImageView imageView, int width, int height,
                      GlideImageLoader.LoadListener listener);

    void downloadImage(Context context, String path, GlideImageLoader.LoadListener listener);

    void clearRequest(View view);

    void clearMemoryCache();
}
