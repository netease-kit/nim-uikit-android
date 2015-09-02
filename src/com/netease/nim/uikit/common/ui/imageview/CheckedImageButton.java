package com.netease.nim.uikit.common.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * 选中图片控件
 */
public class CheckedImageButton extends ImageButton {

    private boolean checked;

    private int normalBkResId;

    private int checkedBkResId;

    private Drawable normalImage;

    private Drawable checkedImage;

    private int leftPadding, topPadding, rightPadding, bottomPadding;

    public CheckedImageButton(Context context) {
        super(context);
    }

    public CheckedImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckedImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPaddingValue(int value) {
        setPaddingValue(value, value, value, value);
    }

    public void setPaddingValue(int left, int top, int right, int bottom) {
        leftPadding = left;
        topPadding = top;
        rightPadding = right;
        bottomPadding = bottom;
        setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean push) {
        this.checked = push;

        Drawable image = push ? checkedImage : normalImage;
        if (image != null) {
            updateImage(image);
        }

        int background = push ? checkedBkResId : normalBkResId;
        if (background != 0) {
            updateBackground(background);
        }
    }

    public void setNormalBkResId(int normalBkResId) {
        this.normalBkResId = normalBkResId;
        updateBackground(normalBkResId);
    }

    public void setCheckedBkResId(int checkedBkResId) {
        this.checkedBkResId = checkedBkResId;
    }

    public void setNormalImageId(int normalResId) {
        normalImage = getResources().getDrawable(normalResId);
        updateImage(normalImage);
    }

    public void setCheckedImageId(int pushedResId) {
        checkedImage = getResources().getDrawable(pushedResId);
    }

    public void setNormalImage(Bitmap bitmap) {

        this.normalImage = new BitmapDrawable(getResources(), bitmap);
        updateImage(this.normalImage);
    }

    public void setCheckedImage(Bitmap bitmap) {
        this.checkedImage = new BitmapDrawable(getResources(), bitmap);
    }

    private void updateBackground(int resId) {
        setBackgroundResource(resId);
        setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
//        int padding = ScreenUtil.dip2px(7);
//        setPadding(padding, padding, padding, padding);
    }

    private void updateImage(Drawable drawable) {
        //  setScaleType(ScaleType.FIT_CENTER);
        setImageDrawable(drawable);
    }
}
