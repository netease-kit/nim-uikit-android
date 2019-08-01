package com.netease.nim.uikit.common.framework.infra;

import android.os.Handler;
import android.os.Looper;

public class DefaultTaskScheduler implements TaskScheduler {
    /**
     * registry
     */
    private final TaskRegistry registry = new DefaultTaskRegistry();

    /**
     * execute callback
     */
    private final AbstractTaskWorker.ExecuteCallback executeCallback = new AbstractTaskWorker.ExecuteCallback() {
        @Override
        public void onExecuted(Task task, boolean unschedule) {
            if (unschedule) {
                unschedule(task);
            }
        }
    };

    /**
     * worker
     */
    private final AbstractTaskWorker worker;

    private final Handler handler;

    private static final Handler sharedHandler = new Handler(Looper.getMainLooper());

    public DefaultTaskScheduler(AbstractTaskWorker worker) {
        this(worker, sharedHandler);
    }

    public DefaultTaskScheduler(AbstractTaskWorker worker, Handler handler) {
        worker.setExecuteCallback(executeCallback);

        this.worker = worker;
        this.handler = handler;
    }

    @Override
    public Task schedule(boolean background, String key, Task task, Object... params) {
        // attach
        task.info = new Task.Info(background, key, params);
        task.state = new Task.State();
        task.handler = handler;

        // register
        Task tsk = registry.register(task);

        if (task == tsk) {
            // execute
            worker.execute(task);
        }

        return tsk;
    }

    @Override
    public void reschedule(Task task) {
        if (registry.registered(task)) {
            // execute
            worker.execute(task);
        }
    }

    @Override
    public void unschedule(Task task) {
        registry.unregister(task);
    }

    @Override
    public Task scheduled(String key) {
        return registry.query(key);
    }

    @Override
    public int count() {
        return registry.count();
    }

    @Override
    public void cancelAll() {
        for (Task task : registry.queryAll()) {
            if (task.scheduled() > 0) {
                registry.unregister(task);
            }
            task.cancel();
        }
    }
}
