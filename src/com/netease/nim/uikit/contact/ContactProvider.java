package com.netease.nim.uikit.contact;

import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import java.util.List;

/**
 * 通讯录（联系人）数据源提供者
 */
public interface ContactProvider {
    /**
     * 返回本地所有好友用户信息（通讯录一般列出所有的好友）
     *
     * @return 用户信息集合
     */
    List<UserInfoProvider.UserInfo> getUserInfoOfMyFriends();

    /**
     * 返回我的好友数量，提供给通讯录显示所有联系人数量使用
     *
     * @return 好友个数
     */
    int getMyFriendsCount();

    /**
     * 返回一个用户显示名（例如：如果有昵称显示昵称，如果没有显示帐号）
     *
     * @param account 用户帐号
     * @return 显示名
     */
    String getUserDisplayName(String account);
}
