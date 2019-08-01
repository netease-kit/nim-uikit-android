package com.netease.nim.uikit.common.media.imagepicker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 */

public class GlideCropImageView extends CropImageView {
    public GlideCropImageView(Context context) {
        super(context);
    }

    public GlideCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlideCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Bitmap getBitmap() {
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            return fromDrawable(drawable);
        }
    }

    private static Bitmap fromDrawable(Drawable drawable) {
        try {
            if (drawable.getIntrinsicWidth() * drawable.getIntrinsicHeight() == 0) {
                return null;
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
