package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.netease.nim.uikit.common.util.file.AttachmentStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CropImageView extends MultiTouchZoomableImageView {

    private static final int MARGIN = 50;

    private int outputX;
    private int outputY;

    Paint shadowPaint;
    Paint linePaint;

    private Rect drawRect;
    private Rect selection;

    // Programatic entry point
    public CropImageView(Context context) {
        super(context);
        initCropImageView(context);
    }

    // XML entry point
    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCropImageView(context);
    }

    protected void initCropImageView(Context context) {

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb((int) (255 * 0.6), 0, 0, 0));
        linePaint = new Paint();
        linePaint.setColor(Color.rgb(0x99, 0x99, 0x99));

        drawRect = new Rect();

        transIgnoreScale = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            selection = updateSelection();
        }
    }

    public void setOutput(int outputX, int outputY) {
        this.outputX = outputX;
        this.outputY = outputY;
    }

    public byte[] getCroppedImage() {
        Bitmap cropped = getCroppedBitmap();
        if (cropped == null) {
            return null;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        if (cropped != mBitmap) {
            cropped.recycle();
        }
        byte[] data = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean saveCroppedImage(String path) {
        Bitmap cropped = getCroppedBitmap();
        return AttachmentStore.saveBitmap(cropped, path, cropped != mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (selection != null) {
            canvas.drawLine(selection.left, selection.top, selection.right, selection.top, linePaint);
            canvas.drawLine(selection.left, selection.top, selection.left, selection.bottom, linePaint);
            canvas.drawLine(selection.right, selection.top, selection.right, selection.bottom, linePaint);
            canvas.drawLine(selection.left, selection.bottom, selection.right, selection.bottom, linePaint);

            drawRect.set(0, 0, getRight(), selection.top);
            canvas.drawRect(drawRect, shadowPaint);
            drawRect.set(0, selection.top, selection.left, selection.bottom);
            canvas.drawRect(drawRect, shadowPaint);
            drawRect.set(selection.right, selection.top, getRight(), selection.bottom);
            canvas.drawRect(drawRect, shadowPaint);
            drawRect.set(0, selection.bottom, getRight(), getBottom());
            canvas.drawRect(drawRect, shadowPaint);
        }
    }

    @Override
    protected Rect updateSelection() {
        if (outputX <= 0 || outputY <= 0) {
            return null;
        }
        Rect selection;

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float outputRatio = ((float) outputY) / outputX;
        float screenRatio = ((float) viewHeight) / viewWidth;
        if (outputRatio < screenRatio) {
            int width = viewWidth - MARGIN * 2;
            int height = outputY * width / outputX;
            int x = MARGIN;
            int y = (viewHeight - height) / 2;
            selection = new Rect(x, y, x + width, y + height);
        } else {
            int height = viewHeight - MARGIN * 2;
            int width = outputX * height / outputY;
            int y = MARGIN;
            int x = (viewWidth - width) / 2;
            selection = new Rect(x, y, x + width, y + height);
        }

        return selection;
    }

    private Bitmap getCroppedBitmap() {
        Bitmap bitmap = getImageBitmap();
        if (bitmap == null) {
            return null;
        }
        if (selection == null) {
            return bitmap;
        }

        Matrix matrix = getImageViewMatrix();
        float transX = getValue(matrix, Matrix.MTRANS_X);
        float transY = getValue(matrix, Matrix.MTRANS_Y);
        float scale = getValue(matrix, Matrix.MSCALE_X);

        int x = (int) ((selection.left - transX) / scale);
        int y = (int) ((selection.top - transY) / scale);
        int width = (int) (selection.width() / scale);
        int height = (int) (selection.height() / scale);

        // 边界判断
        x = (x >= 0 ? x : 0);
        y = (y >= 0 ? y : 0);
        width = (width <= bitmap.getWidth() - x ? width : bitmap.getWidth() - x);
        height = (height <= bitmap.getHeight() - y ? height : bitmap.getHeight() - y);

        // 保持长宽比
        float outputRatio = ((float) outputY) / outputX;
        float screenRatio = ((float) height) / width;
        if (outputRatio < screenRatio) {
            height = (int) (width * outputRatio);
        } else {
            width = (int) (height / outputRatio);
        }

        Matrix m = new Matrix();
        final float sx = outputX / (float) width;
        m.setScale(sx, sx);
        try {

            return Bitmap.createBitmap(bitmap, x, y, width, height, m, false);
        } catch (Exception e) {

            return null;
        }

    }

    @Override
    protected void center(boolean vertical, boolean horizontal, boolean animate) {
        if (mBitmap == null)
            return;
        if (selection == null) {
            invalidate();
            return;
        }

        Matrix m = getImageViewMatrix();

        float[] topLeft = new float[]{0, 0};
        float[] botRight = new float[]{mBitmap.getWidth(), mBitmap.getHeight()};

        translatePoint(m, topLeft);
        translatePoint(m, botRight);

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            if (topLeft[1] > selection.bottom) {
                deltaY = selection.bottom - topLeft[1];
            } else if (botRight[1] < selection.top) {
                deltaY = selection.top - botRight[1];
            }
        }

        if (horizontal) {
            if (topLeft[0] > selection.right) {
                deltaX = selection.right - topLeft[0];
            } else if (botRight[0] < selection.left) {
                deltaX = selection.left - botRight[0];
            }
        }

        postTranslate(deltaX, deltaY);
        if (animate) {
            Animation a = new TranslateAnimation(-deltaX, 0, -deltaY, 0);
            a.setStartTime(SystemClock.elapsedRealtime());
            a.setDuration(250);
            setAnimation(a);
        }
        setImageMatrix(getImageViewMatrix());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = super.onTouchEvent(event);

        if (mBitmap != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (fling()) {
                    if (checkImagePosition(false)) {
                        stopFling();
                    }
                } else {
                    checkImagePosition(true);
                }
            }
        }

        return handled;
    }


    // Sets the bitmap for the image and resets the base
    public void setImageBitmap(final Bitmap bitmap) {
        super.setImageBitmap(bitmap, selection);
    }

    /**
     * 把图片移动回到截屏的矩形框内，保证图片能完全覆盖截屏矩形
     *
     * @return
     * @date 2014年5月22日
     */
    protected boolean checkImagePosition(boolean scroll) {

        boolean translate = false;
        if (mBitmap == null || selection == null) {
            return translate;
        }
        Matrix m = getImageViewMatrix();

        float[] topLeft = new float[]{0, 0};
        float[] botRight = new float[]{mBitmap.getWidth(), mBitmap.getHeight()};

        translatePoint(m, topLeft);
        translatePoint(m, botRight);
        float transX = 0.0f;
        float transY = 0.0f;

        if (topLeft[0] > selection.left) {
            transX = selection.left - topLeft[0];
            translate = true;
        } else if (botRight[0] < selection.right) {
            transX = selection.right - botRight[0];
            translate = true;
        }

        if (topLeft[1] > selection.top) {
            transY = selection.top - topLeft[1];
            translate = true;
        } else if (botRight[1] < selection.bottom) {
            transY = selection.bottom - botRight[1];
            translate = true;
        }

        if (scroll && translate) {
            //直线动画的效果移回
            scrollBy(transX, transY, 200);
        }

        return translate;
    }

    @Override
    protected void onScrollFinish() {
        checkImagePosition(true);
    }
}
