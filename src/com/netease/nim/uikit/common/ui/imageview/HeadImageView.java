package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

/**
 * 圆形头像控件<br>
 * Created by huangjun on 2015/3/10.
 * <p/>
 * 控件绘制由3步组成: <br>
 *     1. 绘制一个遮罩图(mask)，该遮罩图决定头像被剪切的形状，比如圆形，放心，甚至五角星形等。<br>
 *     2. 绘制头像本身，根据遮罩图做裁剪(SRC_IN)。<br>
 *     3. 在已绘制的位图上加上封面(cover),比如边框，徽标等。 <br>
 */
public class HeadImageView extends ImageView {

    public HeadImageView(Context context) {
        super(context);
    }

    public HeadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 解决ViewHolder复用问题
     */
    public void resetImageView() {
        cover = null;
        setImageBitmap(null);
    }

    public void setMask(int maskResId) {
        mask = getResources().getDrawable(maskResId);
    }

    public void loadBuddyAvatar(String account) {
        setMask(R.drawable.nim_portrait_mask_round);
        UserInfoProvider.UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo != null) {

            if (userInfo.getAvatar() != null) {
                setImageBitmap(userInfo.getAvatar());
            } else {
                int width = getWidth();
                int height = getHeight();
                Bitmap bmp = BitmapDecoder.decodeSampled(getResources(), userInfo.getAvatarResId(), width, height);
                setImageBitmap(bmp);
            }
        } else {
            setImageResource(NimUIKit.getUserInfoProvider().getDefaultIconResId());
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

    private Drawable mask;

    private Drawable cover;

    private static final Paint paintMaskSrcIn = createMaskPaint();

    private static final Paint createMaskPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        return paint;
    }

    private static final Paint paintCoverSrcOver = createCoverPaint();

    private static final Paint createCoverPaint() {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        return paint;
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
