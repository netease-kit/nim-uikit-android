package com.netease.nim.uikit.common.framework.infra;

public abstract class DefaultTask extends ManagedTask {
    private DefaultTaskCallback callback;

    public DefaultTask(DefaultTaskCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onTaskResult(Object[] results) {
        if (results != null) {
            notifyResult((Integer) results[0], results[1]);
        }
    }

    private void notifyResult(int result, Object attachment) {
        if (callback != null) {
            callback.onFinish(key(), result, attachment);
        }
    }
}
