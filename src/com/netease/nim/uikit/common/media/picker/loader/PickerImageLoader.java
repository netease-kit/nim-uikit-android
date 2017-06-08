package com.netease.nim.uikit.common.media.picker.loader;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.netease.nim.uikit.NimUIKit;

public class PickerImageLoader {
    public static void initCache() {
        // TODO:HUANGJUN
    }

    public static void clearCache() {
        // TODO:HUANGJUN
    }

    public static void display(final String thumbPath, final String originalPath, final ImageView imageView, final int defResource) {
        Glide.with(NimUIKit.getContext())
                .load(thumbPath).asBitmap().centerCrop()
                .placeholder(defResource)
                .error(defResource)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new RotateTransformation(NimUIKit.getContext(), originalPath))
                .into(imageView);
    }
}
