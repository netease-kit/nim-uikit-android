package com.netease.nim.uikit.common.http;

import android.content.Context;
import android.os.Handler;

import com.netease.nim.uikit.common.framework.NimTaskExecutor;

import java.util.Map;

/**
 * Created by huangjun on 2015/3/6.
 */
public class NimHttpClient {

    /**
     * *********************** Http Task & Callback *************************
     */
    public interface NimHttpCallback {
        void onResponse(String response, int code, Throwable e);
    }

    public class NimHttpTask implements Runnable {

        private String url;
        private Map<String, String> headers;
        private String jsonBody;
        private NimHttpCallback callback;
        private boolean post;

        public NimHttpTask(String url, Map<String, String> headers, String jsonBody, NimHttpCallback callback) {
            this(url, headers, jsonBody, callback, true);
        }

        public NimHttpTask(String url, Map<String, String> headers, String jsonBody, NimHttpCallback callback, boolean post) {
            this.url = url;
            this.headers = headers;
            this.jsonBody = jsonBody;
            this.callback = callback;
            this.post = post;
        }

        @Override
        public void run() {
            final HttpClientWrapper.HttpResult<String> result = post ?
                    HttpClientWrapper.post(url, headers, jsonBody) : HttpClientWrapper.get(url, headers);

            // do callback on ui thread
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onResponse(result.obj, result.code, result.e);
                    }
                }
            });
        }
    }

    /**
     * ************************ Single instance **************************
     */
    private static NimHttpClient instance;

    public synchronized static NimHttpClient getInstance() {
        if (instance == null) {
            instance = new NimHttpClient();
        }

        return instance;
    }

    private NimHttpClient() {

    }

    /**
     * **************** Http Config & Thread pool & Http Client ******************
     */

    private boolean inited = false;

    private NimTaskExecutor executor;

    private Handler uiHandler;

    public void init(Context context) {
        if (inited) {
            return;
        }

        // init thread pool
        executor = new NimTaskExecutor("NIM_HTTP_TASK_EXECUTOR", new NimTaskExecutor.Config(1, 3, 10 * 1000, true));
        uiHandler = new Handler(context.getMainLooper());
        inited = true;
    }

    public void release() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void execute(String url, Map<String, String> headers, String body, NimHttpCallback callback) {
        execute(url, headers, body, true, callback);
    }

    public void execute(String url, Map<String, String> headers, String body, boolean post, NimHttpCallback callback) {
        if (!inited) {
            return;
        }

        executor.execute(new NimHttpTask(url, headers, body, callback, post));
    }
}
