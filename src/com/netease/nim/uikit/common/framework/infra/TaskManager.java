package com.netease.nim.uikit.common.framework.infra;

public class TaskManager {
    /**
     * task scheduler
     */
    private TaskScheduler ts;

    private boolean shutdown = false;

    public TaskManager(TaskScheduler ts) {
        this.ts = ts;
        TaskManagerRegistry.register(this);
    }

    public String schedule(ManagedTask task, Object... params) {
        return schedule(true, task, params);
    }

    public String schedule(boolean background, ManagedTask task, Object... params) {
        // check state
        if (shutdown) {
            return null;
        }

        // attach
        task.attachTaskManager(this);

        // as task key
        String key = ManagedTask.makeTaskKey(task, params);

        // execute
        ManagedTask tsk = (ManagedTask) ts.schedule(background, key, task, params);

        // same
        if (tsk != task) {
            task.onSameTask(tsk);
        }

        // result
        return key;
    }

    public void reschedule(ManagedTask task) {
        // check state
        if (shutdown) {
            return;
        }

        ts.reschedule(task);
    }

    public boolean scheduled(String key) {
        return ts.scheduled(key) != null;
    }

    public int count() {
        return ts.count();
    }

    public void cancel(String key) {
        Task task = ts.scheduled(key);

        if (task != null) {
            task.cancel();
        }
    }

    public void shutdown() {
        shutdown = true;
        cancelAll();
    }

    public void cancelAll() {
        ts.cancelAll();
    }

    public void setProperty(String key, int prop, Object data) {
        Task task = ts.scheduled(key);

        if (task != null) {
            task.setProperty(prop, data);
        }
    }
}
