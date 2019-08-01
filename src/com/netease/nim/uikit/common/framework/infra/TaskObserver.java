package com.netease.nim.uikit.common.framework.infra;

public interface TaskObserver {
    /**
     * on task result
     *
     * @param task
     * @param results
     */
    public void onTaskResult(Task task, Object[] results);

    /**
     * on task progress
     *
     * @param task
     * @param params
     */
    public void onTaskProgress(Task task, Object[] params);
}
