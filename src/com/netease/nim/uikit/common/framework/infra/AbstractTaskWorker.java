package com.netease.nim.uikit.common.framework.infra;

import java.util.concurrent.Executor;

public abstract class AbstractTaskWorker {
    public interface ExecuteCallback {
        public void onExecuted(Task task, boolean unschedule);
    }

    /**
     * execute callback
     */
    private ExecuteCallback executeCallback;

    /**
     * dispatch
     *
     * @param task
     * @return Executor
     */
    protected abstract Executor getTaskHost(Task task);

    public AbstractTaskWorker() {

    }

    public void setExecuteCallback(ExecuteCallback executeCallback) {
        this.executeCallback = executeCallback;
    }

    public void execute(Task task) {
        getExecutor(task).execute(getRunnable(task));
    }

    private final Executor getExecutor(Task task) {
        if (task.info.background) {
            Executor executor = getTaskHost(task);
            if (executor != null) {
                return executor;
            }
        }

        return TaskExecutor.IMMEDIATE_EXECUTOR;
    }

    private final Runnable getRunnable(final Task task) {
        return new Runnable() {
            @Override
            public void run() {
                // schedule
                boolean unschedule = task.schedule();

                // callback
                if (executeCallback != null) {
                    executeCallback.onExecuted(task, unschedule);
                }
            }
        };
    }
}
