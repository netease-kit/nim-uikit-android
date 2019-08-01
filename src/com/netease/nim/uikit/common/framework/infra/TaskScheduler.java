package com.netease.nim.uikit.common.framework.infra;

public interface TaskScheduler {
    /**
     * schedule
     *
     * @param background
     * @param task
     * @param key
     * @param params
     * @return scheduled task
     */
    public Task schedule(boolean background, String key, Task task, Object... params);

    /**
     * reschedule
     *
     * @param task
     */
    public void reschedule(Task task);

    /**
     * unschedule
     *
     * @param task
     */
    public void unschedule(Task task);

    /**
     * scheduled
     *
     * @param key
     * @return Task
     */
    public Task scheduled(String key);

    /**
     * count
     *
     * @return count
     */
    public int count();

    /**
     * cancelAll
     */
    public void cancelAll();
}