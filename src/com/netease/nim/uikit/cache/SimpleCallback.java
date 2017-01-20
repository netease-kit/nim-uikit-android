package com.netease.nim.uikit.cache;

/**
 * 简单的回调接口
 * Created by huangjun on 2015/11/19.
 */
public interface SimpleCallback<T> {

    /**
     * 回调函数返回结果
     *
     * @param success 是否成功，结果是否有效
     * @param result  结果
     */
    void onResult(boolean success, T result);
}
