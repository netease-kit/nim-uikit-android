package com.netease.nim.uikit.impl.cache;

import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;

import java.util.List;

/**
 * UIKit缓存数据管理类
 * <p/>
 * Created by huangjun on 2015/10/19.
 */
public class DataCacheManager {

    private static final String TAG = DataCacheManager.class.getSimpleName();

    /**
     * App初始化时向SDK注册数据变更观察者
     */
    public static void observeSDKDataChanged(boolean register) {
        UIKitOptions options = NimUIKitImpl.getOptions();
        if (options.buildFriendCache) {
            FriendDataCache.getInstance().registerObservers(register);
        }
        if (options.buildNimUserCache) {
            NimUserInfoCache.getInstance().registerObservers(register);
        }
        if (options.buildTeamCache) {
            TeamDataCache.getInstance().registerObservers(register);
        }
        if (options.buildRobotInfoCache) {
            RobotInfoCache.getInstance().registerObservers(register);
        }
    }

    /**
     * 本地缓存构建(异步)
     */
    public static void buildDataCacheAsync() {
        NimSingleThreadExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                buildDataCache();
                LogUtil.i(TAG, "build data cache completed");
                NimUIKitImpl.notifyCacheBuildComplete();
            }
        });
    }

    /**
     * 本地缓存构建（同步）
     */
    public static void buildDataCache() {
        // clear
        clearDataCache();

        UIKitOptions options = NimUIKitImpl.getOptions();
        // build user/friend/team data cache
        if (options.buildFriendCache) {
            FriendDataCache.getInstance().buildCache();
        }
        if (options.buildNimUserCache) {
            NimUserInfoCache.getInstance().buildCache();
        }
        if (options.buildTeamCache) {
            TeamDataCache.getInstance().buildCache();
        }
        if (options.buildRobotInfoCache) {
            RobotInfoCache.getInstance().buildCache();
        }

        // chat room member cache 在进入聊天室之后构建
    }

    /**
     * 清空缓存（同步）
     */
    public static void clearDataCache() {
        UIKitOptions options = NimUIKitImpl.getOptions();

        // clear user/friend/team data cache
        if (options.buildFriendCache) {
            FriendDataCache.getInstance().clear();
        }
        if (options.buildNimUserCache) {
            NimUserInfoCache.getInstance().clear();
        }
        if (options.buildTeamCache) {
            TeamDataCache.getInstance().clear();
        }
        if (options.buildRobotInfoCache) {
            RobotInfoCache.getInstance().clear();
        }
    }

    public static void buildRobotCacheIndependent(String roomId) {
        RobotInfoCache.getInstance().pullRobotListIndependent(roomId, null);
    }

    /**
     * 输出缓存数据变更日志
     */
    public static void Log(List<String> accounts, String event, String logTag) {
        StringBuilder sb = new StringBuilder();
        sb.append(event);
        sb.append(" : ");
        for (String account : accounts) {
            sb.append(account);
            sb.append(" ");
        }
        sb.append(", total size=" + accounts.size());

        LogUtil.i(logTag, sb.toString());
    }
}
