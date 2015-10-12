package com.netease.nim.uikit.contact;

import android.util.Log;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 好友关系缓存
 * 注意：获取通讯录列表即是根据Friend列表帐号，去取对应的UserInfo
 * <p/>
 * Created by huangjun on 2015/9/14.
 */
public class FriendDataCache {

    private static final String TAG = FriendDataCache.class.getSimpleName();

    public static FriendDataCache getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 属性
     */
    private Set<String> friendSet = new HashSet<>();

    private List<FriendDataChangedObserver> friendObservers = new ArrayList<>();

    /**
     * 初始化&清理
     */

    public void clear() {
        clearFriendCache();
    }

    public void buildCache() {
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
        friendSet.clear();
        friendSet.addAll(accounts);

        Log.i(TAG, "build friend data cache completed, friends count = " + friendSet.size());
    }


    private void clearFriendCache() {
        friendSet.clear();
    }

    /**
     * ****************************** 好友查询接口 ******************************
     */

    public List<String> getMyFriendAccounts() {
        if (friendSet.isEmpty()) {
            buildCache();
        }

        List<String> accounts = new ArrayList<>(friendSet.size());
        accounts.addAll(friendSet);

        return accounts;
    }

    public int getMyFriendCounts() {
        if (friendSet.isEmpty()) {
            buildCache();
        }

        return friendSet.size();
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
     * APP监听缓存
     */
    public void registerFriendDataChangedObserver(FriendDataChangedObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!friendObservers.contains(o)) {
                friendObservers.add(o);
            }
        } else {
            friendObservers.remove(o);
        }
    }

    public interface FriendDataChangedObserver {
        void onAddFriend(String account);

        void onDeleteFriend(String account);

        void onAddUserToBlackList(String account);

        void onRemoveUserFromBlackList(String account);
    }

    /**
     * 监听好友关系变化
     */
    private Observer<FriendChangedNotify> friendChangedNotifyObserver = new Observer<FriendChangedNotify>() {
        @Override
        public void onEvent(FriendChangedNotify friendChangedNotify) {
            final String account = friendChangedNotify.getAccount();
            if (friendChangedNotify.getChangeType() == FriendChangedNotify.ChangeType.ADD) {
                // 新增好友
                if (NIMClient.getService(FriendService.class).isInBlackList(account)) {
                    // 如果在黑名单中，那么不加到好友列表中
                    return;
                }

                // 添加好友
                friendSet.add(account);

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onAddFriend(account);
                }

                Log.i(TAG, "on add friend " + account);
            } else if (friendChangedNotify.getChangeType() == FriendChangedNotify.ChangeType.DELETE) {
                // 删除好友
                friendSet.remove(account);

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onDeleteFriend(account);
                }

                Log.i(TAG, "on delete friend " + account);
            }
        }
    };

    /**
     * 监听黑名单变化
     */
    private Observer<BlackListChangedNotify> blackListChangedNotifyObserver = new Observer<BlackListChangedNotify>() {
        @Override
        public void onEvent(BlackListChangedNotify blackListChangedNotify) {
            String account = blackListChangedNotify.getAccount();
            if (blackListChangedNotify.getChangeType() == BlackListChangedNotify.ChangeType.ADD) {
                // 拉黑，即从好友名单中移除
                friendSet.remove(account);

                // 拉黑，要从最近联系人列表中删除该好友
                NIMClient.getService(MsgService.class).deleteRecentContact2(account, SessionTypeEnum.P2P);

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onAddUserToBlackList(account);
                }
            } else if (blackListChangedNotify.getChangeType() == BlackListChangedNotify.ChangeType.REMOVE) {
                // 移出黑名单，判断是否加入好友名单
                if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
                    friendSet.add(account);
                }

                // 通知观察者
                for (FriendDataChangedObserver o : friendObservers) {
                    o.onRemoveUserFromBlackList(account);
                }
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
