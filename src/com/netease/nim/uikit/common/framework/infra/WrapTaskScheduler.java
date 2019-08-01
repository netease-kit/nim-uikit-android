package com.netease.nim.uikit.common.framework.infra;

public class WrapTaskScheduler implements TaskScheduler {
    private TaskScheduler wrap;

    public WrapTaskScheduler(TaskScheduler wrap) {
        this.wrap = wrap;
    }

    @Override
    public Task schedule(boolean background, String key, Task task, Object... params) {
        return wrap.schedule(background, key, task, params);
    }

    @Override
    public void reschedule(Task task) {
        wrap.reschedule(task);
    }

    @Override
    public void unschedule(Task task) {
        wrap.unschedule(task);
    }

    @Override
    public Task scheduled(String key) {
        return wrap.scheduled(key);
    }

    @Override
    public int count() {
        return wrap.count();
    }

    @Override
    public void cancelAll() {
        wrap.cancelAll();
    }
}
