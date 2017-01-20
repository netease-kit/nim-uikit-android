package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.IOException;

public class MsgThumbImageView extends ImageView {

    private Drawable mask; // blend mask drawable

    public MsgThumbImageView(Context context) {
        super(context);
    }

    public MsgThumbImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MsgThumbImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private DisplayImageOptions options = createImageOptions();

    private boolean hasLoaded = false;

    private static final DisplayImageOptions createImageOptions() {
        int defaultIcon = R.drawable.nim_image_default;
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultIcon)
                .showImageOnFail(defaultIcon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private static final Paint paintMask = createMaskPaint();

    private static final Paint createMaskPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mask != null) {
            // bounds
            int width = getWidth();
            int height = getHeight();

            // create blend layer
            canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);

            //
            // mask
            //
            if (mask != null) {
                mask.setBounds(0, 0, width, height);
                mask.draw(canvas);
            }

            //
            // source
            //
            {
                canvas.saveLayer(0, 0, width, height, paintMask, Canvas.ALL_SAVE_FLAG);
                super.onDraw(canvas);
                canvas.restore();
            }

            // apply blend layer
            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    public void loadAsPath(String pathName, int width, int height, int maskId) {
        setBlendDrawable(maskId);
        setImageBitmap(BitmapDecoder.decodeSampled(pathName, width, height));
    }

    public void loadAsPath(boolean isOriginal, final String path, final String tag, final int width, final int height, final int maskId) {
        if (TextUtils.isEmpty(path)) {
            setTag(null);
            loadAsResource(R.drawable.nim_image_default, maskId);
            return;
        }

        if (!isOriginal || getTag() == null || !getTag().equals(tag)) {
            hasLoaded = false; // 由于ViewHolder复用，使得tag发生变化，必须重新加载
        }
        setTag(tag); // 解决ViewHolder复用问题

        // async load
        if (!hasLoaded) {
            // load default image first
            loadAsResource(R.drawable.nim_image_default, maskId);

            final String uri = ImageDownloader.Scheme.FILE.wrap(path);
            final ImageSize imageSize = new ImageSize(width, height);
            ImageLoader.getInstance().displayImage(uri, new NonViewAware(imageSize,
                    ViewScaleType.CROP), options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (getTag() != null && getTag().equals(tag) && !hasLoaded) {
                        setImageBitmap(loadedImage);
                        hasLoaded = true; // 没有复用情况下，已经加载过了，不要重复加载，会闪烁
                    }
                }

                @Override
                public void onLoadingFailed(final String imageUri, View view, FailReason failReason) {
                    // 视频缩略图后缀.mp4等导致ImageLoader解码失败
                    if (failReason.getType() == FailReason.FailType.DECODING_ERROR) {
                        loadBmpAsync(imageUri, path, imageSize, tag);
                    }
                }
            });
        }
    }

    private void loadBmpAsync(final String imageUri, final String path, final ImageSize imageSize, final String tag) {
        NimSingleThreadExecutor.getInstance().execute(new NimSingleThreadExecutor.NimTask<Bitmap>() {
            @Override
            public Bitmap runInBackground() {
                return decodeBmpAndSave(path, imageSize, imageUri);
            }

            @Override
            public void onCompleted(Bitmap result) {
                if (result != null && (getTag() != null && getTag().equals(tag) && !hasLoaded)) {
                    setImageBitmap(result);
                    hasLoaded = true;
                }
            }
        });
    }

    private Bitmap decodeBmpAndSave(String path, ImageSize imageSize, String imageUri) {
        Bitmap bitmap = BitmapDecoder.decodeSampled(path, imageSize.getWidth(), imageSize.getHeight());
        if (bitmap != null) {
            String memoryCacheKey = MemoryCacheUtils.generateKey(imageUri, imageSize);
            ImageLoader.getInstance().getMemoryCache().put(memoryCacheKey, bitmap);
            try {
                ImageLoader.getInstance().getDiskCache().save(imageUri, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void loadAsResource(int resId, int maskId) {
        setBlendDrawable(maskId);
        setImageResource(resId);
    }

    private void setBlendDrawable(int maskId) {
        mask = maskId != 0 ? getResources().getDrawable(maskId) : null;
    }
}
