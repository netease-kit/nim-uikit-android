package com.netease.nim.uikit.business.ait.selector.model;

/**
 * Created by hzchenkang on 2017/6/21.
 */

public class AitContactItem<T> {

    // view type
    private int viewType;

    // data
    private T model;

    public AitContactItem(int viewType, T model) {
        this.viewType = viewType;
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    public int getViewType() {
        return viewType;
    }
}
