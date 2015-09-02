package com.netease.nim.uikit.common.framework;

import android.os.Handler;

import com.netease.nim.uikit.NimUIKit;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by huangjun on 2015/3/12.
 */
public class NimDBExecutor {

    private static NimDBExecutor instance;

    private Handler uiHander;
    private Executor executor;

    private NimDBExecutor() {
        uiHander = new Handler(NimUIKit.getContext().getMainLooper());
        executor = Executors.newSingleThreadExecutor();
    }

    public synchronized static NimDBExecutor getInstance() {
        if (instance == null) {
            instance = new NimDBExecutor();
        }

        return instance;
    }

    public <T> void execute(NimDBTask<T> task) {
        if (executor != null) {
            executor.execute(new NimDBRunnable<>(task));
        }
    }

    public void execute(Runnable runnable) {
        if (executor != null) {
            executor.execute(runnable);
        }
    }

    /**
     * ****************** model *************************
     */

    public interface NimDBTask<T> {
        public T runInBackground();

        public void onCompleted(T result);
    }

    private class NimDBRunnable<T> implements Runnable {

        public NimDBRunnable(NimDBTask<T> task) {
            this.task = task;
        }

        private NimDBTask<T> task;

        @Override
        public void run() {
            final T res = task.runInBackground();
            if (uiHander != null) {
                uiHander.post(new Runnable() {
                    @Override
                    public void run() {
                        task.onCompleted(res);
                    }
                });
            }
        }
    }
}
