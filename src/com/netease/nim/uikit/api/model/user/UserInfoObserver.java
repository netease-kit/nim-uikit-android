package com.netease.nim.uikit.api.model.user;

import java.util.List;

/**
 * UIKit 与 app 好友关系变化监听接口
 */

public interface UserInfoObserver {

    /**
     * 用户信息变更
     *
     * @param accounts 账号列表
     */
    void onUserInfoChanged(List<String> accounts);
}
