package com.netease.nim.uikit.common.ui.ptr2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

/**
 * Created by sunpingji.
 */
public class LoadingView extends View {

    private static final float DEFAULT_RADIUS = ScreenUtil.dip2px(5);
    private static final int DEFAULT_BALL_COLOR = 0xffffffff;
    private static final float DEFAULT_SPEED = (float) 0.5;

    private Paint paint = new Paint();

    private Paint paint2 = new Paint();

    private PointF leftBall;
    private PointF rightBall;

    //两个小球的碰撞参数
    private float handleLenRate = 1f;

    private float radius = DEFAULT_RADIUS;
    private float mv = 0.5f;
    private float maxTouchDistance = (float) (DEFAULT_RADIUS * 2.5);


    private int ballLeftColor = DEFAULT_BALL_COLOR;
    private int ballRightColor = DEFAULT_BALL_COLOR;


    //两个小球动画距离
    private float ballMoveDistance = (float) (DEFAULT_RADIUS * 3.5);

    //默认1秒 speed = 1/time;故speed=0.5表示2秒结束动画
    private float animationSpeed = DEFAULT_SPEED;

    private float baseX;
    private float baseY;

    private boolean isNeedAnimation;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void reset() {
        if (leftBall != null && rightBall != null) {
            leftBall.x = baseX;
            rightBall.x = baseX;
        }
    }

    public void setPaintMode(int mode) {
        paint.setStyle(mode == 0 ? Paint.Style.STROKE : Paint.Style.FILL);
        paint2.setStyle(mode == 0 ? Paint.Style.STROKE : Paint.Style.FILL);
        invalidate();

        leftBall.x = rightBall.x;
    }

    private void init(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingView);
        ballLeftColor = a.getColor(R.styleable.LoadingView_left_ball_color, DEFAULT_BALL_COLOR);
        ballRightColor = a.getColor(R.styleable.LoadingView_right_ball_color, DEFAULT_BALL_COLOR);
        radius = a.getFloat(R.styleable.LoadingView_radius, DEFAULT_RADIUS);
        maxTouchDistance = (float) (radius * 2.5);
        ballMoveDistance = (float) (radius * 3.5);
        animationSpeed = a.getFloat(R.styleable.LoadingView_animation_speed, DEFAULT_SPEED);
        isNeedAnimation = a.getBoolean(R.styleable.LoadingView_need_animation, true);
        a.recycle();
        paint.setColor(ballLeftColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint2.setColor(ballRightColor);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);
    }

    private void initMetaballs() {
        baseX = this.getMeasuredWidth() / 2;
        baseY = this.getMeasuredHeight() / 2;
        leftBall = new PointF(baseX, baseY);
        rightBall = new PointF(baseX, baseY);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            initMetaballs();
        }
    }

    private float[] getVector(float radians, float length) {
        float x = (float) (Math.cos(radians) * length);
        float y = (float) (Math.sin(radians) * length);
        return new float[]{
                x, y
        };
    }

    /**
     * @param canvas          画布
     * @param v               控制两个圆连接时候长度，间接控制连接线的粗细，该值为1的时候连接线为直线
     * @param handle_len_rate
     * @param maxDistance
     */
    private void metaball(Canvas canvas, float v, float handle_len_rate, float maxDistance) {

        float d = getDistance();

        float radius1 = radius;
        float radius2 = radius;
        float pi2 = (float) (Math.PI / 2);
        float u1, u2;


        if (d > maxDistance) {
            canvas.drawCircle(rightBall.x, rightBall.y, radius, paint2);
        } else {
            canvas.drawCircle(rightBall.x, rightBall.y, radius, paint2);
        }

        if (radius1 == 0 || radius2 == 0) {
            return;
        }

        if (d > maxDistance || d <= Math.abs(radius1 - radius2)) {
            return;
        } else if (d < radius1 + radius2) {
            u1 = (float) Math.acos((radius1 * radius1 + d * d - radius2 * radius2) /
                    (2 * radius1 * d));
            u2 = (float) Math.acos((radius2 * radius2 + d * d - radius1 * radius1) /
                    (2 * radius2 * d));
        } else {
            u1 = 0;
            u2 = 0;
        }
        float[] centermin = new float[]{rightBall.x - leftBall.x, rightBall.y - leftBall.y};

        float angle1 = (float) Math.atan2(centermin[1], centermin[0]);
        float angle2 = (float) Math.acos((radius1 - radius2) / d);
        float angle1a = angle1 + u1 + (angle2 - u1) * v;
        float angle1b = angle1 - u1 - (angle2 - u1) * v;
        float angle2a = (float) (angle1 + Math.PI - u2 - (Math.PI - u2 - angle2) * v);
        float angle2b = (float) (angle1 - Math.PI + u2 + (Math.PI - u2 - angle2) * v);


        float[] p1a1 = getVector(angle1a, radius1);
        float[] p1b1 = getVector(angle1b, radius1);
        float[] p2a1 = getVector(angle2a, radius2);
        float[] p2b1 = getVector(angle2b, radius2);

        float[] p1a = new float[]{p1a1[0] + leftBall.x, p1a1[1] + leftBall.y};
        float[] p1b = new float[]{p1b1[0] + leftBall.x, p1b1[1] + leftBall.y};
        float[] p2a = new float[]{p2a1[0] + rightBall.x, p2a1[1] + rightBall.y};
        float[] p2b = new float[]{p2b1[0] + rightBall.x, p2b1[1] + rightBall.y};


        float[] p1_p2 = new float[]{p1a[0] - p2a[0], p1a[1] - p2a[1]};

        float totalRadius = (radius1 + radius2);
        float d2 = Math.min(v * handle_len_rate, getLength(p1_p2) / totalRadius);
        d2 *= Math.min(1, d * 2 / (radius1 + radius2));

        radius1 *= d2;
        radius2 *= d2;

        float[] sp1 = getVector(angle1a - pi2, radius1);
        float[] sp2 = getVector(angle2a + pi2, radius2);
        float[] sp3 = getVector(angle2b - pi2, radius2);
        float[] sp4 = getVector(angle1b + pi2, radius1);


        Path path1 = new Path();
        path1.moveTo(p1a[0], p1a[1]);
        path1.cubicTo(p1a[0] + sp1[0], p1a[1] + sp1[1], p2a[0] + sp2[0], p2a[1] + sp2[1], p2a[0], p2a[1]);
        path1.lineTo(p2b[0], p2b[1]);
        path1.cubicTo(p2b[0] + sp3[0], p2b[1] + sp3[1], p1b[0] + sp4[0], p1b[1] + sp4[1], p1b[0], p1b[1]);
        path1.lineTo(p1a[0], p1a[1]);
        path1.close();
        canvas.drawPath(path1, paint2);

    }


    public float getBallMoveDistance() {
        return ballMoveDistance;
    }

    private float getLength(float[] b) {
        return (float) Math.sqrt(b[0] * b[0] + b[1] * b[1]);
    }

    private float getDistance() {
        float x = leftBall.x - rightBall.x;
        float y = leftBall.y - rightBall.y;
        float d = x * x + y * y;
        return (float) Math.sqrt(d);
    }

    public void setBaseX(float scale) {
        if (leftBall == null || rightBall == null) {
            return;
        }
        leftBall.x = baseX - scale * ballMoveDistance / 2;
        rightBall.x = baseX + scale * ballMoveDistance / 2;
        invalidate();
    }

    public void setNeedAnimation(boolean animation) {
        isNeedAnimation = animation;
        invalidate();
    }

    private void startAnimation() {
        if (isNeedAnimation) {
            float input = (float) ((System.currentTimeMillis() % 10000 * animationSpeed % 1000) * 1.000 / 1000);
            float ratio = (float) ((Math.sin(2 * Math.PI * input)) * ballMoveDistance);
            leftBall.x = baseX + ratio;
            rightBall.x = baseX - ratio;
            float scaleRatio = (float) ((Math.cos(2 * Math.PI * input)) * DEFAULT_RADIUS * 0.2);
            radius = (float) (DEFAULT_RADIUS * 0.8 + scaleRatio);
            maxTouchDistance = (float) (radius * 2.5);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (leftBall == null || rightBall == null) {
            initMetaballs();
        }
        canvas.drawCircle(leftBall.x, leftBall.y, radius, paint);
        metaball(canvas, mv, handleLenRate, maxTouchDistance);
        startAnimation();
    }

}
