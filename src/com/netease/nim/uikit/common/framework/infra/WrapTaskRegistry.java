package com.netease.nim.uikit.common.framework.infra;

import java.util.Collection;

public class WrapTaskRegistry implements TaskRegistry {
    private TaskRegistry wrap;

    public WrapTaskRegistry(TaskRegistry wrap) {
        this.wrap = wrap;
    }

    @Override
    public Task register(Task task) {
        return wrap.register(task);
    }

    @Override
    public Task unregister(Task task) {
        return wrap.unregister(task);
    }

    @Override
    public boolean registered(Task task) {
        return wrap.registered(task);
    }

    @Override
    public Task query(String key) {
        return wrap.query(key);
    }

    @Override
    public Collection<Task> queryAll() {
        return wrap.queryAll();
    }

    @Override
    public int count() {
        return wrap.count();
    }
}
