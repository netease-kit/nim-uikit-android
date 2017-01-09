package com.netease.nim.uikit.custom;

import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.contact.ContactProvider;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit默认的通讯录（联系人）数据源提供者，
 * Created by hzchenkang on 2016/12/19.
 */

public class DefaultContactProvider implements ContactProvider {

    @Override
    public List<UserInfoProvider.UserInfo> getUserInfoOfMyFriends() {
        List<NimUserInfo> nimUsers = NimUserInfoCache.getInstance().getAllUsersOfMyFriend();
        List<UserInfoProvider.UserInfo> users = new ArrayList<>(nimUsers.size());
        if (!nimUsers.isEmpty()) {
            users.addAll(nimUsers);
        }

        return users;
    }

    @Override
    public int getMyFriendsCount() {
        return FriendDataCache.getInstance().getMyFriendCounts();
    }

    @Override
    public String getUserDisplayName(String account) {
        return NimUserInfoCache.getInstance().getUserDisplayName(account);
    }
}
