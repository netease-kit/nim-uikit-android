package com.netease.nim.uikit.common.framework.infra;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TaskExecutor implements Executor {
    private final static int QUEUE_INIT_CAPACITY = 11;

    private static final int CORE = 3;

    private static final int MAX = 5;

    private static final int TIMEOUT = 30 * 1000;

    public static final Executor IMMEDIATE_EXECUTOR = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    public static class Config {
        public int core;

        public int max;

        public int timeout;

        public boolean allowCoreTimeOut;

        public Config(int core, int max, int timeout, boolean allowCoreTimeOut) {
            this.core = core;
            this.max = max;
            this.timeout = timeout;
            this.allowCoreTimeOut = allowCoreTimeOut;
        }
    }

    public static Config defaultConfig = new Config(CORE, MAX, TIMEOUT, true);

    private final String name;

    private final Config config;

    private ExecutorService service;

    public TaskExecutor(String name) {
        this(name, defaultConfig);
    }

    public TaskExecutor(String name, Config config) {
        this(name, config, true);
    }

    public TaskExecutor(String name, Config config, boolean startup) {
        this.name = name;
        this.config = config;

        if (startup) {
            startup();
        }
    }

    public void startup() {
        synchronized (this) {
            // has startup
            if (service != null && !service.isShutdown()) {
                return;
            }

            // create
            service = createExecutor(config);
        }
    }

    public void shutdown() {
        ExecutorService executor = null;

        synchronized (this) {
            // swap
            if (service != null) {
                executor = service;
                service = null;
            }
        }

        if (executor != null) {
            // shutdown
            if (!executor.isShutdown()) {
                executor.shutdown();
            }

            // recycle
            executor = null;
        }
    }

    @Override
    public void execute(Runnable runnable) {
        // executeRunnable runnable with default priority
        executeRunnable(new PRunnable(runnable, 0));
    }

    public Future<?> submit(Runnable runnable) {
        synchronized (this) {
            if (service == null || service.isShutdown()) {
                return null;
            }
            return service.submit(new PRunnable(runnable, 0));
        }
    }

    public void execute(Runnable runnable, int priority) {
        // executeRunnable runnable with priority
        executeRunnable(new PRunnable(runnable, priority));
    }

    private void executeRunnable(Runnable runnable) {
        synchronized (this) {
            // has shutdown, reject
            if (service == null || service.isShutdown()) {
                return;
            }

            // execute
            service.execute(runnable);
        }
    }

    private ExecutorService createExecutor(Config config) {
        ThreadPoolExecutor service = new ThreadPoolExecutor(config.core, config.max, config.timeout,
                TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>(QUEUE_INIT_CAPACITY, mQueueComparator),
                new TaskThreadFactory(name), new ThreadPoolExecutor.DiscardPolicy());

        allowCoreThreadTimeOut(service, config.allowCoreTimeOut);

        return service;
    }

    private static class PRunnable implements Runnable {
        private static int sSerial = 0;

        private Runnable runnable;

        private int priority;

        private int serial;

        public PRunnable(Runnable r, int p) {
            serial = sSerial++;
            runnable = r;
            priority = p;
        }

        @Override
        public void run() {
            if (runnable != null) {
                runnable.run();
            }
        }

        public static final int compare(PRunnable r1, PRunnable r2) {
            if (r1.priority != r2.priority) {
                return r2.priority - r1.priority;
            } else {
                return r1.serial - r2.serial;
            }
        }
    }

    Comparator<Runnable> mQueueComparator = new Comparator<Runnable>() {

        @Override
        public int compare(Runnable lhs, Runnable rhs) {
            PRunnable r1 = (PRunnable) lhs;
            PRunnable r2 = (PRunnable) rhs;

            return PRunnable.compare(r1, r2);
        }
    };

    static class TaskThreadFactory implements ThreadFactory {
        private final ThreadGroup mThreadGroup;

        private final AtomicInteger mThreadNumber = new AtomicInteger(1);

        private final String mNamePrefix;

        TaskThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();

            mThreadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();

            mNamePrefix = name + "#";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(mThreadGroup, r, mNamePrefix + mThreadNumber.getAndIncrement(), 0);

            // no daemon
            if (t.isDaemon())
                t.setDaemon(false);

            // normal priority
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);

            return t;
        }
    }

    private static final void allowCoreThreadTimeOut(ThreadPoolExecutor service, boolean value) {
        if (Build.VERSION.SDK_INT >= 9) {
            allowCoreThreadTimeOut9(service, value);
        }
    }

    @TargetApi(9)
    private static final void allowCoreThreadTimeOut9(ThreadPoolExecutor service, boolean value) {
        service.allowCoreThreadTimeOut(value);
    }
}