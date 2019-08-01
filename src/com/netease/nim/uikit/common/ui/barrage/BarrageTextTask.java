package com.netease.nim.uikit.common.ui.barrage;

import android.text.TextPaint;

/**
 * Created by huangjun on 2016/5/8.
 */
class BarrageTextTask {
    // build
    private String text;
    private int line;
    private int duration;
    private float x;
    private float y;
    private float deltaX;
    private float length;
    private TextPaint paint;

    // inner
    private float runX;
    private boolean hasFree;

    BarrageTextTask(String text, int line, int color, int size, int duration, float x, float y, float deltaX) {
        this.text = text;
        this.line = line;
        this.duration = duration;
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.paint = new TextPaint();
        this.paint.setTextSize(size);
        this.paint.setColor(color);
        this.length = paint.measureText(text);
        this.runX = 0.0f;
        this.hasFree = false;
    }

    void updatePosition() {
        runX += deltaX;
        x -= deltaX;
    }

    boolean canFreeLine() {
        if (hasFree) {
            return false;
        }

        if (runX > length + 60.0f) {
            hasFree = true;
            return true;
        }

        return false;
    }

    boolean isEnd() {
        return x < -1 * length; // 是否应该结束
    }

    String getText() {
        return text;
    }

    int getLine() {
        return line;
    }

    int getDuration() {
        return duration;
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }

    TextPaint getPaint() {
        return paint;
    }
}
