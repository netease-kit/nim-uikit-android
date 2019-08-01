package com.netease.nim.uikit.common.framework.infra;

public class DefaultTaskWorker extends TaskWorker {
    public DefaultTaskWorker() {
        this("Default");
    }

    public DefaultTaskWorker(String name) {
        this(name, TaskExecutor.defaultConfig);
    }

    public DefaultTaskWorker(String name, TaskExecutor.Config config) {
        super(new TaskExecutor("TW#" + name, config));
    }
}
