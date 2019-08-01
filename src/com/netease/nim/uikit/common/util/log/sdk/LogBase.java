package com.netease.nim.uikit.common.util.log.sdk;

import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.uikit.common.util.log.sdk.util.FileUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 日志基类
 * 保证open/write/close/flush都在同个线程操作
 * <p>
 * Created by huangjun on 2017/3/7.
 */

public abstract class LogBase {

    public interface LogInterceptor {
        /**
         * 子类实现写日志前处理逻辑，例如本地存储读写权限检查，只有返回 true 才会执行写操作
         *
         * @return 是否允许执行写操作
         */
        boolean checkValidBeforeWrite();
    }

    static final int K = 1024;
    static final int M = 1024 * K;
    private static final int DEFAULT_MAX_LENGTH = 8 * M; // 每次初始化的时候检查，大于该值时缩减log文件
    private static final int DEFAULT_BASE_LENGTH = 4 * M; // 如果大于MAX_LENGTH，缩减到该值
    private static final boolean RUN_ON_SINGLE_THREAD = true; // 默认在独立线程上排队写日志，测试性能可以改成在主线程上执行

    int maxLength;
    int baseLength;

    String logPath;
    private int level = Log.DEBUG;
    private LogInterceptor interceptor;

    private final Executor logger = Executors.newSingleThreadExecutor();

    /**
     * 动态设置日志级别
     *
     * @param level 日志级别
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 日志初始化
     *
     * @param logDir      日志文件所在的目录
     * @param logName     日志文件名
     * @param maxLength   日志文件最大容量
     * @param baseLength  日志文件裁减后的容量
     * @param level       日志级别
     * @param shrink      是否需要裁剪
     * @param interceptor 日志拦截器，可以在执行写日志前判断本地权限、磁盘是否准备好，决定是否要日志
     */
    public void init(final String logDir, final String logName, final int level, final int maxLength, final int baseLength, final boolean shrink, final LogInterceptor interceptor) {
        this.logPath = FileUtils.getFilePath(logDir, logName);
        this.level = level;
        this.interceptor = interceptor;
        this.maxLength = maxLength;
        this.baseLength = baseLength;

        if (this.maxLength <= 0) {
            this.maxLength = DEFAULT_MAX_LENGTH;
        }
        if (this.baseLength <= 0) {
            this.baseLength = DEFAULT_BASE_LENGTH;
        }

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                open(shrink);
            }
        };

        run(runnable);
    }

    /**
     * 关闭日志，清理缓存
     */
    public void destroy() {
        if (TextUtils.isEmpty(logPath)) {
            return;
        }

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                close();
            }
        };

        run(runnable);
    }

    /**
     * 尝试回写数据到文件中
     */
    public void tryFlush() {
        if (TextUtils.isEmpty(logPath)) {
            return;
        }

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                forceFlush();
            }
        };

        run(runnable);
    }

    /**
     * 写日志
     */
    private void o(final int priority, final String tag, final String msg, final Throwable tr) {
        if (TextUtils.isEmpty(logPath) || TextUtils.isEmpty(msg)) {
            return;
        }

        final long time = System.currentTimeMillis();
        final long threadId = Thread.currentThread().getId();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String timeStr = LogFormat.formatTime(time);
                Log.println(priority, tag, threadId + "/" + msg + '\n' + Log.getStackTraceString(tr));

                if (level <= priority && (interceptor == null || interceptor.checkValidBeforeWrite())) {
                    writeLog(LogFormat.formatLog(tag, timeStr, msg, tr));
                }
            }
        };

        run(runnable);
    }

    private void run(Runnable runnable) {
        if (RUN_ON_SINGLE_THREAD) {
            logger.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public void i(String tag, String msg, Throwable tr) {
        o(Log.INFO, tag, msg, tr);
    }

    public void v(String tag, String msg) {
        v(tag, msg, null);
    }

    public void v(String tag, String msg, Throwable tr) {
        o(Log.VERBOSE, tag, msg, tr);
    }

    public void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public void e(String tag, String msg, Throwable tr) {
        o(Log.ERROR, tag, msg, tr);
    }

    public void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public void d(String tag, String msg, Throwable tr) {
        o(Log.DEBUG, tag, msg, tr);
    }

    public void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public void w(String tag, String msg, Throwable tr) {
        o(Log.WARN, tag, msg, tr);
    }

    private void o(int priority, String tag, String msg) {
        o(priority, tag, msg, null);
    }

    /**
     * 子类实现打开日志
     *
     * @param shrink 是否裁剪
     */
    abstract void open(final boolean shrink);

    /**
     * 子类实现写日志
     *
     * @param log 日志内容
     */
    abstract void writeLog(final String log);

    /**
     * 强制将缓存的数据回写到文件
     */
    abstract void forceFlush();

    /**
     * 子类实现关闭日志，清理缓存
     */
    abstract void close();
}
