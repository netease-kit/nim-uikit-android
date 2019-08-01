package com.netease.nim.uikit.common.adapter;

/**
 */

public class BaseViewHolderData<T> {

    public static final int DEFAULT_VIEW_TYPE = -1;
    public static final int DEFAULT_HEADER_TYPE = -2;
    public static final int DEFAULT_FOOTER_TYPE = -3;

    private int viewType;

    private T data;

    public BaseViewHolderData() {
        viewType = DEFAULT_VIEW_TYPE;
    }

    public BaseViewHolderData(int viewType) {
        this.viewType = viewType;
    }

    public BaseViewHolderData(int viewType, T data) {
        this.viewType = viewType;
        this.data = data;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
