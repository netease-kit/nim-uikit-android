package com.netease.nim.uikit.common.util.log.sdk.wrapper;

import android.text.TextUtils;

import com.netease.nim.uikit.common.util.log.sdk.LogBase;

/**
 * 云信日志基类
 * <p>
 * Created by huangjun on 2017/9/20.
 */

public abstract class AbsNimLog {

    private static LogBase log;
    private static String process; // 进程标识,体现在tag里

    protected static void init(LogBase logImpl, String processName, String logDir, String logFileName,
                               int level, int maxLength, int baseLength, boolean shrink, LogBase.LogInterceptor interceptor) {
        log = logImpl;
        process = processName;
        log.init(logDir, logFileName, level, maxLength, baseLength, shrink, interceptor);
    }

    public static void tryFlush() {
        if (log != null) {
            log.tryFlush();
        }
    }

    protected static LogBase getLog() {
        return log;
    }

    public static void v(String tag, String msg) {
        log.v(buildTag(tag), buildMessage(msg));
    }

    public static void v(String tag, String msg, Throwable thr) {
        log.v(buildTag(tag), buildMessage(msg), thr);
    }

    public static void d(String tag, String msg) {
        log.d(buildTag(tag), buildMessage(msg));
    }

    public static void d(String tag, String msg, Throwable thr) {
        log.d(buildTag(tag), buildMessage(msg), thr);
    }

    public static void i(String tag, String msg) {
        log.i(buildTag(tag), buildMessage(msg));
    }

    public static void i(String tag, String msg, Throwable thr) {
        log.i(buildTag(tag), buildMessage(msg), thr);
    }

    public static void w(String tag, String msg) {
        log.w(buildTag(tag), buildMessage(msg));
    }

    public static void w(String tag, String msg, Throwable thr) {
        log.w(buildTag(tag), buildMessage(msg), thr);
    }

    public static void w(String tag, Throwable thr) {
        log.w(buildTag(tag), buildMessage(""), thr);
    }

    public static void e(String tag, String msg) {
        log.e(buildTag(tag), buildMessage(msg));
    }

    public static void e(String tag, String msg, Throwable thr) {
        log.e(buildTag(tag), buildMessage(msg), thr);
    }

    public static void ui(String msg) {
        log.i(buildTag("ui"), buildMessage(msg));
    }

    public static void core(String msg) {
        log.i(buildTag("core"), buildMessage(msg));
    }

    public static void test(String msg) {
        log.d(buildTag("test"), buildMessage(msg));
    }

    protected static String buildTag(String tag) {
        return TextUtils.isEmpty(process) ? tag : "[" + process + "]" + tag;
    }

    protected static String buildMessage(String msg) {
        return msg;
    }
}
