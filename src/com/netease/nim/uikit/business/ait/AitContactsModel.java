package com.netease.nim.uikit.business.ait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hzchenkang on 2017/7/7.
 *
 * @ 联系人数据
 */

public class AitContactsModel {

    // 已@ 的成员
    private Map<String, AitBlock> aitBlocks = new HashMap<>();

    // 清除所有的@块
    public void reset() {
        aitBlocks.clear();
    }

    public void addAitMember(String account, String name, int type, int start) {
        AitBlock aitBlock = aitBlocks.get(account);
        if (aitBlock == null) {
            aitBlock = new AitBlock(name, type);
            aitBlocks.put(account, aitBlock);
        }
        aitBlock.addSegment(start);
    }

    // 查所有被@的群成员
    public List<String> getAitTeamMember() {
        List<String> teamMembers = new ArrayList<>();
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            if (block.aitType == AitContactType.TEAM_MEMBER && block.valid()) {
                teamMembers.add(account);
            }
        }
        return teamMembers;
    }

    public AitBlock getAitBlock(String account) {
        return aitBlocks.get(account);
    }

    // 查第一个被@ 的机器人
    public String getFirstAitRobot() {
        int start = -1;
        String robotAccount = null;

        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            if (block.valid() && block.aitType == AitContactType.ROBOT) {
                int blockStart = block.getFirstSegmentStart();
                if (blockStart == -1) {
                    continue;
                }
                if (start == -1 || blockStart < start) {
                    start = blockStart;
                    robotAccount = account;
                }
            }
        }
        return robotAccount;
    }

    // 找到 curPos 恰好命中 end 的segment
    public AitBlock.AitSegment findAitSegmentByEndPos(int start) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            AitBlock.AitSegment segment = block.findLastSegmentByEnd(start);
            if (segment != null) {
                return segment;
            }
        }
        return null;
    }

    // 文本插入后更新@块的起止位置
    public void onInsertText(int start, String changeText) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            block.moveRight(start, changeText);
            if (!block.valid()) {
                iterator.remove();
            }
        }
    }

    // 文本删除后更新@块的起止位置
    public void onDeleteText(int start, int length) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            block.moveLeft(start, length);
            if (!block.valid()) {
                iterator.remove();
            }
        }
    }
}
