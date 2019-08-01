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
 * 长按录制时候的倒数进度条
 */
public class CircleProgressView extends View {


    //constant
    private int millisecond = 1000;//每一秒
    private float maxProgressSize = CaptureActivity.RECORD_MAX_TIME * millisecond;//总进度是10s

    private float eachProgressAngle = 0;

    private Context mContext;
    private WindowManager mWindowManager;

    // 画圆环的画笔
    private Paint ringPaint;
    // 画字体的画笔
    private Paint backgroundPaint;
    // 圆环颜色
    private int ringColor;
    // 半径
    private float radius;
    // 圆环宽度
    private float strokeWidth;
    private float currentAngle = 0f;

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressbar, 0, 0);
        radius = typeArray.getDimension(R.styleable.CircleProgressbar_cp_radius, 80);
        strokeWidth = typeArray.getDimension(R.styleable.CircleProgressbar_strokeWidth, 10);
        ringColor = typeArray.getColor(R.styleable.CircleProgressbar_ringColor, 0xFF0000);
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

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setDither(true);
        backgroundPaint.setColor(getResources().getColor(R.color.white_alpha_50));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundPaint.setStrokeWidth(strokeWidth);
    }

    private long initTime = -1;//上一次刷新完成后的时间
    private boolean isStart = false;

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
        if (currentAngle > 0 && currentAngle <= 360) {
            RectF oval = new RectF(getWidth() / 2 - radius, getHeight() / 2 - radius, getWidth() / 2 + radius,
                    getHeight() / 2 + radius);
            canvas.drawArc(oval, -90 + currentAngle, 360 - currentAngle, false, backgroundPaint);
            canvas.drawArc(oval, -90, currentAngle, false, ringPaint);
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
}
