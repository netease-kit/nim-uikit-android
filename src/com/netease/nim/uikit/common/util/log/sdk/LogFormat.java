package com.netease.nim.uikit.common.util.log.sdk;

import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志格式
 * <p>
 * Created by huangjun on 2017/3/8.
 */
class LogFormat {

    /**
     * ******************************* log content format *****************************
     */

    private static final String MESSAGE_FORMAT = "MM-dd HH:mm:ss.SSS";
    private static final DateFormat messageFormat = new SimpleDateFormat(MESSAGE_FORMAT, Locale.getDefault());
    private static final Date date = new Date(); // 复用
    private static long lastTime;
    private static String lastTimeStr;

    static String formatTime(final long time) {
        if (time == lastTime) {
            return lastTimeStr;
        }

        date.setTime(time);
        lastTime = time;
        return lastTimeStr = messageFormat.format(date);
    }

    static String formatLog(String tag, String time, String msg, Throwable tr) {
        StringBuilder sb = new StringBuilder();

        // time
        sb.append(time);
        sb.append(": ");

        // tag
        sb.append(tag);
        sb.append(": ");

        // content
        sb.append(msg);
        sb.append("\r\n");

        // Throwable
        if (tr != null) {
            sb.append(Log.getStackTraceString(tr));
            sb.append("\r\n");
        }

        return sb.toString();
    }

    /**
     * ******************************* date file format *****************************
     */

    private static final String SUFFIX = ".log";
    private static final String FILE_FORMAT = "yyyyMMdd";
    private static final DateFormat fileNameFormat = new SimpleDateFormat(FILE_FORMAT, Locale.getDefault());

    static String getLogFileName(String prefix) {
        // nim_20170911.log or 20170911.log
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(prefix)) {
            sb.append(prefix);
            sb.append("_");
        }

        date.setTime(System.currentTimeMillis());
        sb.append(fileNameFormat.format(date));
        sb.append(SUFFIX);

        return sb.toString();
    }
}
