package com.netease.nim.uikit.impl.cache;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * 好友关系缓存
 * 注意：获取通讯录列表即是根据Friend列表帐号，去取对应的UserInfo
 * <p/>
 * Created by huangjun on 2015/9/14.
 */
public class FriendDataCache {

    public static FriendDataCache getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 属性
     */
    private Set<String> friendAccountSet = new CopyOnWriteArraySet<>();

    private Map<String, Friend> friendMap = new ConcurrentHashMap<>();

    /**
     * 初始化&清理
     */

    public void clear() {
        clearFriendCache();
    }

    public void buildCache() {
        // 获取我所有的好友关系
        List<Friend> friends = NIMClient.getService(FriendService.class).getFriends();
        if (friends == null) {
            return;
        }
        for (Friend f : friends) {
            friendMap.put(f.getAccount(), f);
        }

        // 获取我所有好友的帐号
        List<String> accounts = NIMClient.getService(FriendService.class).getFriendAccounts();
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        // 排除黑名单
        List<String> blacks = NIMClient.getService(FriendService.class).getBlackList();
        accounts.removeAll(blacks);

        // 排除掉自己
        accounts.remove(NimUIKit.getAccount());

        // 确定缓存
        friendAccountSet.addAll(accounts);

        LogUtil.i(UIKitLogTag.FRIEND_CACHE, "build FriendDataCache completed, friends count = " + friendAccountSet.size());
    }

    private void clearFriendCache() {
        friendAccountSet.clear();
        friendMap.clear();
    }

    /**
     * ****************************** 好友查询接口 ******************************
     */

    public List<String> getMyFriendAccounts() {
        List<String> accounts = new ArrayList<>(friendAccountSet.size());
        accounts.addAll(friendAccountSet);

        return accounts;
    }

    public int getMyFriendCounts() {
        return friendAccountSet.size();
    }

    public Friend getFriendByAccount(String account) {
        if (TextUtils.isEmpty(account)) {
            return null;
        }

        return friendMap.get(account);
    }

    public boolean isMyFriend(String account) {
        return friendAccountSet.contains(account);
    }

    /**
     * ****************************** 缓存好友关系变更监听&通知 ******************************
     */

    /**
     * 缓存监听SDK
     */
    public void registerObservers(boolean register) {
        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(friendChangedNotifyObserver, register);
        NIMClient.getService(FriendServiceObserve.class).observeBlackListChangedNotify(blackListChangedNotifyObserver, register);
    }

    /**
     * 监听好友关系变化
     */
    private Observer<FriendChangedNotify> friendChangedNotifyObserver = new Observer<FriendChangedNotify>() {
        @Override
        public void onEvent(FriendChangedNotify friendChangedNotify) {
            List<Friend> addedOrUpdatedFriends = friendChangedNotify.getAddedOrUpdatedFriends();
            List<String> myFriendAccounts = new ArrayList<>(addedOrUpdatedFriends.size());
            List<String> friendAccounts = new ArrayList<>(addedOrUpdatedFriends.size());
            List<String> deletedFriendAccounts = friendChangedNotify.getDeletedFriends();

            // 如果在黑名单中，那么不加到好友列表中
            String account;
            for (Friend f : addedOrUpdatedFriends) {
                account = f.getAccount();
                friendMap.put(account, f);
                friendAccounts.add(account);

                if (NIMClient.getService(FriendService.class).isInBlackList(account)) {
                    continue;
                }

                myFriendAccounts.add(account);
            }

            // 更新我的好友关系
            if (!myFriendAccounts.isEmpty()) {
                // update cache
                friendAccountSet.addAll(myFriendAccounts);

                // log
                DataCacheManager.Log(myFriendAccounts, "on add friends", UIKitLogTag.FRIEND_CACHE);
            }

            // 通知好友关系更新
            if (!friendAccounts.isEmpty()) {
                NimUIKitImpl.getContactChangedObservable().notifyAddedOrUpdated(friendAccounts);
            }

            // 处理被删除的好友关系
            if (!deletedFriendAccounts.isEmpty()) {
                // update cache
                friendAccountSet.removeAll(deletedFriendAccounts);

                for (String a : deletedFriendAccounts) {
                    friendMap.remove(a);
                }
                // log
                DataCacheManager.Log(deletedFriendAccounts, "on delete friends", UIKitLogTag.FRIEND_CACHE);

                // notify
                NimUIKitImpl.getContactChangedObservable().notifyDelete(deletedFriendAccounts);
            }
        }
    };

    /**
     * 监听黑名单变化(决定是否加入或者移出好友列表)
     */
    private Observer<BlackListChangedNotify> blackListChangedNotifyObserver = new Observer<BlackListChangedNotify>() {
        @Override
        public void onEvent(BlackListChangedNotify blackListChangedNotify) {
            List<String> addedAccounts = blackListChangedNotify.getAddedAccounts();
            List<String> removedAccounts = blackListChangedNotify.getRemovedAccounts();

            if (!addedAccounts.isEmpty()) {
                // 拉黑，即从好友名单中移除
                friendAccountSet.removeAll(addedAccounts);

                // log
                DataCacheManager.Log(addedAccounts, "on add users to black list", UIKitLogTag.FRIEND_CACHE);

                // notify
                NimUIKitImpl.getContactChangedObservable().notifyAddToBlackList(addedAccounts);
                // 拉黑，要从最近联系人列表中删除该好友
                for (String account : addedAccounts) {
                    NIMClient.getService(MsgService.class).deleteRecentContact2(account, SessionTypeEnum.P2P);
                }
            }

            if (!removedAccounts.isEmpty()) {
                // 移出黑名单，判断是否加入好友名单
                for (String account : removedAccounts) {
                    if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                        friendAccountSet.add(account);
                    }
                }

                // log
                DataCacheManager.Log(removedAccounts, "on remove users from black list", UIKitLogTag.FRIEND_CACHE);

                // 通知观察者
                NimUIKitImpl.getContactChangedObservable().notifyRemoveFromBlackList(removedAccounts);
            }
        }
    };

    /**
     * ************************************ 单例 **********************************************
     */

    static class InstanceHolder {
        final static FriendDataCache instance = new FriendDataCache();
    }
}
