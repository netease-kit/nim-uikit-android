package com.netease.nim.uikit.api.model.robot;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;

import java.util.List;

/**
 * 智能机器人信息提供者
 */

public interface RobotInfoProvider {

    /**
     * 根据 id 获取智能机器人
     *
     * @param account 智能机器人id
     * @return NimRobotInfo
     */
    NimRobotInfo getRobotByAccount(String account);

    /**
     * 获取所有的智能机器人
     *
     * @return 智能机器人列表
     */
    List<NimRobotInfo> getAllRobotAccounts();

    /**
     * IM 模式下，获取(异步)智能机器人
     */
    void fetchRobotList(SimpleCallback<List<NimRobotInfo>> callback);

    /**
     * 独立聊天室模式下，获取(异步)智能机器人
     *
     * @param roomId 聊天室id
     */
    void fetchRobotListIndependent(String roomId, SimpleCallback<List<NimRobotInfo>> callback);
}
