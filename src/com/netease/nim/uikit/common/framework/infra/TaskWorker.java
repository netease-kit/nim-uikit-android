package com.netease.nim.uikit.common.framework.infra;

import java.util.concurrent.Executor;

public class TaskWorker extends AbstractTaskWorker {
    /**
     * executor
     */
    private Executor executor;

    public TaskWorker(Executor executor) {
        this.executor = executor;
    }

    @Override
    protected Executor getTaskHost(Task task) {
        return executor;
    }
}
