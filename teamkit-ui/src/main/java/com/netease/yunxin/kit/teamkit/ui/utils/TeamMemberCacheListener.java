// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import java.util.List;

/** 群成员缓存监听 */
public interface TeamMemberCacheListener {
  /**
   * 群成员缓存更新通知，包括群成员信息的变更、用户信息变更和好友信息变更
   *
   * @param teamId 群ID
   * @param teamMemberList 群成员列表
   */
  void onTeamMemberCacheUpdate(String teamId, List<TeamMemberWithUserInfo> teamMemberList);

  /**
   * 群成员缓存移除通知，群成员被移除该群
   *
   * @param teamId 群ID
   * @param account 账号列表
   */
  void onTeamMemberCacheRemove(String teamId, List<String> account);

  /**
   * 群成员缓存加载通知，群成员列表拉取完成
   *
   * @param teamId 群ID
   * @param teamMemberList 群成员列表
   */
  void onTeamMemberCacheLoad(String teamId, List<TeamMemberWithUserInfo> teamMemberList);

  /**
   * 群成员缓存添加通知，群中新增群成员时调用
   *
   * @param teamId 群ID
   * @param teamMemberList 群成员列表
   */
  void onTeamMemberCacheAdd(String teamId, List<TeamMemberWithUserInfo> teamMemberList);
}
