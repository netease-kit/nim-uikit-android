/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.model.ait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Team member @ info model
 */
public class AitContactsModel {
    public static final String ACCOUNT_ALL = "ACCOUNT_ALL";

    private final Map<String, AitBlock> aitBlocks = new HashMap<>();

    public void reset() {
        aitBlocks.clear();
    }

    public void addAitMember(String account, String name, int start) {
        AitBlock aitBlock = aitBlocks.get(account);
        if (aitBlock == null) {
            aitBlock = new AitBlock(name);
            aitBlocks.put(account, aitBlock);
        }
        aitBlock.addSegment(start);
    }

    public List<String> getAitTeamMember() {
        List<String> teamMembers = new ArrayList<>();
        for (String account : aitBlocks.keySet()) {
            AitBlock block = aitBlocks.get(account);
            if (block != null && block.valid()) {
                teamMembers.add(account);
            }
        }
        return teamMembers;
    }

    public AitBlock getAitBlock(String account) {
        return aitBlocks.get(account);
    }

    public AitBlock.AitSegment findAitSegmentByEndPos(int start) {
        for (String account : aitBlocks.keySet()) {
            AitBlock block = aitBlocks.get(account);
            AitBlock.AitSegment segment = null;
            if (block != null) {
                segment = block.findLastSegmentByEnd(start);
            }
            if (segment != null) {
                return segment;
            }
        }
        return null;
    }

    public void onInsertText(int start, String changeText) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            if (block != null) {
                block.moveRight(start, changeText);
                if (!block.valid()) {
                    iterator.remove();
                }
            }
        }
    }

    public void onDeleteText(int start, int length) {
        Iterator<String> iterator = aitBlocks.keySet().iterator();
        while (iterator.hasNext()) {
            String account = iterator.next();
            AitBlock block = aitBlocks.get(account);
            if (block != null) {
                block.moveLeft(start, length);
                if (!block.valid()) {
                    iterator.remove();
                }
            }
        }
    }
}
