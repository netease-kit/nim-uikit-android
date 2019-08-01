package com.netease.nim.uikit.api.model.main;

import java.util.Set;

/**
 * Created by hzchenkang on 2017/4/5.
 */

public interface OnlineStateChangeObserver {

    /**
     * 通知在线状态事件变化
     *
     * @param account 在线状态事件发生变化的账号
     */
    void onlineStateChange(Set<String> account);
}
