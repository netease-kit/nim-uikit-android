package com.netease.nim.uikit.common.media.imagepicker.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.netease.nim.uikit.R;


/**
 */

public class ProgressView extends View {
    //constant
    private int millisecond = 1000;//每一秒
    private float maxProgressSize = CaptureActivity.RECORD_MAX_TIME * millisecond;//总进度是10s
    private float eachProgressWidth = 0;//每一格的宽度

    private float eachProgressAngle = 0;

    private Context mContext;
    private WindowManager mWindowManager;
    //    private Paint progressPaint;

    // 画圆环的画笔
    private Paint ringPaint;
    // 画字体的画笔
    private Paint textPaint;
    // 圆环颜色
    private int ringColor;
    // 字体颜色
    private int textColor;
    // 半径
    private float radius;
    // 圆环宽度
    private float strokeWidth;
    // 字的长度
    private float txtWidth;
    // 字的高度
    private float txtHeight;
    // 总进度
    private int totalProgress = 100;
    // 当前进度
    private float currentProgress = 0f;

    private float currentAngle = 0f;
    //  透明度
    private int alpha = 25;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(context, attrs);
        initVariable();
    }

    private void init(Context context, AttributeSet attrs) {
        //设置每一刻度的宽度
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        eachProgressWidth = dm.widthPixels / (maxProgressSize * 1.0f);
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressbar, 0, 0);
        radius = typeArray.getDimension(R.styleable.CircleProgressbar_cp_radius, 80);
        strokeWidth = typeArray.getDimension(R.styleable.CircleProgressbar_strokeWidth, 10);
        ringColor = typeArray.getColor(R.styleable.CircleProgressbar_ringColor, 0xFF0000);
        textColor = typeArray.getColor(R.styleable.CircleProgressbar_textColor, 0xFFFFFF);


        eachProgressAngle = 360 / (maxProgressSize * 1.0f);

    }

    private void initVariable() {
        ringPaint = new Paint();
        ringPaint.setAntiAlias(true);
        ringPaint.setDither(true);
        ringPaint.setColor(ringColor);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setStrokeWidth(strokeWidth);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setTextSize(radius / 2);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        txtHeight = fm.descent + Math.abs(fm.ascent);
    }

    private long initTime = -1;//上一次刷新完成后的时间
    private boolean isStart = false;
    private float countWidth = 0;//进度条进度的进程，每次调用invalidate（）都刷新一次

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isStart) {
            drawCirecle(canvas, currentAngle);
            return;
        }
        if (initTime == -1) {
            initTime = System.currentTimeMillis();
            drawCirecle(canvas, currentAngle);
            invalidate();
            return;
        }
        //这次刷新的时间，用于与上一次刷新完成的时间作差得出进度条需要增加的进度
        long thisTime = System.currentTimeMillis();
        currentAngle += eachProgressAngle * (thisTime - initTime) * 1.0f;
        if (currentAngle > 360) {
            currentAngle = 360;
        }

        drawCirecle(canvas, currentAngle);

        if (currentAngle < 360 && isStart) {
            initTime = System.currentTimeMillis();
            invalidate();
        } else {
            currentAngle = 0;
            initTime = -1;
            isStart = false;
        }
    }

    private void drawCirecle(Canvas canvas, float currentAngle) {
        if (currentProgress >= 0 && currentProgress <= 100) {
            RectF oval = new RectF(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius,
                    getHeight() / 2 + radius);
            canvas.drawArc(oval, -90, currentAngle, false, ringPaint);
            String txt = currentProgress + "%";
            txtWidth = textPaint.measureText(txt, 0, txt.length());
            canvas.drawText(txt, getWidth() / 2 - txtWidth / 2, getHeight() / 2 + txtHeight / 4, textPaint);
        }
    }

    //开始或暂停进度条进度刷新
    public void setIsStart(boolean isStart) {
        if (isStart == this.isStart) {
            return;
        }
        this.isStart = isStart;
        if (isStart) {
            initTime = -1;
            invalidate();
        }
    }

    //重置进度条
    public void reset() {
        currentAngle = 0;
        initTime = -1;
        isStart = false;
        invalidate();
    }

    //设置每一个像素的宽度
    public void setEachProgressWidth(int width) {
        eachProgressWidth = width / (maxProgressSize * 1.0f);
    }
}
