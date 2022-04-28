/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.common;

import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {

    public static Map<String,UserInfo> getUserInfoMap(List<UserInfo> data){
        if (data == null || data.size() < 1){
            return null;
        }
        Map<String,UserInfo> result = new HashMap<>();
        for (int index = 0; index < data.size(); index++) {
            result.put(data.get(index).getAccount(),data.get(index));
        }
        return result;
    }

    public static Map<String,FriendInfo> getFriendInfoMap(List<FriendInfo> data){
        if (data == null || data.size() < 1){
            return null;
        }
        Map<String,FriendInfo> result = new HashMap<>();
        for (int index = 0; index < data.size(); index++) {
            result.put(data.get(index).getAccount(),data.get(index));
        }
        return result;
    }

    public static Map<String,Team> getTeamInfoMap(List<Team> data){
        if (data == null || data.size() < 1){
            return null;
        }
        Map<String,Team> result = new HashMap<>();
        for (int index = 0; index < data.size(); index++) {
            result.put(data.get(index).getId(),data.get(index));
        }
        return result;
    }

}
