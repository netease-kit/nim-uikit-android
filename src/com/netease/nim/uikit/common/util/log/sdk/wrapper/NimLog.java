package com.netease.nim.uikit.common.util.log.sdk.wrapper;

import com.netease.nim.uikit.common.util.log.sdk.LogBase;
import com.netease.nim.uikit.common.util.log.sdk.NDateLogImpl;
import com.netease.nim.uikit.common.util.log.sdk.NLogImpl;

/**
 * 云信日志封装
 * <p>
 * Created by huangjun on 2017/9/20.
 */
public class NimLog extends AbsNimLog {

    /**
     * 初始化多进程日志系统，支持多个进程写入一个日志文件
     * 适用场景：多进程，对性能没有特别高的要求
     * 缺点：持续频繁打日志会造成CPU内核态占用升高
     *
     * @param processName 进程名，单进程可以填 null，每行日志tag为[process]tag
     * @param logDir      日志所在的目录，一般是 SD 卡下的目录绝对路径
     * @param logFileName 日志名称，例如 nim_sdk.log
     * @param level       日志级别，大于等于此日志级别的日志才会被写入
     * @param maxLength   日志最大的文件大小，日志初始化时(一般是进程启动后)，判断日志如果超过此大小将进行裁剪，填0表示默认值8M
     * @param baseLength  日志裁减后的大小，填0表示默认值4M
     * @param shrink      日志初始化时是否进行裁减
     * @param interceptor 日志拦截器，可以在执行写日志前判断本地权限、磁盘是否准备好，决定是否要日志
     */
    public static void initNLog(String processName, String logDir, String logFileName, int level, int maxLength, int baseLength, boolean shrink, LogBase.LogInterceptor interceptor) {
        init(new NLogImpl(), processName, logDir, logFileName, level, maxLength, baseLength, shrink, interceptor);
    }

    /**
     * 初始化多进程按日期存储的日志系统，支持多个进程写入一个日志文件，每天产生一个日志文件。
     * 适用场景：多进程，要求按天记录日志，对性能没有特别高的要求。
     * 缺点：持续频繁打日志会造成CPU内核态占用升高
     *
     * @param processName       进程名，单进程可以填 null，每行日志tag为[process]tag
     * @param logDir            日志所在的目录，一般是 SD 卡下的目录绝对路径
     * @param logFileNamePrefix 日志名前缀，会自动根据日期生成日志，例如nim_20170911.log
     * @param level             日志级别，大于等于此日志级别的日志才会被写入
     * @param maxLength         日志最大的文件大小，日志初始化时(一般是进程启动后)，判断日志如果超过此大小将进行裁剪，填0表示默认值8M
     * @param baseLength        日志裁减后的大小，填0表示默认值4M
     * @param shrink            日志初始化时是否进行裁减
     * @param interceptor       日志拦截器，可以在执行写日志前判断本地权限、磁盘是否准备好，决定是否要日志
     */
    public static void initDateNLog(String processName, String logDir, String logFileNamePrefix, int level, int maxLength, int baseLength, boolean shrink, LogBase.LogInterceptor interceptor) {
        init(new NDateLogImpl(), processName, logDir, logFileNamePrefix, level, maxLength, baseLength, shrink, interceptor);
    }
}
