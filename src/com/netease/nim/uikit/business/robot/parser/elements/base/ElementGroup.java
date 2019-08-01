package com.netease.nim.uikit.business.robot.parser.elements.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjun on 2017/6/22.
 * <p>
 * 支持嵌套的容器
 */

public abstract class ElementGroup<T extends Element> extends Element {

    private List<T> elements;

    protected void addElement(T e) {
        if (elements == null) {
            elements = new ArrayList<>();
        }

        if (e != null) {
            elements.add(e);
        }
    }

    protected void addElements(List<T> es) {
        if (elements == null) {
            elements = new ArrayList<>();
        }

        if (es != null) {
            elements.addAll(es);
        }
    }

    public List<T> getElements() {
        return elements;
    }
}
