package com.netease.nim.uikit.common.ui.barrage;

import java.util.List;

/**
 * 弹幕全局配置：
 * 1.最大最小的文本尺寸SP
 * 2.文本颜色集合
 * 3.一个文本弹幕在屏幕上滚动的时间
 */
public class BarrageConfig {
    private int minTextSizeSp = 14;
    private int maxTextSizeSp = 18;
    private int duration = 5000;
    private List<Integer> colors;

    public BarrageConfig() {
    }

    public int getMinTextSizeSp() {
        return minTextSizeSp;
    }

    public void setMinTextSizeSp(int minTextSizeSp) {
        this.minTextSizeSp = minTextSizeSp;
    }

    public int getMaxTextSizeSp() {
        return maxTextSizeSp;
    }

    public void setMaxTextSizeSp(int maxTextSizeSp) {
        this.maxTextSizeSp = maxTextSizeSp;
    }

    public List<Integer> getColors() {
        return colors;
    }

    public void setColors(List<Integer> colors) {
        this.colors = colors;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
