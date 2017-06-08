package com.netease.nim.uikit.common.media.picker.loader;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.netease.nim.uikit.common.media.picker.util.BitmapUtil;

/**
 * Created by huangjun on 2017/4/11.
 */

class RotateTransformation extends BitmapTransformation {
    private String path;

    RotateTransformation(Context context, String path) {
        super(context);
        this.path = path;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return BitmapUtil.reviewPicRotate(toTransform, path);
    }

    @Override
    public String getId() {
        return "rotate_" + path.hashCode();
    }
}
