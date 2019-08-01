package com.netease.nim.uikit.common.framework.infra;

public class CallbackWrapper<T> implements TaskObserver {
    protected T callback;

    public CallbackWrapper(T callback) {
        this.callback = callback;
    }

    @Override
    public void onTaskResult(Task task, Object[] results) {
    }

    @Override
    public void onTaskProgress(Task task, Object[] params) {
    }
}