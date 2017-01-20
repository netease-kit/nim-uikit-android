package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.netease.nim.uikit.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by huangjun on 2015/12/13.
 */
public class ImageViewEx extends ImageView {

    private int defaultImageResId;

    private DisplayImageOptions options;

    private final DisplayImageOptions createImageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImageResId)
                .showImageOnFail(defaultImageResId)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public ImageViewEx(Context context) {
        super(context);
    }

    public ImageViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewEx, defStyle, 0);
        defaultImageResId = a.getResourceId(R.styleable.ImageViewEx_exiv_default_image_res, 0);
        a.recycle();

        this.options = createImageOptions();
    }

    /**
     * 加载图片
     */
    public void load(final String url) {
        ImageLoader.getInstance().displayImage(url, this, options);
    }
}
