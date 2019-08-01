package com.netease.nim.uikit.common.framework.infra;

import java.util.Collection;

public interface TaskRegistry {
    /**
     * register task
     *
     * @param task
     * @return task registered
     */
    public Task register(Task task);

    /**
     * unregister task
     *
     * @param task
     * @return task unregistered
     */
    public Task unregister(Task task);

    /**
     * task registered
     *
     * @param task
     * @return registered
     */
    public boolean registered(Task task);

    /**
     * query task
     *
     * @param key
     * @return task
     */
    public Task query(String key);

    /**
     * query all tasks registered
     *
     * @return
     */
    public Collection<Task> queryAll();

    /**
     * count
     *
     * @return count
     */
    public int count();
}