package com.netease.nim.uikit.common.framework.infra;

public class TraceTaskRegistry extends WrapTaskRegistry {
    public TraceTaskRegistry(TaskRegistry wrap) {
        super(wrap);
    }

    @Override
    public Task register(Task task) {
        Task tsk = super.register(task);

        if (tsk == task) {
            trace("register " + tsk.dump(false));
        }

        return tsk;
    }

    @Override
    public Task unregister(Task task) {
        Task tsk = super.unregister(task);

        if (tsk != null) {
            trace("unregister " + tsk.dump(false));
        }

        return tsk;
    }

    private final void trace(String msg) {

    }
}

