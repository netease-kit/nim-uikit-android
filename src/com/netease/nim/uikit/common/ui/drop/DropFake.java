package com.netease.nim.uikit.common.ui.drop;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by huangjun on 2016/9/13.
 */
public class DropFake extends View {

    public interface ITouchListener {
        void onDown();

        void onMove(float curX, float curY);

        void onUp();
    }

    private int radius; // 圆形半径
    private float circleX; // 圆心x坐标
    private float circleY; // 圆心y坐标
    private String text; // 要显示的文本（数字）
    private boolean firstInit = true; // params init once
    private ITouchListener touchListener;

    public DropFake(Context context, AttributeSet attrs) {
        super(context, attrs);

        DropManager.getInstance().initPaint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (firstInit) {
            firstInit = false;
            radius = DropManager.CIRCLE_RADIUS; // 或者view.getWidth()/2
            circleX = w / 2;
            circleY = h / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // circle
        canvas.drawCircle(circleX, circleY, radius, DropManager.getInstance().getCirclePaint());
        // text
        if (!TextUtils.isEmpty(text)) {
            canvas.drawText(text, circleX, circleY + DropManager.getInstance().getTextYOffset(),
                    DropManager.getInstance().getTextPaint());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (DropManager.getInstance().isTouchable()) {
                    if (touchListener != null) {
                        DropManager.getInstance().setTouchable(false);
                        // 不允许父控件处理TouchEvent，当父控件为ListView这种本身可滑动的控件时必须要控制
                        getParent().requestDisallowInterceptTouchEvent(true);
                        touchListener.onDown();
                    }
                    return true; // eat
                }

                return false;
            case MotionEvent.ACTION_MOVE:
                if (touchListener != null) {
                    // getRaw:获取手指当前所处的相对整个屏幕的坐标
                    touchListener.onMove(event.getRawX(), event.getRawY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (touchListener != null) {
                    // 将控制权还给父控件
                    getParent().requestDisallowInterceptTouchEvent(false);
                    touchListener.onUp();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public String getText() {
        return text;
    }

    public void setClickListener(ITouchListener listener) {
        touchListener = listener;
    }
}
