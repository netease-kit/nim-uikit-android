package com.netease.nim.uikit.common.framework.infra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class DefaultTaskRegistry implements TaskRegistry {
    /**
     * tasks
     */
    public HashMap<String, Task> tasks = new HashMap<String, Task>();

    @Override
    public Task register(Task task) {
        // key
        String key = task.key();

        synchronized (tasks) {
            Task tsk = tasks.get(key);

            if (tsk == null) {
                tsk = task;

                tasks.put(key, task);
            }

            return tsk;
        }
    }

    @Override
    public Task unregister(Task task) {
        // key
        String key = task.key();

        synchronized (tasks) {
            return tasks.remove(key);
        }
    }

    @Override
    public boolean registered(Task task) {
        // key
        String key = task.key();

        synchronized (tasks) {
            return tasks.containsKey(key);
        }
    }

    @Override
    public Task query(String key) {
        synchronized (tasks) {
            return tasks.get(key);
        }
    }

    @Override
    public Collection<Task> queryAll() {
        synchronized (tasks) {
            return new ArrayList<Task>(tasks.values());
        }
    }

    @Override
    public int count() {
        synchronized (tasks) {
            return tasks.size();
        }
    }
}