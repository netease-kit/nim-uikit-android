package com.netease.nim.uikit.common.http;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by huangjun on 2016/11/21.
 */

public class HttpClientWrapper {

    private static final String TAG = "http";

    private static final Integer TIMEOUT = 30 * 1000; // 允许反射修此常量，如果是基本类型，那么编译时会被优化直接替换，运行时无法在修改。
    private static final int BUFFER_SIZE = 1024;
    private static final int RES_CODE_SUCCESS = 200;
    private static final String CHARSET = "UTF-8";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";

    public static class HttpResult<T> {
        public int code;
        public Throwable e;
        public T obj;

        public HttpResult() {
            this.code = 0;
            e = null;
            obj = null;
        }
    }

    public static String buildRequestParams(Map<String, Object> params) {
        if (params == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }

        return result.toString();
    }

    public static HttpResult<String> get(final String urlStr, final Map<String, String> headers) {
        LogUtil.d(TAG, "http get url=" + urlStr);
        HttpResult<String> result = new HttpResult<>();

        HttpURLConnection urlConnection = null;
        try {
            // conn
            urlConnection = buildGet(urlStr, headers);

            // request
            int resCode = result.code = urlConnection.getResponseCode(); // 开始连接并发送数据

            // response
            if (resCode == RES_CODE_SUCCESS) {
                result.obj = buildString(urlConnection.getInputStream());
                LogUtil.d(TAG, "http get success, result=" + result.obj + ", url=" + urlStr);
            } else {
                LogUtil.e(TAG, "http get failed, code=" + resCode + ", url=" + urlStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.e = e;
            LogUtil.e(TAG, "http get error, e=" + e.getMessage() + ", url=" + urlStr);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    public static <T> HttpResult<String> post(final String urlStr, final Map<String, String> headers, T body) {
        LogUtil.d(TAG, "http post url=" + urlStr);
        HttpResult<String> result = new HttpResult<>();

        HttpURLConnection urlConnection = null;
        try {
            // conn
            urlConnection = buildPost(urlStr, headers, body); // os.flush 开始建立连接

            // request
            int resCode = result.code = urlConnection.getResponseCode(); // 开始发送数据

            // response
            if (resCode == RES_CODE_SUCCESS) {
                result.obj = buildString(urlConnection.getInputStream());
                LogUtil.d(TAG, "http post success, result=" + result + ", url=" + urlStr);
            } else {
                LogUtil.e(TAG, "http post failed, code=" + resCode + ", url=" + urlStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.e = e;
            LogUtil.e(TAG, "http post error, e=" + e.getMessage() + ", url=" + urlStr);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    private static HttpURLConnection buildGet(String urlStr, final Map<String, String> headers) throws IOException {
        URL url = new URL(urlStr); // URLEncoder.encode(param, "UTF-8")

        // conn
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        configUrlConnection(urlConnection);
        urlConnection.setRequestMethod(HTTP_GET);

        // headers
        buildHeaders(urlConnection, headers);

        return urlConnection;
    }

    private static <T> HttpURLConnection buildPost(String urlStr, final Map<String, String> headers, T body) throws IOException {
        URL url = new URL(urlStr);

        // conn
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        configUrlConnection(urlConnection);
        urlConnection.setRequestMethod(HTTP_POST);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        // headers
        buildHeaders(urlConnection, headers);

        // json body
        buildJsonHeaders(urlConnection, body);

        // body
        OutputStream os = urlConnection.getOutputStream();
        DataOutputStream out = new DataOutputStream(os);
        IOException exception = null;
        try {
            if (body instanceof String) {
                out.write(((String) body).getBytes(CHARSET));
            } else if (body instanceof byte[]) {
                out.write((byte[]) body);
            } else if (body instanceof JSONObject) {
                out.write(((JSONObject) body).toJSONString().getBytes(CHARSET));
            } else if (body instanceof org.json.JSONObject) {
                out.write(body.toString().getBytes(CHARSET));
            }
            os.flush(); // 开始与对方建立三次握手。
        } catch (IOException e) {
            exception = e;
        } finally {
            out.close();
            os.close();
        }

        if (exception != null) {
            throw exception;
        }

        return urlConnection;
    }

    private static void configUrlConnection(HttpURLConnection urlConnection) {
        urlConnection.setReadTimeout(TIMEOUT);
        urlConnection.setConnectTimeout(TIMEOUT);
        urlConnection.setUseCaches(false);
    }

    private static void buildHeaders(HttpURLConnection urlConnection, final Map<String, String> headers) {
        // common
        urlConnection.setRequestProperty("charset", CHARSET);

        // custom
        if (headers != null) {
            for (String key : headers.keySet()) {
                urlConnection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    private static <T> void buildJsonHeaders(HttpURLConnection urlConnection, T body) {
        if (body instanceof JSONObject || body instanceof org.json.JSONObject) {
            urlConnection.setRequestProperty("Content-Type", "application/json");
        }
    }

    private static String buildString(final InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            int len;
            byte buffer[] = new byte[BUFFER_SIZE];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            is.close();
            os.close();
        }

        return new String(os.toByteArray(), CHARSET);
    }
}
