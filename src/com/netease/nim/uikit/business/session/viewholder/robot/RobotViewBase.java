package com.netease.nim.uikit.business.session.viewholder.robot;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.netease.nim.uikit.business.robot.parser.elements.base.Element;

/**
 * Created by hzchenkang on 2017/6/26.
 */

public abstract class RobotViewBase<T extends Element> extends FrameLayout {

    protected T element;

    protected String content;

    public RobotViewBase(Context context, @Nullable AttributeSet attrs, T element, String content) {
        super(context, attrs);
        this.element = element;
        this.content = content;
        LayoutInflater.from(context).inflate(getResLayout(), this);
        onInflate();
    }

    public RobotViewBase(Context context, T element, String content) {
        this(context, null, element, content);
    }

    public RobotViewBase(Context context) {
        this(context, null, null);
    }

    public T getElement() {
        return element;
    }

    protected abstract int getResLayout();

    protected abstract void onInflate();

    protected abstract void onBindContentView();

    public abstract void onParentMeasured(int width, int height);

    protected LinearLayout.LayoutParams createLayoutParams() {
        return null;
    }

    public String getShowContent() {
        return null;
    }
}
