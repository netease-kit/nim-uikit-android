package com.netease.nim.uikit.common.util.log.sdk;

import com.netease.nim.uikit.common.util.log.sdk.util.FileUtils;

import java.io.File;

/**
 * I/O write 方式按日期打日志，按天生成日志，日志名 prefix_date.log。支持多进程同时读写，不丢日志，写入效率较低（比NLogImpl还要牺牲一个获取日期文件名的开销）。
 * <p>
 * Created by huangjun on 2017/9/20.
 */
public class NDateLogImpl extends NLogImpl {

    private String logDir;
    private String logNamePrefix;

    @Override
    public void init(final String logDir, final String logNamePrefix, final int level,
                     final int maxLength, final int baseLength, final boolean shrink, final LogInterceptor interceptor) {
        this.logDir = logDir;
        this.logNamePrefix = logNamePrefix;
        final String logName = LogFormat.getLogFileName(logNamePrefix);

        super.init(logDir, logName, level, maxLength, baseLength, shrink, interceptor);
    }

    @Override
    void writeLog(final String log) {
        this.logPath = logDir + File.separator + LogFormat.getLogFileName(logNamePrefix);
        FileUtils.appendFile(log, logPath);
    }
}