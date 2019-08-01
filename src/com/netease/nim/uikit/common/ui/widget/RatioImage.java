package com.netease.nim.uikit.common.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.netease.nim.uikit.R;

/**
 */

public class RatioImage extends android.support.v7.widget.AppCompatImageView {

    private float ratioWidth;

    private float ratioHeight;

    // 0 for width
    // 1 for height
    private int standard;

    private static final int sEnumWidth = 0;
    private static final int sEnumHeight = 1;

    public RatioImage(Context context) {
        this(context, null);
    }

    public RatioImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatioImage, defStyleAttr, 0);
        if (ta != null) {
            ratioWidth = ta.getFloat(R.styleable.RatioImage_ri_ratio_width, 1);
            ratioHeight = ta.getFloat(R.styleable.RatioImage_ri_ratio_height, 1);
            standard = ta.getInt(R.styleable.RatioImage_ri_standard, 0);

            ta.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        switch (standard) {
            case sEnumWidth:
                // 以width为准
                height = (int) (width / ratioWidth * ratioHeight);
                break;
            case sEnumHeight:
                // 以height为准
                width = (int) (height / ratioHeight * ratioWidth);
                break;
        }

        setMeasuredDimension(width, height);
    }
}
