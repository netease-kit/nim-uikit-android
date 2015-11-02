package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.netease.nim.uikit.ImageLoaderKit;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nimlib.sdk.nos.model.NosThumbParam;
import com.netease.nimlib.sdk.nos.util.NosThumbImageUtil;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 圆形头像控件<br>
 * Created by huangjun on 2015/3/10.
 * <p/>
 * 控件绘制由3步组成: <br>
 * 1. 绘制一个遮罩图(mask)，该遮罩图决定头像被剪切的形状，比如圆形，放心，甚至五角星形等。<br>
 * 2. 绘制头像本身，根据遮罩图做裁剪(SRC_IN)。<br>
 * 3. 在已绘制的位图上加上封面(cover),比如边框，徽标等。 <br>
 */
public class HeadImageView extends ImageView {

    public static final int DEFAULT_THUMB_SIZE = 100;

    private Drawable mask;

    private Drawable cover;

    private DisplayImageOptions options = createImageOptions();

    private static final Paint paintMaskSrcIn = createMaskPaint();

    private static final Paint paintCoverSrcOver = createCoverPaint();

    public HeadImageView(Context context) {
        super(context);
    }

    public HeadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static final Paint createMaskPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        return paint;
    }

    private static final Paint createCoverPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        return paint;
    }

    private static final DisplayImageOptions createImageOptions() {
        int defaultIcon = NimUIKit.getUserInfoProvider().getDefaultIconResId();
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultIcon)
                .showImageOnFail(defaultIcon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public void setMask(int maskResId) {
        mask = getResources().getDrawable(maskResId);
    }

    /**
     * 加载用户头像（默认大小的缩略图）
     *
     * @param account
     */
    public void loadBuddyAvatar(String account) {
        loadBuddyAvatar(account, DEFAULT_THUMB_SIZE);
    }

    /**
     * 加载用户头像（原图）
     *
     * @param account
     */
    public void loadBuddyOriginalAvatar(String account) {
        loadBuddyAvatar(account, 0);
    }

    /**
     * 加载用户头像（指定缩略大小）
     *
     * @param account
     * @param thumbSize 缩略图的宽、高
     */
    public void loadBuddyAvatar(final String account, final int thumbSize) {
        // 先显示默认头像
        setMask(R.drawable.nim_portrait_mask_round);
        setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());

        // 判断是否需要ImageLoader加载
        final UserInfoProvider.UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        boolean needLoad = userInfo != null && ImageLoaderKit.isImageUriValid(userInfo.getAvatar());

        // ImageLoader异步加载
        if (needLoad) {
            setTag(account); // 解决ViewHolder复用问题
            /**
             * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
             * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
             */
            final String thumbUrl = thumbSize > 0 ? NosThumbImageUtil.makeImageThumbUrl(userInfo.getAvatar(),
                    NosThumbParam.ThumbType.Crop, thumbSize, thumbSize) : userInfo.getAvatar();

            // 异步从cache or NOS加载图片
            ImageLoader.getInstance().displayImage(thumbUrl, new NonViewAware(new ImageSize(thumbSize, thumbSize),
                    ViewScaleType.CROP), options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (getTag() != null && getTag().equals(account)) {
                        setImageBitmap(loadedImage);
                    }
                }
            });
        } else {
            setTag(null);
        }
    }

    public void loadTeamIcon(String tid) {
        Bitmap bitmap = NimUIKit.getUserInfoProvider().getTeamIcon(tid);
        setImageBitmap(bitmap);
    }

    public void setBackgroundResource(int resId) {
        mask = null;
        super.setBackgroundResource(resId);
    }

    /**
     * 解决ViewHolder复用问题
     */
    public void resetImageView() {
        cover = null;
        setImageBitmap(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mask != null || cover != null) {
            // bounds
            int width = getWidth();
            int height = getHeight();

            // MASK层
            if (mask != null) {
                canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
                mask.setBounds(0, 0, width, height);
                mask.draw(canvas);
            }

            // 头像层
            canvas.saveLayer(0, 0, width, height, paintMaskSrcIn, Canvas.ALL_SAVE_FLAG);
            try {
                super.onDraw(canvas);
            } catch (Throwable th) {
                // bitmap由外面管理，这里有可能会已经被回收了
            }

            // 头像层与MASK层合并：mask ∩ source => MERGE层
            canvas.restore();

            // COVER层
            if (cover != null) {
                canvas.saveLayer(0, 0, width, height, paintCoverSrcOver, Canvas.ALL_SAVE_FLAG);
                cover.setBounds(0, 0, width, height);
                cover.draw(canvas);

                // COVER层与MERGE合并，cover ∪ merge => RESULT
                canvas.restore();
            }

            // RESULT层与DEFAULT层合并
            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
    }
}
