package com.netease.nim.uikit.common.framework.infra;

public abstract class ObservableTask extends Task {
    private TaskObservable observable = new TaskObservable();

    /**
     * register observer
     *
     * @param observer
     */
    public void registerObserver(TaskObserver observer) {
        observable.registerObserver(observer);
    }

    /**
     * unregister observer
     *
     * @param observer
     */
    public void unregisterObserver(TaskObserver observer) {
        observable.unregisterObserver(observer);
    }

    /**
     * notify task result
     *
     * @param results
     */
    protected final void notifyTaskResult(Object[] results) {
        // observable
        observable.onTaskResult(this, results);
    }

    /**
     * notify task progress
     *
     * @param params
     */
    protected final void notifyTaskProgress(Object[] params) {
        // observable
        observable.onTaskProgress(this, params);
    }
}
