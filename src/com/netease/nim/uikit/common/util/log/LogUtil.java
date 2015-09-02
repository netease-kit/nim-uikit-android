package com.netease.nim.uikit.common.util.log;

public class LogUtil {
    public static final void init(String logFile, int level) {
        LogImpl.init(logFile, level);
    }

	public static final void v(String tag, String msg) {
		LogImpl.v(tag, buildMessage(msg));
	}

	public static final void v(String tag, String msg, Throwable thr) {
		LogImpl.v(tag, buildMessage(msg), thr);
	}

	public static final void d(String tag, String msg) {
		LogImpl.d(tag, buildMessage(msg));
	}

	public static final void d(String tag, String msg, Throwable thr) {
		LogImpl.d(tag, buildMessage(msg), thr);
	}

	public static final void i(String tag, String msg) {
		LogImpl.i(tag, buildMessage(msg));
	}

	public static final void i(String tag, String msg, Throwable thr) {
		LogImpl.i(tag, buildMessage(msg), thr);
	}

	public static final void w(String tag, String msg) {
		LogImpl.w(tag, buildMessage(msg));
	}

	public static final void w(String tag, String msg, Throwable thr) {
		LogImpl.w(tag, buildMessage(msg), thr);
	}

	public static final void w(String tag, Throwable thr) {
		LogImpl.w(tag, buildMessage(""), thr);
	}

	public static final void e(String tag, String msg) {
		LogImpl.e(tag, buildMessage(msg));
	}

	public static final void e(String tag, String msg, Throwable thr) {
		LogImpl.e(tag, buildMessage(msg), thr);
	}
	
	public static final void ui(String msg) {
		LogImpl.i("ui", buildMessage(msg));
	}

	public static final void core(String msg) {
		LogImpl.i("core", buildMessage(msg));
	}
	
	public static final void coreDebug(String msg) {
		LogImpl.d("core", buildMessage(msg));
	}

	public static final void res(String msg) {
		LogImpl.i("RES", buildMessage(msg));
	}
	
	public static final void resDebug(String msg) {
		LogImpl.d("RES", buildMessage(msg));
	}

	public static final void audio(String msg) {
		LogImpl.i("AudioRecorder", buildMessage(msg));
	}
	
	public static void vincent(String msg) {
        LogImpl.v("Vincent", buildMessage(msg));
	}
	
	public static void pipeline(String prefix, String msg) {
		LogImpl.i("Pipeline", prefix + " " + buildMessage(msg));
	}

    public static void pipelineDebug(String prefix, String msg) {
        LogImpl.d("Pipeline", prefix + " " + buildMessage(msg));
    }
	
	public static String getLogFileName(String cat) {
		return LogImpl.getLogFileName(cat);
	}

	private static String buildMessage(String msg) {
		return msg;
	}
}
