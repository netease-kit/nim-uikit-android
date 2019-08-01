package com.netease.nim.uikit.common.framework.infra;

import java.util.concurrent.Executor;

public class CompoundTaskWorker extends AbstractTaskWorker {
    private AbstractTaskWorker[] workers;

    public CompoundTaskWorker(AbstractTaskWorker... workers) {
        this.workers = workers;
    }

    protected int dispatch(Task task) {
        return 0;
    }

    @Override
    protected Executor getTaskHost(Task task) {
        int index = dispatch(task);

        if (index >= 0 && index < workers.length) {
            return workers[index].getTaskHost(task);
        }

        return null;
    }
}
