package com.netease.nim.uikit.api.model.contact;

import java.util.List;

/**
 * UIKit 与 app 好友关系变化监听接口
 */

public interface ContactChangedObserver {

    /**
     * 增加或者更新好友
     *
     * @param accounts 账号列表
     */
    void onAddedOrUpdatedFriends(List<String> accounts);

    /**
     * 删除好友
     *
     * @param accounts 账号列表
     */
    void onDeletedFriends(List<String> accounts);

    /**
     * 增加到黑名单
     *
     * @param accounts 账号列表
     */
    void onAddUserToBlackList(List<String> accounts);

    /**
     * 从黑名单移除
     *
     * @param accounts 账号列表
     */
    void onRemoveUserFromBlackList(List<String> accounts);
}
