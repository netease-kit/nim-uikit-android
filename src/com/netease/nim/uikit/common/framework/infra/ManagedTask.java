package com.netease.nim.uikit.common.framework.infra;

import android.text.TextUtils;

import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ManagedTask extends ObservableTask {
    private static final String TAG = "ManagedTask";
    private static final String ENCLOSURE = "()";

    private static AtomicInteger serial = new AtomicInteger();

    private TaskManager taskManager;

    private TaskObserver taskObserver;

    private final TaskObserver getTaskObserver() {
        if (taskObserver == null) {
            taskObserver = new TaskObserver() {
                @Override
                public void onTaskResult(Task task, Object[] results) {
                    if (!cancelled()) {
                        onDepTaskResult(task, results);
                    }
                }

                @Override
                public void onTaskProgress(Task task, Object[] params) {
                    if (!cancelled()) {
                        onDepTaskProgress(task, params);
                    }
                }
            };
        }

        return taskObserver;
    }

    private ArrayList<ManagedTask> links = new ArrayList<ManagedTask>();

    /**
     * on task result
     *
     * @param results
     */
    protected void onTaskResult(Object[] results) {
    }

    /**
     * on task progress
     *
     * @param params
     */
    protected void onTaskProgress(Object[] params) {
    }

    /**
     * on dependent task result
     *
     * @param task
     * @param results
     */
    protected void onDepTaskResult(Task task, Object[] results) {
    }

    /**
     * on dependent task progress
     *
     * @param task
     * @param params
     */
    protected void onDepTaskProgress(Task task, Object[] params) {
    }

    /**
     * on task result
     *
     * @param results
     */
    @Override
    protected final void onPublishResult(Object[] results) {
        // self
        if (!cancelled()) {
            onTaskResult(results);
        }
        notifyTaskResult(results);

        // links
        synchronized (links) {
            for (ManagedTask link : links) {
                link.onPublishResult(results);
            }
        }
    }

    /**
     * on task progress
     *
     * @param params
     */
    @Override
    protected final void onPublishProgress(Object[] params) {
        // self
        if (!cancelled()) {
            onTaskProgress(params);
        }
        notifyTaskProgress(params);

        // links
        synchronized (links) {
            for (ManagedTask link : links) {
                link.onPublishProgress(params);
            }
        }
    }

    /**
     * attach task manager
     *
     * @param taskManager
     */
    /*package*/
    final void attachTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * an same task has been scheduled
     *
     * @param task
     */
    /*package*/
    final void onSameTask(ManagedTask task) {
        // link
        task.link(this);
    }

    /**
     * make task key
     *
     * @param task
     * @param params
     * @return
     */
	/*package*/
    static final String makeTaskKey(ManagedTask task, Object... params) {
        String tag = task.getTaskTag();
        String id = task.getTaskId();
        String extraId = task.getTaskExtraId(params);

        StringBuilder sb = new StringBuilder();

        // tag
        sb.append("T");
        sb.append(ENCLOSURE.charAt(0));
        sb.append(tag);
        sb.append(ENCLOSURE.charAt(1));

        // id
        sb.append("I");
        sb.append(ENCLOSURE.charAt(0));
        sb.append(id);
        sb.append(ENCLOSURE.charAt(1));

        // extra id
        if (!TextUtils.isEmpty(extraId)) {
            sb.append("E");
            sb.append(ENCLOSURE.charAt(0));
            sb.append(extraId);
            sb.append(ENCLOSURE.charAt(1));
        }

        return sb.toString();
    }

    /**
     * get task tag
     *
     * @return task tag
     */
    protected String getTaskTag() {
        return getClass().getSimpleName();
    }

    /**
     * get task id
     *
     * @return task id
     */
    protected String getTaskId() {
        return Integer.toString(serial.getAndIncrement());
    }

    /**
     * get task extra id
     *
     * @param params
     * @return
     */
    protected String getTaskExtraId(Object... params) {
        return null;
    }

    /**
     * schedule task
     *
     * @param background
     * @param task
     * @param params
     * @return task key
     */
    protected String schedule(boolean background, ManagedTask task, Object... params) {
        // register
        task.registerObserver(getTaskObserver());

        // execute
        return taskManager.schedule(background, task, params);
    }

    /**
     * reschedule self
     */
    protected void reschedule() {
        taskManager.reschedule(this);
    }

    /**
     * cancel linked tasks recursively
     */
    @Override
    public void cancel() {
        super.cancel();
        synchronized (links) {
            for (ManagedTask link : links) {
                link.cancel();
            }
        }
    }

    /**
     * link task
     *
     * @param task
     */
    private final void link(ManagedTask task) {
        trace("link " + dump(true));

        synchronized (links) {
            links.add(task);
        }
    }

    private static final void trace(String msg) {
        LogUtil.d(TAG, msg);
    }
}
