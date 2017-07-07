package com.netease.nim.uikit.cache;

import android.text.TextUtils;

import com.netease.nim.uikit.UIKitLogTag;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.robot.RobotService;
import com.netease.nimlib.sdk.robot.RobotServiceObserve;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.robot.model.RobotChangedNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 机器人缓存
 * <p/>
 * Created by huangjun on 2017/6/22.
 */
public class RobotInfoCache {

    public static RobotInfoCache getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 数据
     */

    private Map<String, NimRobotInfo> robotMap = new ConcurrentHashMap<>();

    /**
     * 初始化&清理
     */

    public void clear() {
        clearRobotCache();
    }

    void buildCache() {
        // 获取所有有效的机器人
        List<NimRobotInfo> robots = NIMClient.getService(RobotService.class).getAllRobots();
        for (NimRobotInfo r : robots) {
            robotMap.put(r.getAccount(), r);
        }

        LogUtil.i(UIKitLogTag.ROBOT_CACHE, "build RobotInfoCache completed, robots count = " + robotMap.size());
    }

    private void clearRobotCache() {
        robotMap.clear();
    }

    /**
     * ****************************** 机器人信息查询接口 ******************************
     */

    public List<NimRobotInfo> getAllRobotAccounts() {
        return new ArrayList<>(robotMap.values());
    }

    public NimRobotInfo getRobotByAccount(String account) {
        if (TextUtils.isEmpty(account)) {
            return null;
        }

        return robotMap.get(account);
    }

    /**
     * ****************************** 缓存机器人变更监听&通知 ******************************
     */

    /**
     * 缓存监听SDK
     */
    public void registerObservers(boolean register) {
        NIMClient.getService(RobotServiceObserve.class).observeRobotChangedNotify(robotChangedNotifyObserver, register);
    }

    /**
     * 监听机器人变化
     */
    private Observer<RobotChangedNotify> robotChangedNotifyObserver = new Observer<RobotChangedNotify>() {
        @Override
        public void onEvent(RobotChangedNotify robotChangedNotify) {
            List<NimRobotInfo> addedOrUpdatedRobots = robotChangedNotify.getAddedOrUpdatedRobots();
            List<String> addedOrUpdateRobotAccounts = new ArrayList<>(addedOrUpdatedRobots.size());
            List<String> deletedRobotAccounts = robotChangedNotify.getDeletedRobots();

            String account;
            for (NimRobotInfo f : addedOrUpdatedRobots) {
                account = f.getAccount();
                robotMap.put(account, f);
                addedOrUpdateRobotAccounts.add(account);
            }

            // 通知机器人变更
            if (!addedOrUpdateRobotAccounts.isEmpty()) {
                // log
                DataCacheManager.Log(addedOrUpdateRobotAccounts, "on add robot", UIKitLogTag.ROBOT_CACHE);
            }

            // 处理被删除的机器人
            if (!deletedRobotAccounts.isEmpty()) {
                // update cache
                for (String a : deletedRobotAccounts) {
                    robotMap.remove(a);
                }

                // log
                DataCacheManager.Log(deletedRobotAccounts, "on delete robots", UIKitLogTag.FRIEND_CACHE);
            }
        }
    };

    /**
     * ************************************ 单例 **********************************************
     */

    static class InstanceHolder {
        final static RobotInfoCache instance = new RobotInfoCache();
    }
}
