package com.netease.nim.uikit.common.ui.barrage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * SurfaceView 任务队列驱动/间隔绘制模板
 * Created by huangjun on 2016/5/8.
 */
public abstract class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas canvas;
    private boolean isRunning;
    private final Object lock = new Object();

    public SurfaceViewTemplate(Context context) {
        this(context, null);
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);

        // 设置Surface在Window（普通视图架构）之上
        setZOrderOnTop(true);
        // 设置色彩格式，半透明
        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        // 设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);

        // 设置保持屏幕亮
        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        new Thread(this).start(); // 开启线程
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        notifyHasTask(); // 释放锁
        isRunning = false; // 通知关闭线程
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // 判断是否还有绘制任务，任务队列空可以先wait，等待任务唤醒
                synchronized (lock) {
                    if (!hasTask()) {
                        lock.wait();
                    }
                }

                // 执行绘制任务
                draw();

                // 控制绘制的时间间隔
                Thread.sleep(getRunTimeInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw() {
        try {
            // get canvas
            canvas = mHolder.lockCanvas(); // 如果SurfaceView不在前台，这里会阻塞
            if (canvas != null) {
                // clear screen
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                // do draw task
                onDrawView(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                mHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 执行绘制任务
     */
    protected abstract void onDrawView(Canvas canvas);

    /**
     * 绘制任务执行间隔
     */
    protected abstract int getRunTimeInterval();

    /**
     * 是否有绘制任务
     */
    protected abstract boolean hasTask();

    protected void notifyHasTask() {
        synchronized (lock) {
            lock.notify();
        }
    }
}